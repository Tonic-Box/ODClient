package osrs.dev.mixins;

import osrs.dev.annotations.Inject;
import osrs.dev.annotations.MethodHook;
import osrs.dev.annotations.Mixin;
import osrs.dev.api.RSClient;

@Mixin("GameEngine")
public abstract class RSGameEngineMixin implements RSClient
{
    @Inject
    private static boolean disableRender = false;

    @Override
    @Inject
    public void setDisableRender(boolean bool)
    {
        if(bool)
        {
            System.out.println("off");
        }
        else
        {
            System.out.println("On");
        }
        disableRender = bool;
    }

    @MethodHook("graphicsTick")
    public static boolean gTickHook()
    {
        return disableRender;
    }
}
