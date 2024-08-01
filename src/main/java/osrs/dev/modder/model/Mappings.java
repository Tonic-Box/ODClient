package osrs.dev.modder.model;

import javassist.CtClass;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Mappings
{
    @Getter
    private static final List<CtClass> classes = new ArrayList<>();
    @Getter
    private static final List<Mapping> mappings = new ArrayList<>();

    public static void addField(String name, String obfuscatedName, String obfuscatedClass, String descriptor, int modifiers)
    {
        mappings.add(new Mapping(name, obfuscatedName, obfuscatedClass, descriptor, modifiers, MappedType.FIELD));
    }

    public static void addMethod(String name, String obfuscatedName, String obfuscatedClass, String descriptor, int modifiers)
    {
        mappings.add(new Mapping(name, obfuscatedName, obfuscatedClass, descriptor, modifiers, MappedType.METHOD));
    }

    public static void addClass(String name, String obfuscatedName)
    {
        mappings.add(new Mapping(name, obfuscatedName, obfuscatedName, "", -1, MappedType.CLASS));
    }

    public static Mapping findByTag(String name)
    {
        return mappings.stream()
                .filter(m -> m.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public static CtClass getClazz(String name)
    {
        Mapping mapping = findByTag(name);
        return getClasses().stream().filter(c -> c.getName().equals(mapping.getObfuscatedClass())).findFirst().orElse(null);
    }
}
