package osrs.dev.ui;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;

@Getter
public class ToggleButton extends JToggleButton
{
    @Setter
    private boolean flat = false;

    @Override
    public void paintComponent(Graphics g)
    {
        Color bg;

        if (isSelected()){
            bg = Color.decode("#080b0c");
        } else {
            bg = flat ? new Color(1f,0f,0f,0f ) : Color.darkGray;
        }
        setBackground(bg);
        super.paintComponent(g);
    }
}