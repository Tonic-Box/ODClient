package osrs.dev.ui;

import com.sun.management.OperatingSystemMXBean;
import lombok.Getter;
import osrs.dev.api.RSClient;
import osrs.dev.util.ClientManager;
import osrs.dev.util.ImageUtil;
import osrs.dev.util.Logger;
import osrs.dev.util.keylistener.DefaultFocusListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LoggerPanel extends JPanel {
    private final JTextPane loggerPanel;
    @Getter
    private final JTextPane statsPanel; // Add the stats panel
    private final JPanel controlPane;
    private JTextField input;
    private final OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private final NumberFormat format = NumberFormat.getInstance(Locale.US);
    private final SimpleAttributeSet headStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet bodyStyle = new SimpleAttributeSet();
    private final StyledDocument doc;

    public LoggerPanel() {
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setLayout(new BorderLayout());

        loggerPanel = makeLoggerArea();
        JScrollPane loggerScroll = new JScrollPane(loggerPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        loggerScroll.setAutoscrolls(true);
        loggerScroll.setPreferredSize(new Dimension(800, 160));
        loggerScroll.setMinimumSize(new Dimension(800, 160));
        loggerScroll.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        statsPanel = makeStatsArea(); // Initialize the stats panel
        doc = statsPanel.getStyledDocument();
        startStatsWorkerThread();

        JPanel topBar = new JPanel() {{
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(0, 0, 2, 0));
            setPreferredSize(new Dimension(1000, 34));
            setMaximumSize(new Dimension(1000, 34));
            setMinimumSize(new Dimension(100, 34));
            add(new JPanel().add(new JLabel()), BorderLayout.CENTER);
            add(makeLoggerLevelPane(), BorderLayout.WEST);
        }};

        add(topBar, BorderLayout.NORTH);
        add(loggerScroll, BorderLayout.CENTER);
        add(statsPanel, BorderLayout.EAST); // Add the stats scroll to the east

        Logger.setInstance(loggerPanel);

        controlPane = makeControlPane();
        add(controlPane, BorderLayout.SOUTH);
    }

    private JTextPane makeLoggerArea() {
        JTextPane loggerPanel = new JTextPane() {{
            setBackground(Color.BLACK);
            setForeground(Color.LIGHT_GRAY);
            setAutoscrolls(true);
            setEditable(false);
            setFont(new Font("Monoid", Font.PLAIN, 14));
        }};
        DefaultCaret caret = (DefaultCaret) loggerPanel.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        return loggerPanel;
    }

    private JTextPane makeStatsArea() {
        JTextPane statsPanel = new JTextPane() {{
            setBackground(Color.BLACK);
            setForeground(Color.LIGHT_GRAY);
            setEditable(false);
            setFont(new Font("Monoid", Font.PLAIN, 14));
            setBorder(BorderFactory.createLineBorder(Color.GRAY));
            setPreferredSize(new Dimension(200, 160));
            setMinimumSize(new Dimension(200, 160));
        }};
        return statsPanel;
    }

    private JPanel makeLoggerLevelPane() {
        Dimension size = new Dimension(25, 25);

        ToggleButton info = new ToggleButton() {{
            setToolTipText("Information logger level.");
            setBorderPainted(false);
            setIcon(ImageUtil.sizedIcon(LoggerPanel.class, "info.png", 23));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setSize(size);
            setSelected(true);
            addItemListener(ev -> {
                boolean selected = ev.getStateChange() == ItemEvent.SELECTED;
                Logger.setInfo(selected);
            });
        }};

        ToggleButton normal = new ToggleButton() {{
            setToolTipText("Normal logger level.");
            setBorderPainted(false);
            setIcon(ImageUtil.sizedIcon(LoggerPanel.class, "norm.png", 23));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setSize(size);
            setSelected(true);
            addItemListener(ev -> {
                boolean selected = ev.getStateChange() == ItemEvent.SELECTED;
                Logger.setNormal(selected);
            });
        }};

        ToggleButton warning = new ToggleButton() {{
            setToolTipText("Warning logger level.");
            setBorderPainted(false);
            setIcon(ImageUtil.sizedIcon(LoggerPanel.class, "warn.png", 23));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setSize(size);
            setSelected(true);
            addItemListener(ev -> {
                boolean selected = ev.getStateChange() == ItemEvent.SELECTED;
                Logger.setWarning(selected);
            });
        }};

        ToggleButton error = new ToggleButton() {{
            setToolTipText("Error logger level.");
            setBorderPainted(false);
            setIcon(ImageUtil.sizedIcon(LoggerPanel.class, "error.png", 23));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setSize(size);
            setSelected(true);
            addItemListener(ev -> {
                boolean selected = ev.getStateChange() == ItemEvent.SELECTED;
                Logger.setError(selected);
            });
        }};
        return new JPanel() {{
            setLayout(new FlowLayout());
            setBorder(new EmptyBorder(-4, 0, 0, 0));
            add(info);
            add(normal);
            add(warning);
            add(error);
        }};
    }

    private JPanel makeControlPane() {
        JPanel togglePane = new JPanel();
        togglePane.setLayout(new BorderLayout());
        JButton clearBtn = new JButton("Clear");
        input = new JTextField();

        clearBtn.addActionListener((e) -> loggerPanel.setText(""));
        input.addFocusListener(new DefaultFocusListener());
        input.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !Objects.equals(input.getText(), "")) {
                    Logger.console(input.getText());
                    if (input.getText().equals("clear") || input.getText().equals("cls")) {
                        loggerPanel.setText("");
                    }
                    else if(input.getText().startsWith("login "))
                    {
                        System.out.println("?????????????");
                        RSClient client = ClientManager.getClient();

                        String data = input.getText().replace("login ", "");
                        String[] parts = data.split(":");
                        client.setUsername(parts[0]);
                        client.setPassword(parts[1]);
                        //client.setGameState(20);
                    }
                    else
                    {
                        //TODO: exec stuff
                    }
                    input.setText("");
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        input.setForeground(Color.GRAY);
        input.setText("...");
        input.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (input.getText().equals("...")) {
                    input.setText("");
                    input.setForeground(Color.LIGHT_GRAY);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (input.getText().isEmpty()) {
                    input.setForeground(Color.GRAY);
                    input.setText("...");
                }
            }
        });

        togglePane.add(input, BorderLayout.CENTER);
        togglePane.add(clearBtn, BorderLayout.EAST);
        togglePane.setMaximumSize(new Dimension(1250, 25));
        togglePane.setMinimumSize(new Dimension(1250, 25));
        togglePane.setPreferredSize(new Dimension(1250, 25));
        return togglePane;
    }

    private void scrollToBottom() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                int endPosition = loggerPanel.getDocument().getLength();
                Rectangle bottom = loggerPanel.modelToView(endPosition);
                loggerPanel.scrollRectToVisible(bottom);
            } catch (BadLocationException e) {
                System.err.println("Could not scroll to " + e);
            }
        });
    }

    public void requestFocus() {
        SwingUtilities.invokeLater(() -> {
            input.requestFocus();
        });
    }

    private void startStatsWorkerThread()
    {
        StyleConstants.setForeground(headStyle, Color.decode("#ADD8E6"));
        StyleConstants.setForeground(bodyStyle, Color.LIGHT_GRAY);

        updateStatsPanel();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::updateStatsPanel, 0, 1, TimeUnit.SECONDS);
    }

    private void updateStatsPanel()
    {
        System.gc();
        String cpu = os.getProcessCpuLoad() + "";
        if (cpu.length() > 10) {
            cpu = cpu.substring(0, 10);
        }
        String finalCpu = cpu;
        SwingUtilities.invokeLater(() -> {
            try {
                doc.remove(0, doc.getLength()); // Clear the document efficiently

                // Append styled text
                doc.insertString(doc.getLength(), " (PID) ", headStyle);
                doc.insertString(doc.getLength(), ProcessHandle.current().pid() + "\n", bodyStyle);

                doc.insertString(doc.getLength(), " (CPU Usage) ", headStyle);
                doc.insertString(doc.getLength(), finalCpu + "\n", bodyStyle);

                doc.insertString(doc.getLength(), " (Total Ram) ", headStyle);
                doc.insertString(doc.getLength(), format.format(bytesToMegabytes(Runtime.getRuntime().totalMemory())) + "mb\n", bodyStyle);

                doc.insertString(doc.getLength(), " (Free Ram) ", headStyle);
                doc.insertString(doc.getLength(), format.format(bytesToMegabytes(Runtime.getRuntime().freeMemory())) + "mb\n", bodyStyle);

                doc.insertString(doc.getLength(), " (Used Ram) ", headStyle);
                doc.insertString(doc.getLength(), format.format(bytesToMegabytes(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())) + "mb\n", bodyStyle);

                doc.insertString(doc.getLength(), " (Active Threads) ", headStyle);
                doc.insertString(doc.getLength(), Thread.activeCount() + "\n", bodyStyle);

                doc.insertString(doc.getLength(), " (Peak Threads) ", headStyle);
                doc.insertString(doc.getLength(), threadMXBean.getPeakThreadCount() + "\n", bodyStyle);

                doc.insertString(doc.getLength(), " (Daemon Threads) ", headStyle);
                doc.insertString(doc.getLength(), threadMXBean.getDaemonThreadCount() + "\n", bodyStyle);

            } catch (Exception ignored) {
            }
        });
    }

    public static double bytesToMegabytes(long bytes) {
        double megabytes = bytes / 1048576.0;
        return Math.round(megabytes * 1000.0) / 1000.0;
    }
}
