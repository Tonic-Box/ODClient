package osrs.dev;

import com.formdev.flatlaf.FlatDarkLaf;
import lombok.SneakyThrows;
import osrs.dev.client.Loader;
import osrs.dev.modder.Modder;
import osrs.dev.ui.ODClientFrame;
import osrs.dev.util.JagConfigUtil;

import javax.swing.*;

public class Main {
    private static JagConfigUtil config;
    public static void main(String[] args) throws Exception {
        FlatDarkLaf.setup();
        config = new JagConfigUtil(60);
        Modder.mod(config.getGamePack());
        SwingUtilities.invokeLater(ODClientFrame::new);
    }

    @SneakyThrows
    public static Loader getLoader()
    {
        return new Loader(config);
    }
}