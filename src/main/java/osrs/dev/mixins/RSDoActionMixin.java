package osrs.dev.mixins;

import osrs.dev.annotations.MethodHook;
import osrs.dev.annotations.Mixin;
import osrs.dev.annotations.Shadow;
import osrs.dev.api.RSClient;
import osrs.dev.util.Logger;
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
//        StringBuilder log = new StringBuilder();
//        log.append("[MA] param0=")
//                .append(param0)
//                .append(", param1=")
//                .append(param1)
//                .append(", opcode=")
//                .append(opcode)
//
//                .append(", identifier=")
//                .append(identifier)
//                .append(", itemId=")
//                .append(itemId)
//                .append(", worldViewId=")
//                .append(worldViewId)
//                .append(", option=")
//                .append(option)
//                .append(", target=")
//                .append(target)
//                .append(", canvasX=")
//                .append(canvasX)
//                .append(", canvasY=")
//                .append(canvasY);
        MenuOptionClicked event = new MenuOptionClicked(param0, param1, opcode, identifier, itemId, worldViewId, option, target, canvasX, canvasY);

        EventBus.post(getClient(), event);
        System.out.println("posted!");
        return false;
    }
}
