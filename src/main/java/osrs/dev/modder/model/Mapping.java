package osrs.dev.modder.model;

import lombok.Data;

@Data
public class Mapping
{
    private final String name;
    private final String obfuscatedName;
    private final String obfuscatedClass;
    private final String descriptor;
    private final int modifiers;
    private final MappedType type;
}
