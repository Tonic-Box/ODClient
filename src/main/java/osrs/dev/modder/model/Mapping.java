package osrs.dev.modder.model;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import lombok.Data;

@Data
public class Mapping
{
    private final String name;
    private final String obfuscatedName;
    private final String obfuscatedClass;
    private final String descriptor;
    private final MappedType type;

    private boolean done = false;
    private GarbageValue garbage = null;
    private boolean fieldHookAfter = false;

    public CtMethod getMethod()
    {
        try
        {
            CtClass clazz = Mappings.getClazz(obfuscatedClass);
            if(clazz == null)
                return null;

            return clazz.getMethod(obfuscatedName, descriptor);
        }
        catch (Exception ignored) {}
        return null;
    }

    /**
     * get the return/fieldtype of a method/field entry
     * @return string name of the type
     */
    public String getDataType()
    {
        CtClass clazz = Mappings.getClasses().stream().filter(c -> c.getName().equals(obfuscatedClass)).findFirst().orElse(null);
        if(clazz == null)
            return "";
        try {
            return clazz.getDeclaredField(obfuscatedName).getType().getName();
        } catch (NotFoundException e) {
            return "";
        }
    }
}
