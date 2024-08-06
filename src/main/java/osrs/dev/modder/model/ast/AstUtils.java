package osrs.dev.modder.model.ast;

import javassist.bytecode.*;
import osrs.dev.modder.model.ast.enums.BlockType;
import osrs.dev.modder.model.ast.instructions.*;
import osrs.dev.util.CircularList;
import java.util.ArrayList;
import java.util.List;

public class AstUtils
{
    public static List<InstructionLine> cleanControlFlow(List<InstructionLine> lines) {
        List<InstructionLine> cleaned = new ArrayList<>();
        int i = 0;
        while (i < lines.size())
        {
            InstructionLine line = lines.get(i);
            lines.remove(i);
            if (line.hasOpcode(Opcode.GOTO, Opcode.GOTO_W)) {
                JumpLine jump = (JumpLine) line;
                int targetIndex = jump.getJumpPos();
                i = getIndexFromJumpPos(targetIndex, lines);
            }
            else
            {
                cleaned.add(line);
            }
        }

        return fixPositions(cleaned);
    }

    private static int getIndexFromJumpPos(int jumpPos, List<InstructionLine> lines) {
        for (int i = 0; i < lines.size(); i++) {
            InstructionLine line = lines.get(i);
            if(line.getPosition() == jumpPos) {
                return i;
            }
        }

        int score = Integer.MAX_VALUE;
        int index = -1;

        for (int i = 0; i < lines.size(); i++) {
            InstructionLine line = lines.get(i);
            int diff = Math.abs(line.getPosition() - jumpPos);
            if (diff < score) {
                score = diff;
                index = i;
            }
        }

        return index;
    }

    public static List<CodeBlock> getCodeBlocks(List<InstructionLine> lines) {
        List<CodeBlock> controlFlow = new ArrayList<>();
        List<InstructionLine> instructions = new ArrayList<>();
        int lastOpcode = -1;
        for(InstructionLine line : lines)
        {
            instructions.add(line);
            BlockType type = Iterator.isEndOfLine(line, lastOpcode);
            if(type != null)
            {
                controlFlow.add(new CodeBlock(new ArrayList<>(instructions), type));
                instructions.clear();
                lastOpcode = -1;
                continue;
            }
            lastOpcode = line.getOpcode();
        }
        if(!instructions.isEmpty())
        {
            controlFlow.add(new CodeBlock(instructions, BlockType.UNKNOWN));
        }

        return controlFlow;
    }

    private static List<InstructionLine> fixPositions(List<InstructionLine> lines)
    {
        int pos = 0;
        for(InstructionLine line : lines)
        {
            line.setPosition(pos);
            pos += line.getLength();
        }
        return lines;
    }

    public static List<InstructionLine> smooshStrings(List<InstructionLine> lines)
    {
        List<InstructionLine> smooshed = new ArrayList<>();
        boolean inBuilder = false;
        StringBuilder buffer = new StringBuilder();
        ValueLine tempy = null;
        int length = 0;
        int i = -1;
        for(InstructionLine line : lines)
        {
            i++;
            if(line instanceof InitLine)
            {
                InitLine initLine = (InitLine) line;
                inBuilder = initLine
                        .getInfo()
                        .equals("java.lang.StringBuilder");
                continue;
            }

            if(!inBuilder)
            {
                smooshed.add(line);
                continue;
            }

            if(!(line instanceof ValueLine) && !(line instanceof ArithmeticLine) &&  !(line instanceof StackLine) && !line.hasOpcode(Opcode.INVOKEVIRTUAL,Opcode.INVOKESPECIAL))
            {
                inBuilder = false;
                if(tempy != null)
                {
                    smooshed.add(new ValueLine(tempy, length, buffer.toString()));
                }
                tempy = null;
                length = 0;
                buffer = new StringBuilder();
                smooshed.add(line);
                continue;
            }

            length += line.getLength();

            if(line instanceof ValueLine)
            {
                ValueLine valueLine = (ValueLine) line;
                if(tempy == null)
                {
                    tempy = valueLine;
                }
                InstructionLine nextLine = lines.get(i + 1);
                if(nextLine.hasOpcode(Opcode.INVOKEVIRTUAL))
                {
                    MethodLine methodLine = (MethodLine) nextLine;
                    if(methodLine.getType().equals("(C)Ljava/lang/StringBuilder;"))
                    {
                        int c = valueLine.getValue();
                        buffer.append((char)c);
                        continue;
                    }
                }
                length += valueLine.getLength();
                String val = valueLine.getValue() + "";
                buffer.append(val);
            }
            else if(line instanceof ArithmeticLine)
            {
                ArithmeticLine arithmeticLine = (ArithmeticLine) line;
                buffer.append("[").append(arithmeticLine.getOperator()).append("]");
            }
        }
        return smooshed;
    }

