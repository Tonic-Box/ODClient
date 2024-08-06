package osrs.dev.modder;

import javassist.*;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import osrs.dev.modder.model.Mapping;
import osrs.dev.modder.model.Mappings;
import osrs.dev.modder.model.ast.CodeBlock;
import osrs.dev.modder.model.ast.MethodDefinition;
import osrs.dev.modder.model.ast.enums.BlockType;
import osrs.dev.modder.model.ast.instructions.FieldLine;
import osrs.dev.modder.model.ast.instructions.MethodLine;
import osrs.dev.util.modding.CodeUtil;
import osrs.dev.util.modding.Descriptor;

import java.util.ArrayList;
import java.util.List;

public class Mapper
{
    /**
     * handles finding our mappings
     */
    public static void map()
    {
        for(CtClass clazz : Mappings.getClasses())
        {
            clazz.defrost();
            findClient(clazz);

            for(CtMethod method : clazz.getDeclaredMethods())
            {
                if(!Mappings.getUsedMethods().contains(method.getLongName()))
                    continue;
                findDoAction(method);
                findGetDeviceId(method);
                mapLoginUsername(method);
            }

            for(CtField field : clazz.getDeclaredFields())
            {
                findClientField(field);
            }
        }
    }

    private static void mapLoginUsername(CtMethod method)
    {
        try
        {
            if(Mappings.findByTag("Login_username") != null)
                return;

            if(method.getParameterTypes().length != 1)
                return;

            if(!method.getMethodInfo2().getDescriptor().endsWith(")V"))
                return;

            int len = method.getMethodInfo2().getCodeAttribute().getCodeLength();
            if(len < 90 || len > 110)
                return;

            MethodDefinition methodDefinition = new MethodDefinition(method);

            int conditions = methodDefinition.countBlocksOfType(BlockType.CONDITION);
            int fieldStores = methodDefinition.countBlocksOfType(BlockType.FIELD_STORE);

            if(fieldStores != 2 || conditions != 6)
                return;

            for(CodeBlock block : methodDefinition.getBody())
            {
                if(block.contains(Opcode.IFNULL))
                {
                    FieldLine fieldLine = (FieldLine) block.getInstructions().get(0);
                    Mappings.addField("Login_username", fieldLine.getName(), fieldLine.getClazz(), fieldLine.getType(), -1);
                    Mappings.addMethod("focusPasswordWhenUsernameFilled", method.getName(), method.getDeclaringClass().getName(), method.getMethodInfo2().getDescriptor(), method.getModifiers());
                    return;
                }
            }
        }
        catch (Exception ignored)
        {}
    }

    public static void findGetDeviceId(CtMethod method)
    {
        if(Mappings.findByTag("getDeviceId") != null)
            return;

        try
        {
            MethodDefinition methodDefinition = new MethodDefinition(method);

            for(CodeBlock block : methodDefinition.getBody())
            {
                if(!block.getBlockType().equals(BlockType.LOCAL_STORE))
                    continue;

                if(!block.containsValue("12345678-0000-0000-0000-123456789012"))
                    continue;

                Mappings.addMethod("getDeviceId", method.getName(), method.getDeclaringClass().getName(), method.getMethodInfo2().getDescriptor(), method.getModifiers());
                Mappings.addClass("PlatformInfo", method.getDeclaringClass().getName());
                return;
            }
        }
        catch (Exception ignored)
        {
        }
    }

    private static void findClientField(CtField field)
    {
        if(Mappings.findByTag("clientField") != null)
            return;

        try
        {
            if(field.getType().getName().equals("client"))
            {
                Mappings.addField("clientField", field.getName(), field.getDeclaringClass().getName(), field.getFieldInfo2().getDescriptor(), field.getModifiers());
            }
        }
        catch (Exception ignored) {}
    }

    private static void findClient(CtClass clazz)
    {
        if(Mappings.findByTag("Client") != null)
            return;
        try
        {
            if(!clazz.getName().equals("client"))
                return;
            Mappings.addClass("Client", clazz.getName());

            CtClass gameEngine = clazz.getSuperclass();
            Mappings.addClass("GameEngine", gameEngine.getName());
            findGraphicsTick(gameEngine);
            findRunAndStuff(gameEngine);
            findJagAuthStuff(clazz);
        }
        catch (Exception ignored) {
        }
    }

