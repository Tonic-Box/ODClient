package osrs.dev.util.eventbus.events;

import lombok.Data;

@Data
public class MenuOptionClicked
{
    private final int param0;
    private final int param1;
    private final int opcode;
    private final int identifier;
    private final int itemId;
    private final int worldViewId;
    private final String option;
    private final String target;
    private final int canvasX;
    private final int canvasY;
}
