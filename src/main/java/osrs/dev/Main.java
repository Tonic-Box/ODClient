package osrs.dev;

import com.formdev.flatlaf.FlatDarkLaf;
import lombok.SneakyThrows;
import osrs.dev.client.Loader;
import osrs.dev.modder.Modder;
import osrs.dev.ui.ODClientFrame;
import osrs.dev.util.JagConfigUtil;
import osrs.dev.util.OptionsParser;

import javax.swing.*;
import java.io.File;

public class Main {
    private static JagConfigUtil config;
    public static final File ODCLIENT_HOME = new File(System.getProperty("user.home") + "/ODClient/");
    public static void main(String[] args) throws Exception {
        OptionsParser.parse(args);
        FlatDarkLaf.setup();
        config = new JagConfigUtil(OptionsParser.getWorld());
        Modder.mod(config.getGamePack());
        SwingUtilities.invokeLater(ODClientFrame::new);
    }

    /**
     * Launch a new client Loader instance
     * @return client Loader object
     */
    @SneakyThrows
    public static Loader getLoader()
    {
        return new Loader(config);
    }
}