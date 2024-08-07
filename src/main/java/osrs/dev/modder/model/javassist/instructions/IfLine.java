package osrs.dev.modder.model.javassist.instructions;

import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Mnemonic;
import lombok.Getter;
import osrs.dev.modder.model.javassist.enums.LineType;

import java.util.List;

@Getter
public class IfLine extends InstructionLine {
    public IfLine(CodeIterator iterator, ConstPool constPool, int pos, int length) {
        super(iterator, iterator.byteAt(pos), Mnemonic.OPCODE[iterator.byteAt(pos)], LineType.FIELD, pos, constPool, length);
        jumpPos = pos + iterator.s16bitAt(pos + 1);
    }

    private final int jumpPos;

    public InstructionLine getLocationLine(List<InstructionLine> instructions)
    {
        return instructions.stream().filter(i -> i.getPosition() == jumpPos).findFirst().orElse(null);
    }

    public int getLocationListIndex(List<InstructionLine> instructions)
    {
        for(int i = 0; i < instructions.size(); i++)
        {
            if(instructions.get(i).getPosition() == getJumpPos())
                return i;
        }
        return -1;
    }
}