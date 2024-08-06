package osrs.dev.modder.model.ast.instructions;

import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Mnemonic;
import osrs.dev.modder.model.ast.enums.LineType;

public class StackLine extends InstructionLine {
    public StackLine(CodeIterator iterator, ConstPool constPool, int pos, int length) {
        super(iterator, iterator.byteAt(pos), Mnemonic.OPCODE[iterator.byteAt(pos)], LineType.LDC, pos, constPool, length);
    }
}