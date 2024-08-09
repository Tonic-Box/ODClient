package osrs.dev.mappings;

import javassist.*;
import javassist.bytecode.Opcode;
import osrs.dev.annotations.mapping.Definition;
import osrs.dev.annotations.mapping.MappingSet;
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

@MappingSet
public class ClientMap
{
    @Definition(targets = {"clientField"})
    public static void findClientField(CtField field)
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

    @Definition(targets = {
            "Client","GameEngine","processServerPacket", "init",
            "JX_ACCESS_TOKEN","JX_REFRESH_TOKEN", "JX_SESSION_ID",
            "JX_CHARACTER_ID","JX_DISPLAY_NAME", "run",
            "graphicsTick","clientTick","doCycle","graphicsTick"
    })
    public static void findClient(CtClass clazz)
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
            for(CtMethod method : clazz.getDeclaredMethods())
            {
                findServerPacketReceiver(method);
            }
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

    public static void findGraphicsTick(CtClass clazz) throws Exception
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

    public static void findServerPacketReceiver(CtMethod method)
    {
        try
        {
            int len = method.getMethodInfo2().getCodeAttribute().getCodeLength();
            if(len < 18_000 || len > 22_000)
                return;

            if(!method.getReturnType().getName().equals("boolean"))
                return;

            Mappings.addMethod("processServerPacket", method.getName(), method.getDeclaringClass().getName(), method.getMethodInfo2().getDescriptor());
        }
        catch (Exception ignored) {
        }
    }
}
