package osrs.dev.modder.model;

import javassist.CtClass;
import javassist.CtMethod;
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
}
