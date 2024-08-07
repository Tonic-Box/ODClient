package osrs.dev.modder.model.javassist.instructions;

import javassist.CtClass;
import javassist.CtField;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Mnemonic;
import lombok.Getter;
import lombok.Setter;
import osrs.dev.modder.model.Mapping;
import osrs.dev.modder.model.Mappings;
import osrs.dev.modder.model.javassist.enums.LineType;
import osrs.dev.util.modding.CodeUtil;

@Getter
public class FieldLine extends InstructionLine {
    private static boolean scanningInit = false;
    public FieldLine(CodeIterator iterator, ConstPool constPool, int pos, int length) {
        super(iterator, iterator.byteAt(pos), Mnemonic.OPCODE[iterator.byteAt(pos)], LineType.FIELD, pos, constPool, length);
        int ref = iterator.u16bitAt(pos + 1);
        type = constPool.getFieldrefType(ref);
        clazz = constPool.getFieldrefClassName(ref);
        name = constPool.getFieldrefName(ref);
        Static = Mnemonic.OPCODE[iterator.byteAt(pos)].toLowerCase().contains("static");

        try
        {
            //to prevent SOF
            if(scanningInit)
                return;
            scanningInit = true;
            initialValue = CodeUtil.inspectFieldInit(isStatic(), clazz, name) + "";
            scanningInit = false;
        }
        catch(Exception ex)
        {
            scanningInit = false;
        }
    }

    /**
     * checks if a supplies class is a super of the referenced
     * class and contains the field in question.
     * @param spr super
     * @return boolean
     */
    public boolean fromSuper(String spr)
    {
        try
        {
            CtClass aClass = Mappings.getClazz(name);
            try
            {
                if(aClass.getDeclaredField(name) != null)
                    return false;
            }
            catch (Exception ignored) {}
            while(true)
            {
                if(aClass.getSuperclass().getName().equals("java.lang.Object"))
                    return false;
                else if(aClass.getSuperclass().getName().equals(spr))
                {
                    try
                    {
                        return aClass.getSuperclass().getDeclaredField(name) != null;
                    }
                    catch (Exception ignored) {}
                }
                aClass = aClass.getSuperclass();
            }
        }
        catch (Exception ignored)
        {
            return false;
        }
    }

    public boolean refTo(CtField field)
    {
        if(field == null)
            return false;
        return name.equals(field.getName()) && clazz.equals(field.getDeclaringClass().getName());
    }

    public boolean refTo(Mapping field)
    {
        if(field == null)
            return false;
        return name.equals(field.getObfuscatedName()) && clazz.equals(field.getObfuscatedClass());
    }

    private final String name;
    private final String clazz;
    private final String type;
    private final boolean Static;
    private String initialValue = null;
    @Setter
    private Number garbageSetter = null;
    @Setter
    private Number garbageGetter = null;
}