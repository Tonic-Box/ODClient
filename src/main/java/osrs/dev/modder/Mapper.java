package osrs.dev.modder;

import javassist.*;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import osrs.dev.modder.model.Mapping;
import osrs.dev.modder.model.Mappings;
import osrs.dev.modder.model.javassist.CodeBlock;
import osrs.dev.modder.model.javassist.MethodDefinition;
import osrs.dev.modder.model.javassist.enums.BlockType;
import osrs.dev.modder.model.javassist.instructions.FieldLine;
import osrs.dev.modder.model.javassist.instructions.MethodLine;
import osrs.dev.util.modding.CodeUtil;

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
        if(Mappings.findByTag("Login_username") != null)
            return;

        MethodDefinition definition = new MethodDefinition(method);
        if(!definition.containsBlockWithValue("Please enter your username/email address."))
            return;

        FieldLine fieldLine;
        String username = null;
        for(CodeBlock block : definition.getBody())
        {
            if(block.hasMethodCall("java.lang.String", "trim", "()Ljava/lang/String;"))
            {
                fieldLine = block.findFirst(m -> m.getOpcode() == Opcode.GETSTATIC);
                if(fieldLine != null)
                {
                    Mappings.addField("Login_username", fieldLine.getName(), fieldLine.getClazz(), fieldLine.getType());
                    username = fieldLine.getName();
                }
            }

            if(block.hasMethodCall("java.lang.String", "length", "()I") && username != null)
            {
                String finalUsername = username;
                FieldLine methodLine = block.findFirst(m -> {
                    if(m.getOpcode() != Opcode.GETSTATIC)
                        return false;

                    FieldLine line = m.transpose();
                    return !line.getName().equals(finalUsername);
                });
                if(methodLine != null)
                {
                    Mappings.addField("Login_password", methodLine.getName(), methodLine.getClazz(), methodLine.getType());
                }
            }
        }
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

                Mappings.addMethod("getDeviceId", method.getName(), method.getDeclaringClass().getName(), method.getMethodInfo2().getDescriptor());
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
                Mappings.addField("clientField", field.getName(), field.getDeclaringClass().getName(), field.getFieldInfo2().getDescriptor());
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
        Mappings.addMethodNoGarbage("init", init.getName(), clazz.getName(), init.getMethodInfo2().getDescriptor());
        MethodDefinition definition = new MethodDefinition(init);
        FieldLine fieldLine;
        for(CodeBlock block : definition.getBody())
        {
            fieldLine = block.findFirst(i -> i instanceof FieldLine);
            if(block.containsValue("JX_ACCESS_TOKEN"))
            {
                Mappings.addField("JX_ACCESS_TOKEN", fieldLine.getName(), fieldLine.getClazz(), fieldLine.getType());
            }
            else if(block.containsValue("JX_REFRESH_TOKEN"))
            {
                Mappings.addField("JX_REFRESH_TOKEN", fieldLine.getName(), fieldLine.getClazz(), fieldLine.getType());
            }
            else if(block.containsValue("JX_SESSION_ID"))
            {
                Mappings.addField("JX_SESSION_ID", fieldLine.getName(), fieldLine.getClazz(), fieldLine.getType());
            }
            else if(block.containsValue("JX_CHARACTER_ID"))
            {
                Mappings.addField("JX_CHARACTER_ID", fieldLine.getName(), fieldLine.getClazz(), fieldLine.getType());
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
                    Mappings.addField("JX_DISPLAY_NAME", fieldLine.getName(), fieldLine.getClazz(), fieldLine.getType());
                }
            }
        }
    }

    public static void findRunAndStuff(CtClass clazz) throws Exception
    {
        if(Mappings.findByTag("run") != null)
            return;

        CtMethod run = clazz.getMethod("run", "()V");
        Mappings.addMethodNoGarbage("run", run.getName(), clazz.getName(), run.getMethodInfo2().getDescriptor());
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
        Mappings.addMethod("clientTick", clientTick.getName(), clazz.getName(), clientTick.getMethodInfo2().getDescriptor());

        methods.clear();
        CodeUtil.scanForMethodRefs(clientTick, methods);

        for(CtMethod method : methods)
        {
            if(!method.getDeclaringClass().getName().equals("client"))
                continue;
            Mappings.addMethod("doCycle", method.getName(), "client", method.getMethodInfo2().getDescriptor());
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

            Mappings.addMethod("graphicsTick", method.getName(), method.getDeclaringClass().getName(), method.getMethodInfo2().getDescriptor());
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

        Mappings.addMethod("menuAction", method.getName(), method.getDeclaringClass().getName(), info.getDescriptor());
    }
}
