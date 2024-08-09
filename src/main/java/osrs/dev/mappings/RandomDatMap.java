package osrs.dev.mappings;

import javassist.CtMethod;
import javassist.bytecode.Opcode;
import osrs.dev.annotations.mapping.Definition;
import osrs.dev.annotations.mapping.MappingSet;
import osrs.dev.modder.model.Mappings;
import osrs.dev.modder.model.javassist.CodeBlock;
import osrs.dev.modder.model.javassist.MethodDefinition;
import osrs.dev.modder.model.javassist.enums.BlockType;
import osrs.dev.modder.model.javassist.instructions.FieldLine;

@MappingSet
public class RandomDatMap
{
    @Definition(targets = {"readFromRandomDat"})
    public static void findReadRandomDat(CtMethod method)
    {
        try
        {
            if(!method.getLongName().startsWith("ii.an(") || !method.getMethodInfo2().getDescriptor().equals("(Lvp;II)V"))
                return;

            if(method.getParameterTypes().length != 3)
                return;

            if(!method.getMethodInfo2().getDescriptor().endsWith(")V"))
                return;

            if(!method.getParameterTypes()[1].getName().equals("int"))
                return;

            int len = method.getMethodInfo2().getCodeAttribute().getCodeLength();
            if(len < 50 || len > 100)
                return;

//            MethodDefinition definition = new MethodDefinition(method);
//            System.out.println(definition);
//            System.exit(0);
            Mappings.addMethod("readFromRandomDat", method.getName(), method.getDeclaringClass().getName(), method.getMethodInfo2().getDescriptor());
        }
        catch (Exception ignored) {}
    }

    @Definition(targets = {"writeToRandomDat"})
    public static void findWriteRandomDat(CtMethod method)
    {
        try
        {
            if(method.getParameterTypes().length != 3)
                return;

            if(!method.getMethodInfo2().getDescriptor().endsWith(")V"))
                return;

            if(!method.getParameterTypes()[1].getName().equals("int"))
                return;

            int len = method.getMethodInfo2().getCodeAttribute().getCodeLength();
            if(len < 50 || len > 100)
                return;

            MethodDefinition definition = new MethodDefinition(method);
            if(!definition.getBody().get(0).getBlockType().equals(BlockType.LOCAL_STORE))
                return;

            boolean pass = false;
            for(CodeBlock block : definition.getBody())
            {
                if(!block.getBlockType().equals(BlockType.VOID_METHOD_CALL))
                    continue;

                if(!block.containsAll(Opcode.ICONST_0, Opcode.BIPUSH, Opcode.GETSTATIC, Opcode.INVOKESTATIC))
                    continue;

                if(!block.containsValue(0) || !block.containsValue(24))
                    continue;

                FieldLine line = block.findFirst(m -> m.hasOpcode(Opcode.GETSTATIC) && ((FieldLine)m).getType().equals("[B"));
                if(line == null)
                    return;

                Mappings.addField("randomDatData", line.getName(), line.getClazz(), line.getType());

                pass = true;
                break;
            }

            if(!pass)
                return;

            Mappings.addMethod("writeToRandomDat", method.getName(), method.getDeclaringClass().getName(), method.getMethodInfo2().getDescriptor());
        }
        catch (Exception ignored) {}
    }
}
