package osrs.dev.util.eventbus.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class GameTick {
    private int count;
}
