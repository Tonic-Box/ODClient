package osrs.dev.util;

import lombok.SneakyThrows;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Logger {
    private static Logger INSTANCE;

    /**
     * set normal logging
     * @param state state
     */
    public static void setNormal(boolean state)
    {
        if(INSTANCE == null)
            return;
        INSTANCE.normal = state;
    }

    /**
     * set info logging
     * @param state state
     */
    public static void setInfo(boolean state)
    {
        if(INSTANCE == null)
            return;
        INSTANCE.info = state;
    }

    /**
     * set warning logging
     * @param state state
     */
    public static void setWarning(boolean state)
    {
        if(INSTANCE == null)
            return;
        INSTANCE.warning = state;
    }

    /**
     * set error logging
     * @param state state
     */
    public static void setError(boolean state)
    {
        if(INSTANCE == null)
            return;
        INSTANCE.error = state;
    }

    /**
     * for console input
     * @param data data
     */
    public static void console(String data, Object... args)
    {
        if(INSTANCE == null)
            return;
        INSTANCE._console(format(data, args));
    }

    /**
     * for console output
     * @param head header
     * @param body message
     */
    public static void consoleOutput(String head, String body)
    {
        if(INSTANCE == null)
            return;
        INSTANCE._consoleOutput("(" + head + ") ", body);
    }

    /**
     * for console error output
     * @param head header
     * @param body message
     */
    public static void consoleErrorOutput(String head, String body)
    {
        if(INSTANCE == null)
            return;
        INSTANCE._consoleErrorOutput("(" + head + ") ", body);
    }

    /**
     * for normal purposeful logging
     * @param data data
     */
    public static void norm(String data, Object... args)
    {
        if(INSTANCE == null)
            return;
        INSTANCE._norm(format(data, args));
    }

    /**
     * for general diagnostic logging
     * @param data data
     */
    public static void info(String data, Object... args)
    {
        if(INSTANCE == null)
            return;
        INSTANCE._info(format(data, args));
    }

    /**
     * For non fatal warnings
     * @param data data
     */
    public static void warn(String data, Object... args)
    {
        if(INSTANCE == null)
            return;
        INSTANCE._warn(format(data, args));
    }

    /**
     * for fatal errors
     * @param data data
     */
    public static void error(String data, Object... args)
    {
        if(INSTANCE == null)
            return;
        INSTANCE._error(format(data, args));
    }

    public static void error(String data, Throwable error)
    {
        if(INSTANCE == null)
            return;
        INSTANCE._error(data);
        INSTANCE._error(error.toString());
        for (StackTraceElement element : error.getStackTrace())
            INSTANCE._error(element.toString());
    }

    private static final Pattern BRACE_PATTERN = Pattern.compile("\\{}");

    private static String format(String message, Object... args) {
        Matcher matcher = BRACE_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        int argIndex = 0;

        while (matcher.find()) {
            if (argIndex < args.length) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(args[argIndex])));
                argIndex++;
            } else {
                matcher.appendReplacement(sb, "{}");
            }
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public static void trace(String data, Object... args)
    {
    }

    /**
     * Initial binding for statically stored instance
     * @param console Our output component for the logger
     */
    public static void setInstance(JTextPane console)
    {
        INSTANCE = new Logger(console);
    }

    private final JTextPane console;
    private boolean info = true;
    private boolean normal = true;
    private boolean warning = true;
    private boolean error = true;
    private final SimpleAttributeSet CONSOLE;
    private final SimpleAttributeSet NORM;
    private final SimpleAttributeSet INFO;
    private final SimpleAttributeSet WARN;
    private final SimpleAttributeSet ERROR;
    private final float SPACING = 1.5f;
    private Logger(JTextPane console)
    {
        this.console = console;

        CONSOLE = new SimpleAttributeSet();
        StyleConstants.setForeground(CONSOLE, Color.LIGHT_GRAY);

        NORM = new SimpleAttributeSet();
        StyleConstants.setForeground(NORM, Color.GREEN);

        INFO = new SimpleAttributeSet();
        StyleConstants.setForeground(INFO, Color.decode("#ADD8E6"));

        WARN = new SimpleAttributeSet();
        StyleConstants.setForeground(WARN, Color.YELLOW);

        ERROR = new SimpleAttributeSet();
        StyleConstants.setForeground(ERROR, Color.RED);
    }

    private void stream(String data, SimpleAttributeSet style)
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {
                StyleConstants.setLineSpacing(style,SPACING);
                console.getStyledDocument().insertString(console.getStyledDocument().getLength(), data + "\n", style);
            }
        });
    }

    @SneakyThrows
    private void _console(String data)
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {
                StyleConstants.setLineSpacing(NORM,SPACING);
                StyleConstants.setLineSpacing(CONSOLE,SPACING);
                console.getStyledDocument().insertString(console.getStyledDocument().getLength(), "$ ", NORM);
                console.getStyledDocument().insertString(console.getStyledDocument().getLength(), data + "\n", CONSOLE);
            }
        });
    }

    @SneakyThrows
    private void _consoleOutput(String head, String body)
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {
                StyleConstants.setLineSpacing(NORM,SPACING);
                StyleConstants.setLineSpacing(CONSOLE,SPACING);
                console.getStyledDocument().insertString(console.getStyledDocument().getLength(), head, INFO);
                console.getStyledDocument().insertString(console.getStyledDocument().getLength(), body + "\n", CONSOLE);
            }
        });
    }

    @SneakyThrows
    private void _consoleErrorOutput(String head, String body)
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {
                StyleConstants.setLineSpacing(NORM,SPACING);
                StyleConstants.setLineSpacing(CONSOLE,SPACING);
                console.getStyledDocument().insertString(console.getStyledDocument().getLength(), head, INFO);
                console.getStyledDocument().insertString(console.getStyledDocument().getLength(), body + "\n", ERROR);
            }
        });
    }

    private void _norm(String data)
    {
        if(!normal)
            return;

        stream(data, NORM);
    }

    private void _info(String data)
    {
        if(!info)
            return;

        stream(data, INFO);
    }

    private void _warn(String data)
    {
        if(!warning)
            return;

        stream(data, WARN);
    }

    private void _error(String data)
    {
        if(!error)
            return;

        stream(data, ERROR);
    }
}