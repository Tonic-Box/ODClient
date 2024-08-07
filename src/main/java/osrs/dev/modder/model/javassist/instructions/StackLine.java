package osrs.dev.modder.model.javassist.instructions;

import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Mnemonic;
import osrs.dev.modder.model.javassist.enums.LineType;

public class StackLine extends InstructionLine {
    public StackLine(CodeIterator iterator, ConstPool constPool, int pos, int length) {
        super(iterator, iterator.byteAt(pos), Mnemonic.OPCODE[iterator.byteAt(pos)], LineType.LDC, pos, constPool, length);
    }
}