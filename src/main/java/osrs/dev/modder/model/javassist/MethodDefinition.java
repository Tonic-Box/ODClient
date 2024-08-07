package osrs.dev.modder.model.javassist;

import javassist.CtMethod;
import javassist.Modifier;
import javassist.bytecode.Opcode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import osrs.dev.modder.model.javassist.enums.BlockType;
import osrs.dev.modder.model.javassist.instructions.FieldLine;
import osrs.dev.modder.model.javassist.instructions.InstructionLine;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
public class MethodDefinition {
    public MethodDefinition(CtMethod method)
    {
        name = method.getName();
        declaringClass = method.getDeclaringClass().getName();
        modifiers = method.getModifiers();
        descriptor = method.getMethodInfo2().getDescriptor();
        returnType = descriptor.substring(descriptor.indexOf(')') + 1)
                .replaceFirst("^L(.+);$", "$1")
                .replace('/', '.');
        parameters = getParameterTypes(descriptor);
        if(Modifier.isAbstract(modifiers))
            body = new ArrayList<>();
        else
            body = Iterator.getCleanFlow(method);

        int inst = 0;
        for(CodeBlock block : getBody())
        {
            inst += block.getInstructions().size();
        }
        length = inst;
        signature = modifiers + "/" + descriptor + "/" + returnType;
    }
    @Setter
    private String tag;
    @Setter
    private String parentTag;
    private final String name;
    private final String declaringClass;
    private final int modifiers;
    private final String descriptor;
    private final String returnType;
    private final String[] parameters;
    private final List<CodeBlock> body;
    private final int length;
    @Setter
    private int localCalls;
    @Setter
    private int globalCalls;
    private final String signature;

    public int countBlocksOfType(BlockType type)
    {
        return (int) getBody().stream().filter(b -> b.getBlockType().equals(type)).count();
    }

    public <T extends Number> boolean containsBlockWithValue(T value)
    {
        for(CodeBlock block : body)
        {
            if(block.containsValue(value))
                return true;

        }
        return false;
    }

    public boolean containsBlockWithValue(String value)
    {
        for(CodeBlock block : body)
        {
            if(block.containsValue(value))
                return true;

        }
        return false;
    }

    private static String[] getParameterTypes(String descriptor) {
        List<String> parameterTypes = new ArrayList<>();
        int index = 1; // start after the opening parenthesis

        while (descriptor.charAt(index) != ')') {
            if (descriptor.charAt(index) == 'L') { // reference type
                int endIndex = descriptor.indexOf(';', index);
                String parameterType = descriptor.substring(index + 1, endIndex).replace('/', '.');
                parameterTypes.add(parameterType);
                index = endIndex + 1; // move to the next character after the semicolon
            } else if (descriptor.charAt(index) == '[') { // array type
                int endIndex = index + 1;
                while (descriptor.charAt(endIndex) == '[') {
                    endIndex++;
                }
                if (descriptor.charAt(endIndex) == 'L') { // array of reference type
                    endIndex = descriptor.indexOf(';', endIndex);
                }
                String parameterType = descriptor.substring(index, endIndex + 1).replace('/', '.');
                parameterTypes.add(parameterType);
                index = endIndex + 1; // move to the next character after the closing bracket
            } else { // primitive type
                String parameterType = descriptor.substring(index, index + 1);
                switch (parameterType) {
                    case "B":
                        parameterTypes.add("byte");
                        break;
                    case "C":
                        parameterTypes.add("char");
                        break;
                    case "D":
                        parameterTypes.add("double");
                        break;
                    case "F":
                        parameterTypes.add("float");
                        break;
                    case "I":
                        parameterTypes.add("int");
                        break;
                    case "J":
                        parameterTypes.add("long");
                        break;
                    case "S":
                        parameterTypes.add("short");
                        break;
                    case "Z":
                        parameterTypes.add("boolean");
                        break;
                }
                index++; // move to the next character
            }
        }

        return parameterTypes.toArray(new String[0]);
    }

    @Override
    public String toString()
    {
        StringBuilder info = new StringBuilder("# " + getDeclaringClass() + "." + getName() + getDescriptor() + "\n");
        int i = 0;
        for(CodeBlock block : getBody())
        {
            info.append("Block ").append(i++).append(" [").append(block.getBlockType()).append("]\n");
            for(InstructionLine line : block.getInstructions())
            {
                info.append("\t").append(line.getInfo()).append("\n");
            }
        }
        return info.toString();
    }

    public String toString(int i, CodeBlock block)
    {
        StringBuilder info = new StringBuilder();
        info.append("Block ").append(i).append(" [").append(block.getBlockType()).append("]\n");
        for(InstructionLine line : block.getInstructions())
        {
            info.append("\t").append(line.getInfo()).append("\n");
        }
        return info.toString();
    }

    public String getLongName()
    {
        return getDeclaringClass() + "." + getName() + getDescriptor();
    }

    public String toStringCompact()
    {
        StringBuilder info = new StringBuilder("# " + getDescriptor() + "\n");
        int i = 0;
        for(CodeBlock block : getBody())
        {
            info.append("Block ").append(i).append(" [").append(block.getBlockType()).append("]\n");
            for(InstructionLine line : block.getInstructions())
            {
                info.append(line.getInfo()).append("\n");
            }
        }
        return info.toString();
    }

    public List<String> getClassFieldRefs(String clazz)
    {
        List<String> pool = new ArrayList<>();
        for(CodeBlock block : body)
        {
            if(!block.getBlockType().equals(BlockType.FIELD_STORE))
                continue;
            for(InstructionLine line : block.getInstructions())
            {
                if(!line.hasOpcode(Opcode.PUTSTATIC,Opcode.PUTFIELD))
                    continue;

                FieldLine fieldLine = line.transpose();
                if(!fieldLine.getClazz().equals(clazz))
                    continue;
                pool.add(fieldLine.getName());
            }
        }
        return pool;
    }

    public int countOpcodesOf(int... opcode)
    {
        int count = 0;
        for(CodeBlock block : getBody())
        {
            count += block.count(opcode);
        }
        return count;
    }
}