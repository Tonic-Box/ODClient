package osrs.dev.modder.model.ast.instructions;

import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Mnemonic;
import lombok.Getter;
import lombok.Setter;
import osrs.dev.modder.model.ast.enums.LineType;

@Getter
public class MethodLine extends InstructionLine {
    public MethodLine(CodeIterator iterator, ConstPool constPool, int pos, int length) {
        super(iterator, iterator.byteAt(pos), Mnemonic.OPCODE[iterator.byteAt(pos)], LineType.METHOD, pos, constPool, length);
        int ref = iterator.u16bitAt(pos + 1);
        type = constPool.getMethodrefType(ref);
        clazz = constPool.getMethodrefClassName(ref);
        name = constPool.getMethodrefName(ref);
        Static = Mnemonic.OPCODE[iterator.byteAt(pos)].contains("STATIC");
    }
    private final String name;
    private final String clazz;
    private final String type;
    private final boolean Static;
    @Setter
    private Number garbage = null;
    @Setter
    private String garbageType = null;
}