    public static List<CodeBlock> combineGarbs(List<CodeBlock> blocks)
    {
        List<ValueLine> values = new ArrayList<>();
        List<InstructionLine> removals = new ArrayList<>();
        InstructionLine multiply = null;
        CircularList<InstructionLine> circularList = new CircularList<>(4);
        for (CodeBlock block : blocks)
        {
            //field setter garbs
            values.clear();
            for(int i = 0; i < block.getInstructions().size(); i++)
            {
                InstructionLine line = block.getInstructions().get(i);

                if(line.hasOpcode(Opcode.IMUL,Opcode.LMUL))
                {
                    multiply = line;
                }

                if(line.hasOpcode(Opcode.LDC, Opcode.LDC_W, Opcode.LDC2_W) && line instanceof ValueLine)
                {
                    ValueLine valueLine = (ValueLine) line;
                    if(valueLine.getValue() instanceof String)
                        continue;
                    values.add(valueLine);
                    continue;
                }

                if(!line.hasOpcode(Opcode.PUTFIELD, Opcode.PUTSTATIC))
                    continue;

                if(i >= 2 && block.getInstructions().get(i-2).getOpcode() == Opcode.ALOAD_0)
                    continue;

                if(values.isEmpty())
                    continue;

                FieldLine fieldLine = (FieldLine) line;

                if(!fieldLine.getType().equals("I") && !fieldLine.getType().equals("J"))
                    continue;

                ValueLine valueLine = values.get(values.size() - 1);
                Number value = valueLine.getValue();
                fieldLine.setGarbageSetter(value);
                block.getInstructions().remove(valueLine);
                try
                {
                    //Todo: figure this shit out
                    InstructionLine aload = block.getInstructions().get(i-2);
                    block.getInstructions().remove(aload);
                }
                catch (Exception ignored) {}
                if(multiply != null)
                    block.getInstructions().remove(multiply);
                break;
            }

            int last = -1;
            values.clear();
            for(InstructionLine line : block.getInstructions())
            {
                if(line.hasOpcode(Opcode.ALOAD_0) && last == Opcode.ALOAD_0)
                {
                    removals.add(line);
                    last = -1;
                    continue;
                }
                last = line.getOpcode();
            }


            //method garbs
            values.clear();
            for(int i = 0; i < block.getInstructions().size(); i++)
            {
                InstructionLine line = block.getInstructions().get(i);

                if(line.hasOpcode(Opcode.LDC, Opcode.LDC_W, Opcode.LDC2_W) && line instanceof ValueLine)
                {
                    values.add((ValueLine) line);
                    continue;
                }

                if(!line.hasOpcode(Opcode.INVOKESTATIC,Opcode.INVOKEVIRTUAL))
                    continue;

                if(values.isEmpty())
                    break;

                MethodLine methodLine = (MethodLine) line;
                ValueLine valueLine = values.get(values.size() - 1);
                Object value = values.get(values.size() - 1).getValue();
                if(value instanceof String)
                    break;

                removals.add(valueLine);

                methodLine.setGarbageType(valueLine.getType());
                methodLine.setGarbage((Number)value);
            }

            //field getter
            values.clear();
            for(int i = 0; i < block.getInstructions().size(); i++) {
                InstructionLine line = block.getInstructions().get(i);
                if(!line.hasOpcode(Opcode.IMUL,Opcode.LMUL))
                {
                    circularList.add(line);
                    continue;
                }

                FieldLine fieldLine = (FieldLine) circularList.getList().stream()
                        .filter(l -> l.hasOpcode(Opcode.GETSTATIC,Opcode.GETFIELD) &&
                                (((FieldLine)l).getType().equals("I") || ((FieldLine)l).getType().equals("J")))
                        .findFirst().orElse(null);
                if(fieldLine == null)
                {
                    circularList.clear();
                    continue;
                }
                ValueLine valueLine = (ValueLine) circularList.getList().stream()
                        .filter(l -> l instanceof ValueLine && !(((ValueLine)l).getValue() instanceof String))
                        .findFirst().orElse(null);

                if(valueLine == null)
                {
                    circularList.clear();
                    continue;
                }

                fieldLine.setGarbageGetter(valueLine.getValue());
                removals.add(valueLine);
                removals.add(line);
                circularList.clear();
            }

            block.getInstructions().removeAll(removals);
            removals.clear();
        }
        return blocks;
    }
}
