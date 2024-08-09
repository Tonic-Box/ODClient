package osrs.dev.mixins;

import osrs.dev.annotations.mixin.MethodHook;
import osrs.dev.annotations.mixin.Mixin;

@Mixin("Client")
public class RSRandomDatMixin
{
    @MethodHook("readFromRandomDat")
    public static boolean disableRead()
    {
        System.out.println("Disabled read!");
        return true;
    }

    @MethodHook("writeToRandomDat")
    public static boolean disableWrite()
    {
        System.out.println("Disabled write!");
        return true;
    }
}
