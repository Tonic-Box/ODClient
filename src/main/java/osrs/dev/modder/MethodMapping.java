package osrs.dev.modder;

import lombok.Data;

@Data
public class MethodMapping
{
    private final String name;
    private final String obfuscatedName;
    private final String obfuscatedClass;
    private final String signature;
    private final Number garbage;
}
