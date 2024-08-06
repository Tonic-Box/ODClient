package osrs.dev.modder.model.ast.instructions;

import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;
import lombok.Getter;
import osrs.dev.modder.model.ast.enums.LineType;

@Getter
public class InitLine extends InstructionLine {
    public InitLine(CodeIterator iterator, ConstPool constPool, int pos, int length) {
        super(iterator, iterator.byteAt(pos), Mnemonic.OPCODE[iterator.byteAt(pos)], LineType.FIELD, pos, constPool, length);
        if(iterator.byteAt(pos) == Opcode.NEWARRAY)
            info = arrayInfo(iterator.byteAt(pos + 1));
        else
            info = constPool.getClassInfo(iterator.u16bitAt(pos + 1));
    }

    private final String info;

    private static String arrayInfo(int type) {
        switch (type) {
            case Opcode.T_BOOLEAN:
                return "boolean";
            case Opcode.T_CHAR:
                return "char";
            case Opcode.T_BYTE:
                return "byte";
            case Opcode.T_SHORT:
                return "short";
            case Opcode.T_INT:
                return "int";
            case Opcode.T_LONG:
                return "long";
            case Opcode.T_FLOAT:
                return "float";
            case Opcode.T_DOUBLE:
                return "double";
            default:
                throw new RuntimeException("Invalid array type");
        }
    }
}