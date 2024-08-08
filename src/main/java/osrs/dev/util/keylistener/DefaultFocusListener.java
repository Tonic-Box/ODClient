package osrs.dev.util.keylistener;

import lombok.Getter;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class DefaultFocusListener extends FocusAdapter {
    @Getter
    private static boolean focussed = false;

    @Override
    public void focusGained(FocusEvent e) {
        super.focusGained(e);
        focussed = true;
    }

    @Override
    public void focusLost(FocusEvent e) {
        super.focusGained(e);
        focussed = false;
    }
}