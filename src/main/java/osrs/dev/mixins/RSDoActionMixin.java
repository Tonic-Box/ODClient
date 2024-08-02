package osrs.dev.mixins;

import osrs.dev.annotations.MethodHook;
import osrs.dev.annotations.Mixin;
import osrs.dev.util.Logger;

@Mixin("menuAction")
public class RSDoActionMixin
{
    @MethodHook("menuAction")
    public static boolean doAction(int param0, int param1, int opcode, int identifier, int itemId, int worldViewId, String option, String target, int canvasX, int canvasY)
    {
        StringBuilder log = new StringBuilder();
        log.append("[MA] param0=")
                .append(param0)
                .append(", param1=")
                .append(param1)
                .append(", opcode=")
                .append(opcode)

                .append(", identifier=")
                .append(identifier)
                .append(", itemId=")
                .append(itemId)
                .append(", worldViewId=")
                .append(worldViewId)
                .append(", option=")
                .append(option)
                .append(", target=")
                .append(target)
                .append(", canvasX=")
                .append(canvasX)
                .append(", canvasY=")
                .append(canvasY);

        System.out.println(log);
        Logger.info(log.toString());
        return false;
    }
}
