package osrs.dev.modder.model.ast.instructions;

import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;
import lombok.Getter;
import osrs.dev.modder.model.ast.enums.LineType;

import java.util.List;

@Getter
public class JumpLine extends InstructionLine {
    public JumpLine(CodeIterator iterator, ConstPool constPool, int pos, int length)
    {
        super(iterator, iterator.byteAt(pos), Mnemonic.OPCODE[iterator.byteAt(pos)], LineType.LDC, pos, constPool, length);
        int opcode = iterator.byteAt(pos);

        switch(opcode)
        {
            case Opcode.GOTO:
                jumpPos = pos + iterator.s16bitAt(pos + 1);
                break;
            case Opcode.GOTO_W:
                jumpPos = pos + iterator.s32bitAt(pos + 1);
                break;
            default:
                jumpPos = -1;
        }
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