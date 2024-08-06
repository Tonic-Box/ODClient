package osrs.dev.modder.model.ast.instructions;

import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;
import lombok.Getter;
import osrs.dev.modder.model.ast.enums.LineType;

@Getter
public class LocalLine extends InstructionLine {
    public LocalLine(CodeIterator iterator, ConstPool constPool, int pos, int length) {
        super(iterator, iterator.byteAt(pos), Mnemonic.OPCODE[iterator.byteAt(pos)], LineType.FIELD, pos, constPool, length);
        int opcode = iterator.byteAt(pos);
        switch (opcode)
        {
            case Opcode.ILOAD_0:
            case Opcode.LLOAD_0:
            case Opcode.FLOAD_0:
            case Opcode.DLOAD_0:
            case Opcode.ALOAD_0:
            case Opcode.ISTORE_0:
            case Opcode.LSTORE_0:
            case Opcode.FSTORE_0:
            case Opcode.DSTORE_0:
                index = 0;
                break;
            case Opcode.ILOAD_1:
            case Opcode.LLOAD_1:
            case Opcode.FLOAD_1:
            case Opcode.DLOAD_1:
            case Opcode.ALOAD_1:
            case Opcode.ISTORE_1:
            case Opcode.LSTORE_1:
            case Opcode.FSTORE_1:
            case Opcode.DSTORE_1:
            case Opcode.ASTORE_1:
                index = 1;
                break;
            case Opcode.ILOAD_2:
            case Opcode.LLOAD_2:
            case Opcode.FLOAD_2:
            case Opcode.DLOAD_2:
            case Opcode.ALOAD_2:
            case Opcode.ISTORE_2:
            case Opcode.LSTORE_2:
            case Opcode.FSTORE_2:
            case Opcode.DSTORE_2:
            case Opcode.ASTORE_2:
                index = 2;
                break;
            case Opcode.ILOAD_3:
            case Opcode.LLOAD_3:
            case Opcode.FLOAD_3:
            case Opcode.DLOAD_3:
            case Opcode.ALOAD_3:
            case Opcode.ISTORE_3:
            case Opcode.LSTORE_3:
            case Opcode.FSTORE_3:
            case Opcode.DSTORE_3:
            case Opcode.ASTORE_3:
                index = 3;
                break;
            default:
                index = iterator.byteAt(pos + 1);
        }
    }

    private final int index;
}