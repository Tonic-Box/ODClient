package osrs.dev.ui;

import osrs.dev.Main;
import osrs.dev.client.Loader;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ODClientFrame extends JFrame {

    private final JTabbedPane tabbedPane;

    public ODClientFrame() {
        setTitle("[ODClient] An Example OSRS Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        buildMenuBar();

        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private void buildMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        JLabel addTabButton = new JLabel("Add Tab");
        addTabButton.setForeground(Color.LIGHT_GRAY);
        addTabButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        addTabButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addTabButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                addNewTab("Tab " + (tabbedPane.getTabCount() + 1), Main.getLoader());
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                addTabButton.setForeground(Color.GREEN);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                addTabButton.setForeground(Color.LIGHT_GRAY);
            }
        });
        menuBar.add(addTabButton);
        setJMenuBar(menuBar);
    }

    private void addNewTab(String title, Loader loader) {
        JPanel panel = new JPanel(new BorderLayout());
        Applet applet = loader.getApplet();
        loader.run();
        panel.add(applet, BorderLayout.CENTER);

        JLabel closeButton = new JLabel("x");
        closeButton.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.setForeground(Color.BLACK);
        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = tabbedPane.indexOfComponent(panel);
                if (index != -1) {
                    tabbedPane.remove(index);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setForeground(Color.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setForeground(Color.BLACK);
            }
        });

        JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabHeader.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        tabHeader.add(titleLabel);
        tabHeader.add(closeButton);

        tabbedPane.addTab(title, panel);
        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, tabHeader);
    }
}
