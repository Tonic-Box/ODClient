package osrs.dev.mixins;

import osrs.dev.annotations.mixin.MethodHook;
import osrs.dev.annotations.mixin.Mixin;
import osrs.dev.annotations.mixin.Shadow;
import osrs.dev.api.RSClient;
import osrs.dev.util.eventbus.EventBus;
import osrs.dev.util.eventbus.events.MenuOptionClicked;

@Mixin("menuAction")
public abstract class RSDoActionMixin
{
    @Shadow("clientField")
    public static RSClient getClient()
    {
        return null;
    }
    @MethodHook("menuAction")
    public static boolean doAction(int param0, int param1, int opcode, int identifier, int itemId, int worldViewId, String option, String target, int canvasX, int canvasY)
    {
        MenuOptionClicked event = new MenuOptionClicked(param0, param1, opcode, identifier, itemId, worldViewId, option, target, canvasX, canvasY);
        EventBus.post(getClient(), event);
        return false;
    }
}
