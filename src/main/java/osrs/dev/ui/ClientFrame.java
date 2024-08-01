package osrs.dev.ui;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;

public class ClientFrame extends JFrame
{
    public ClientFrame(Applet applet)
    {
        setTitle("ODClient");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(applet, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
