package osrs.dev.modder.model;

import javassist.CtClass;
import javassist.CtMethod;
import lombok.Getter;

import java.util.*;

/**
 * our in-memory manager for gamepack mappings
 */
public class Mappings
{
    @Getter
    private static final List<CtClass> classes = new ArrayList<>();
    @Getter
    private static final Set<String> usedMethods = new HashSet<>();
    @Getter
    private static final List<Mapping> mappings = new ArrayList<>();

    public static void addField(String name, String obfuscatedName, String obfuscatedClass, String descriptor)
    {
        System.out.println("Found Field: " + name + " : <" + descriptor + "> " + obfuscatedClass + "." + obfuscatedName);
        mappings.add(new Mapping(name, obfuscatedName, obfuscatedClass, descriptor, MappedType.FIELD));
    }

    public static void addMethod(String name, String obfuscatedName, String obfuscatedClass, String descriptor)
    {
        System.out.println("Found Method: " + name + " : " + obfuscatedClass + "." + obfuscatedName + descriptor);
        mappings.add(new Mapping(name, obfuscatedName, obfuscatedClass, descriptor, MappedType.METHOD));
    }

    public static void addMethodNoGarbage(String name, String obfuscatedName, String obfuscatedClass, String descriptor)
    {
        System.out.println("Found Method: " + name + " : " + obfuscatedClass + "." + obfuscatedName + descriptor);
        Mapping mapping = new Mapping(name, obfuscatedName, obfuscatedClass, descriptor, MappedType.METHOD);
        mapping.setDone(true);
        mappings.add(mapping);
    }

    public static void addClass(String name, String obfuscatedName)
    {
        System.out.println("Found class: " + name);
        mappings.add(new Mapping(name, obfuscatedName, obfuscatedName, "", MappedType.CLASS));
    }

    /**
     * find a Mapping by our name we give it
     * @param name name
     * @return Mapping
     */
    public static Mapping findByTag(String name)
    {
        return mappings.stream()
                .filter(m -> m.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * find a class by our name we give it, if not found it will look for a class directly matching the supplied name.
     * @param name name
     * @return CtClass
     */
    public static CtClass getClazz(String name)
    {
        Mapping mapping = findByTag(name);
        if(mapping == null)
        {
            return getClasses().stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
        }
        return getClasses().stream().filter(c -> c.getName().equals(mapping.getObfuscatedClass())).findFirst().orElse(null);
    }
}
