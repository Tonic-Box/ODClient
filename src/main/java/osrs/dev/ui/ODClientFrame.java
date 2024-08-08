package osrs.dev.ui;

import osrs.dev.Main;
import osrs.dev.annotations.Subscribe;
import osrs.dev.api.RSClient;
import osrs.dev.client.Loader;
import osrs.dev.util.ClientManager;
import osrs.dev.util.ImageUtil;
import osrs.dev.util.Logger;
import osrs.dev.util.ThreadPool;
import osrs.dev.util.eventbus.EventBus;
import osrs.dev.util.eventbus.events.GameTick;
import osrs.dev.util.eventbus.events.MenuOptionClicked;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class ODClientFrame extends JFrame {

    private final JTabbedPane tabbedPane;
    private boolean logMenuActions = false;

    public ODClientFrame() {
        setTitle("[ODClient] An Example OSRS Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 750);
        setLayout(new BorderLayout());

        EventBus.register(this);

        BufferedImage icon = ImageUtil.loadImageResource(ODClientFrame.class, "pixal_bot.png");
        setIconImage(icon);

        buildMenuBar();

        tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(new TabChangeListener());
        add(tabbedPane, BorderLayout.CENTER);

        JTextPane loggerPane = makeLoggerArea();
        Logger.setInstance(loggerPane);
        JScrollPane scroll = new JScrollPane(loggerPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setAutoscrolls(true);
        scroll.setPreferredSize(new Dimension(800, 150));
        scroll.setMinimumSize(new Dimension(800, 150));
        scroll.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        add(scroll, BorderLayout.SOUTH);

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

        JMenu loggingMenu = new JMenu("Logging");
        JCheckBoxMenuItem menuActionsCheckbox = new JCheckBoxMenuItem("MenuActions");
        menuActionsCheckbox.setSelected(false);
        menuActionsCheckbox.addActionListener(e -> logMenuActions = menuActionsCheckbox.isSelected());
        loggingMenu.add(menuActionsCheckbox);
        menuBar.add(loggingMenu);

        setJMenuBar(menuBar);
    }

    private JTextPane makeLoggerArea()
    {
        JTextPane loggerPanel = new JTextPane() {{
            setBackground(Color.BLACK);
            setForeground(Color.LIGHT_GRAY);
            setAutoscrolls(true);
            setEditable(false);
            setFont(new Font("Monoid", Font.PLAIN, 14));
        }};
        DefaultCaret caret = (DefaultCaret)loggerPanel.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        loggerPanel.setPreferredSize(new Dimension(800, 150));
        loggerPanel.setMinimumSize(new Dimension(800, 150));
        return loggerPanel;
    }

    private void addNewTab(String title, Loader loader) {

        ClientContainer panel = new ClientContainer(loader);

        JLabel closeButton = new JLabel("x");
        closeButton.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.setForeground(Color.BLACK);
        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = tabbedPane.indexOfComponent(panel);
                if (index != -1) {
                    ClientContainer container = (ClientContainer) tabbedPane.getComponentAt(index);
                    ThreadPool.submit(container::shutdown);
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

    private static class TabChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
            int index = sourceTabbedPane.getSelectedIndex();
            if (index != -1) {
                Component selectedComponent = sourceTabbedPane.getComponentAt(index);
                if (selectedComponent instanceof ClientContainer) {
                    ClientContainer clientContainer = (ClientContainer) selectedComponent;
                    // Perform the desired action with clientContainer
                    handleClientContainer(clientContainer);
                }
            }
            else
            {
                ClientManager.setCurrentClient(null);
                Logger.info("Swapped to: null");
            }
        }

        private void handleClientContainer(ClientContainer clientContainer)
        {
            clientContainer.swapToFront();
        }
    }

    @Subscribe
    public void onMenuOptionClicked(RSClient client, MenuOptionClicked event)
    {
        if(!logMenuActions)
            return;
        Logger.info("[" + client.getClientID() + "] target=" + event.getTarget() + ", option=" + event.getOption());
    }

    @Subscribe
    public void onGameTick(RSClient client, GameTick event)
    {
        Logger.info("[" + client.getClientID() + "] tick=" + event.getCount());
    }
}
