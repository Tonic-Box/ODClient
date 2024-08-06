package osrs.dev.modder.model.ast.instructions;

import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;
import lombok.Getter;
import osrs.dev.modder.model.ast.enums.LineType;

public class ValueLine extends InstructionLine {
    @Getter
    private String type;
    private Object value;
    @Getter
    private Class<?> typeClass;

    public ValueLine(InstructionLine line, int length, Object value) {
        super(line.getIterator(), line.getOpcode(), Mnemonic.OPCODE[line.getOpcode()], LineType.LDC, line.getPosition(), line.getConstPool(), length);
        setLength(length);
        this.value = value;
        this.type = "java.lang.String";
        this.typeClass = String.class;
    }

    public ValueLine(CodeIterator iterator, ConstPool constPool, int pos, int length) {
        super(iterator, iterator.byteAt(pos), Mnemonic.OPCODE[iterator.byteAt(pos)], LineType.LDC, pos, constPool, length);
        int opcode = iterator.byteAt(pos);

        switch (opcode) {
            case Opcode.ICONST_M1:
                setTypeAndValue(int.class, "int", -1);
                return;
            case Opcode.ICONST_0:
                setTypeAndValue(int.class, "int", 0);
                return;
            case Opcode.ICONST_1:
                setTypeAndValue(int.class, "int", 1);
                return;
            case Opcode.ICONST_2:
                setTypeAndValue(int.class, "int", 2);
                return;
            case Opcode.ICONST_3:
                setTypeAndValue(int.class, "int", 3);
                return;
            case Opcode.ICONST_4:
                setTypeAndValue(int.class, "int", 4);
                return;
            case Opcode.ICONST_5:
                setTypeAndValue(int.class, "int", 5);
                return;
            case Opcode.BIPUSH:
                setTypeAndValue(byte.class, "byte", iterator.signedByteAt(pos + 1));
                return;
            case Opcode.SIPUSH:
                setTypeAndValue(short.class, "short", iterator.s16bitAt(pos + 1));
                return;
            case Opcode.LDC:
                int index = iterator.byteAt(pos + 1);
                handleLDC(constPool, index);
                return;
            case Opcode.LDC_W:
            case Opcode.LDC2_W:
                index = iterator.u16bitAt(pos + 1);
                handleLDC(constPool, index);
                return;
            default:
                throw new RuntimeException("Unsupported opcode: " + opcode);
        }
    }

    private void setTypeAndValue(Class<?> typeClass, String type, Object value) {
        this.typeClass = typeClass;
        this.type = type;
        this.value = value;
    }

    private void handleLDC(ConstPool constPool, int index) {
        int tag = constPool.getTag(index);

        switch (tag) {
            case ConstPool.CONST_String:
                setTypeAndValue(String.class, "java.lang.String", constPool.getStringInfo(index));
                break;
            case ConstPool.CONST_Integer:
                setTypeAndValue(int.class, "int", constPool.getIntegerInfo(index));
                break;
            case ConstPool.CONST_Float:
                setTypeAndValue(float.class, "float", constPool.getFloatInfo(index));
                break;
            case ConstPool.CONST_Long:
                setTypeAndValue(long.class, "long", constPool.getLongInfo(index));
                break;
            case ConstPool.CONST_Double:
                setTypeAndValue(double.class, "double", constPool.getDoubleInfo(index));
                break;
            case ConstPool.CONST_Class:
                setTypeAndValue(Class.class, "clazz", constPool.getClassInfo(index));
                break;
            default:
                throw new RuntimeException("bad LDC tag: " + tag);
        }
    }

    public <T> T getValue() {
        return (T) value;
    }

    public Object getRawValue() {
        return value;
    }
}
