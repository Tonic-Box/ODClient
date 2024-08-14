package osrs.dev.modder.model.javassist;

import javassist.bytecode.Opcode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import osrs.dev.modder.model.javassist.enums.BlockType;
import osrs.dev.modder.model.javassist.instructions.FieldLine;
import osrs.dev.modder.model.javassist.instructions.InstructionLine;
import osrs.dev.modder.model.javassist.instructions.MethodLine;
import osrs.dev.modder.model.javassist.instructions.ValueLine;
import osrs.dev.util.ArrayUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@AllArgsConstructor
@Getter
public class CodeBlock {
    private final List<InstructionLine> instructions;
    private final BlockType blockType;

    public boolean contains(int... opcode)
    {
        for(InstructionLine line : instructions)
        {
            if(ArrayUtil.contains(opcode, line.getOpcode()))
            {
                return true;
            }
        }
        return false;
    }

    public boolean containsAll(int... opcode)
    {
        for(int op : opcode)
        {
            if(!contains(op))
            {
                return false;
            }
        }
        return true;
    }

    public boolean hasMethodCall(String methodClassName, String methodName, String descriptor)
    {
        for(InstructionLine line : getInstructions())
        {
            if(!line.hasOpcode(Opcode.INVOKEVIRTUAL, Opcode.INVOKESTATIC, Opcode.INVOKESPECIAL, Opcode.INVOKEINTERFACE, Opcode.INVOKEDYNAMIC))
                continue;

            MethodLine methodLine = line.transpose();
//            System.out.println();
//            System.out.println("Class: " + methodLine.getClazz() + " : " + methodClassName);
//            System.out.println("name: " + methodLine.getName() + " : " + methodName);
//            System.out.println("descriptor: " + methodLine.getType() + " : " + descriptor);
            if(!methodLine.getName().equalsIgnoreCase(methodName) || !methodLine.getClazz().equalsIgnoreCase(methodClassName) || !methodLine.getType().equalsIgnoreCase(descriptor))
                continue;

            return true;
        }
        return false;
    }

    public <T extends Number> boolean containsValue(T value) {
        for (InstructionLine line : getInstructions()) {
            if (!(line instanceof ValueLine))
                continue;

            ValueLine valueLine = (ValueLine) line;
            if (!value.getClass().isInstance(valueLine.getValue()))
                continue;

            Number n = valueLine.getValue();
            if (!value.equals(n))
                continue;
            return true;
        }
        return false;
    }

    public boolean containsValue(String value) {
        for (InstructionLine line : getInstructions()) {
            if (line instanceof ValueLine)
            {
                ValueLine valueLine = (ValueLine) line;
                if (!value.getClass().isInstance(valueLine.getValue()))
                    continue;

                String n = valueLine.getValue();
                if (!value.equals(n))
                    continue;
                return true;
            }
            else if (line instanceof FieldLine)
            {
                FieldLine fieldLine = line.transpose();

                String n = fieldLine.getInitialValue();
                if (!value.equals(n))
                    continue;
                return true;
            }
        }
        return false;
    }

    public int count(int... opcode)
    {
        int count = 0;
        for(InstructionLine line : instructions)
        {
            if(ArrayUtil.contains(opcode, line.getOpcode()))
            {
                count++;
            }
        }
        return count;
    }

    public <T extends InstructionLine> T findFirst(Predicate<InstructionLine> predicate)
    {
        return (T) instructions.stream().filter(predicate).findFirst().orElse(null);
    }

    @Override
    public String toString()
    {
        StringBuilder out = new StringBuilder("Block [" + blockType.name() + "]\t");
        for(InstructionLine line : instructions)
        {
            out.append("\t").append(line.getInfo()).append("\n");
        }
        return out.toString();
    }
}