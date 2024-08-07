package osrs.dev.modder.model.javassist.instructions;

import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;
import lombok.Getter;
import osrs.dev.modder.model.javassist.enums.LineType;

@Getter
public class ArithmeticLine extends InstructionLine {
    public ArithmeticLine(CodeIterator iterator, ConstPool constPool, int pos, int length) {
        super(iterator, iterator.byteAt(pos), Mnemonic.OPCODE[iterator.byteAt(pos)], LineType.LDC, pos, constPool, length);
        operator = getOperatorFromOpcode(getOpcode());
    }

    private final String operator;

    private static String getOperatorFromOpcode(int opcode) {
        switch (opcode) {
            case Opcode.IADD:
            case Opcode.LADD:
            case Opcode.FADD:
            case Opcode.DADD:
                return "+";
            case Opcode.ISUB:
            case Opcode.LSUB:
            case Opcode.FSUB:
            case Opcode.DSUB:
                return "-";
            case Opcode.IMUL:
            case Opcode.LMUL:
            case Opcode.FMUL:
            case Opcode.DMUL:
                return "*";
            case Opcode.IDIV:
            case Opcode.LDIV:
            case Opcode.FDIV:
            case Opcode.DDIV:
                return "/";
            case Opcode.IREM:
            case Opcode.LREM:
            case Opcode.FREM:
            case Opcode.DREM:
                return "%";
            case Opcode.ISHL:
            case Opcode.LSHL:
                return "<<";
            case Opcode.ISHR:
            case Opcode.LSHR:
                return ">>";
            case Opcode.IUSHR:
            case Opcode.LUSHR:
                return ">>>";
            case Opcode.IAND:
            case Opcode.LAND:
                return "&";
            case Opcode.IOR:
            case Opcode.LOR:
                return "|";
            case Opcode.IXOR:
            case Opcode.LXOR:
                return "^";
            default:
                return "";
        }
    }
}