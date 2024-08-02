package osrs.dev.mixins;

import osrs.dev.annotations.Inject;
import osrs.dev.annotations.MethodHook;
import osrs.dev.annotations.Mixin;
import osrs.dev.api.RSClient;

import java.applet.Applet;

@Mixin("GameEngine")
public abstract class RSGameEngineMixin extends Applet implements RSClient
{
    @Inject
    private static boolean disableRender = false;

    @Inject
    public static boolean shouldExit;

    @Override
    @Inject
    public void setDisableRender(boolean bool)
    {
        disableRender = bool;
    }

    @MethodHook("graphicsTick")
    public boolean gTickHook()
    {
        if(shouldExit)
        {
            this.destroy();
            return false;
        }
        return disableRender;
    }

    @Inject
    @Override
    public void setShouldExit(boolean bool)
    {
        shouldExit = bool;
    }

    @Inject
    @Override
    public boolean getShouldExit()
    {
        return shouldExit;
    }
}
