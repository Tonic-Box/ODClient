package osrs.dev.modder.model.ast.instructions;

import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import osrs.dev.modder.model.ast.enums.LineType;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public class InstructionLine {
    private final transient CodeIterator iterator;
    private final int opcode;
    private final String mnemonic;
    private final LineType lineType ;
    @Setter
    private int position;
    private final transient ConstPool constPool;
    @Setter
    private int length;

    public boolean hasOpcode(int... opcodes)
    {
        return Arrays.stream(opcodes).filter(o -> opcode == o).count() != 0;
    }

    public void print()
    {
        System.out.println(getInfo());
    }

    @Override
    public String toString()
    {
        return getInfo();
    }

    public String getInfo()
    {
        String out = "[" + getPosition() + "] " + getMnemonic() + " ";
        if(this instanceof FieldLine)
        {
            FieldLine fieldLine = (FieldLine) this;
            String garb = "";
            if(fieldLine.getGarbageSetter() != null)
            {
                garb = " / GarbageSetter=" + fieldLine.getGarbageSetter().longValue();
            }
            else if(fieldLine.getGarbageGetter() != null)
            {
                garb = " / GarbageGetter=" + fieldLine.getGarbageGetter().longValue();
            }
            out += "<" + fieldLine.getType() + "> " + fieldLine.getClazz() + "." + fieldLine.getName() + garb;
        }
        else if(this instanceof MethodLine)
        {
            MethodLine methodLine = (MethodLine) this;
            String garb = "";
            if(methodLine.getGarbage() != null)
            {
                garb = " / Garbage=" + methodLine.getGarbage().longValue();
            }
            out += "<" + methodLine.getType() + "> " + methodLine.getClazz() + "." + methodLine.getName() + garb;
        }
        else if(this instanceof ValueLine)
        {
            ValueLine valueLine = (ValueLine) this;
            String value = "" + (valueLine.getRawValue() instanceof String ? "\"" + valueLine.getValue() + "\"" : valueLine.getValue());
            out += "<" + valueLine.getType() + "> " + value;
        }
        else if(this instanceof JumpLine)
        {
            JumpLine jumpLine = (JumpLine) this;
            out += jumpLine.getJumpPos();
        }
        else if(this instanceof IfLine)
        {
            IfLine ifLine = (IfLine) this;
            out += ifLine.getJumpPos();
        }
        else if(this instanceof LocalLine)
        {
            LocalLine localLine = (LocalLine) this;
            out += localLine.getIndex();
        }
        else if(this instanceof InitLine)
        {
            InitLine initLine = (InitLine) this;
            out += initLine.getInfo();
        }
        else if(this instanceof ArithmeticLine)
        {
            ArithmeticLine arithmeticLine = (ArithmeticLine) this;
            out += arithmeticLine.getOperator();
        }
        return out;
    }

    public <T> T transpose()
    {
        return (T)this;
    }
}