    public static void findJagAuthStuff(CtClass clazz) throws Exception
    {
        CtMethod init = clazz.getMethod("init", "()V");
        Mappings.addMethodNoGarbage("init", init.getName(), clazz.getName(), init.getMethodInfo2().getDescriptor(), init.getModifiers());

        MethodDefinition definition = new MethodDefinition(init);
        FieldLine fieldLine;
        for(CodeBlock block : definition.getBody())
        {
            fieldLine = block.findFirst(i -> i instanceof FieldLine);
            if(block.containsValue("JX_ACCESS_TOKEN"))
            {
                Mappings.addField("JX_ACCESS_TOKEN", fieldLine.getName(), fieldLine.getClazz(), fieldLine.getType(), -1);
            }
            else if(block.containsValue("JX_REFRESH_TOKEN"))
            {
                Mappings.addField("JX_REFRESH_TOKEN", fieldLine.getName(), fieldLine.getClazz(), fieldLine.getType(), -1);
            }
            else if(block.containsValue("JX_SESSION_ID"))
            {
                Mappings.addField("JX_SESSION_ID", fieldLine.getName(), fieldLine.getClazz(), fieldLine.getType(), -1);
            }
            else if(block.containsValue("JX_CHARACTER_ID"))
            {
                Mappings.addField("JX_CHARACTER_ID", fieldLine.getName(), fieldLine.getClazz(), fieldLine.getType(), -1);
            }
            else if(block.containsValue("JX_DISPLAY_NAME"))
            {
                MethodLine methodLine = block.findFirst(line -> line.hasOpcode(Opcode.INVOKESTATIC) && !((MethodLine)line).getName().equals("getenv"));
                CtClass _clazz = Mappings.getClazz(methodLine.getClazz());
                CtMethod method = _clazz.getMethod(methodLine.getName(), methodLine.getType());
                MethodDefinition methodDefinition = new MethodDefinition(method);
                for(CodeBlock block1 : methodDefinition.getBody())
                {
                    if(!block1.getBlockType().equals(BlockType.FIELD_STORE))
                        continue;

                    fieldLine = block1.findFirst(i -> i.getOpcode() == Opcode.PUTSTATIC);
                    Mappings.addField("JX_DISPLAY_NAME", fieldLine.getName(), fieldLine.getClazz(), fieldLine.getType(), -1);
                }
            }
        }
    }

    public static void findRunAndStuff(CtClass clazz) throws Exception
    {
        if(Mappings.findByTag("run") != null)
            return;

        CtMethod run = clazz.getMethod("run", "()V");
        Mappings.addMethodNoGarbage("run", run.getName(), clazz.getName(), run.getMethodInfo2().getDescriptor(), run.getModifiers());
        Mapping mapping = Mappings.findByTag("graphicsTick");
        String name = mapping.getObfuscatedName();
        String descriptor = mapping.getDescriptor();
        List<CtMethod> methods = new ArrayList<>();
        CodeUtil.scanForMethodRefs(run, methods);
        CtMethod clientTick = null;
        for(int i = 0; i < methods.size(); i++)
        {
            if(name.equals(methods.get(i).getName()) && descriptor.equals(methods.get(i).getMethodInfo2().getDescriptor()))
            {
                clientTick = methods.get(i - 1);
                break;
            }
        }
        if(clientTick == null)
        {
            throw new NotFoundException("ClientTick not found");
        }
        Mappings.addMethod("clientTick", clientTick.getName(), clazz.getName(), clientTick.getMethodInfo2().getDescriptor(), clientTick.getModifiers());

        methods.clear();
        CodeUtil.scanForMethodRefs(clientTick, methods);

        for(CtMethod method : methods)
        {
            if(!method.getDeclaringClass().getName().equals("client"))
                continue;
            Mappings.addMethod("doCycle", method.getName(), "client", method.getMethodInfo2().getDescriptor(), method.getModifiers());
            break;
        }
    }

    private static void findGraphicsTick(CtClass clazz) throws Exception
    {
        for(CtMethod method : clazz.getDeclaredMethods())
        {
            int mod = method.getModifiers();
            if(Modifier.isAbstract(mod))
                continue;

            if(!method.getReturnType().getName().equals("void"))
                continue;

            if(Modifier.isStatic(mod))
                continue;

            if(method.getParameterTypes().length != 1)
                continue;

            int length = method.getMethodInfo2().getCodeAttribute().getCodeLength();
            if(length < 420 || length > 475)
                continue;

            Mappings.addMethod("graphicsTick", method.getName(), method.getDeclaringClass().getName(), method.getMethodInfo2().getDescriptor(), method.getModifiers());
        }
    }



    private static void findDoAction(CtMethod method)
    {
        if(Mappings.findByTag("menuAction") != null)
            return;

        MethodInfo info = method.getMethodInfo2();
        if(!info.getDescriptor().startsWith("(IIIIIILjava/lang/String;Ljava/lang/String;II") || !info.getDescriptor().endsWith(")V"))
            return;

        if(info.getCodeAttribute().getCodeLength() < 5000)
            return;

        Mappings.addMethod("menuAction", method.getName(), method.getDeclaringClass().getName(), info.getDescriptor(), method.getModifiers());
    }
}
