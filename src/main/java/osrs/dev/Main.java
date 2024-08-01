package osrs.dev;

import osrs.dev.client.Loader;
import osrs.dev.modder.Modder;
import osrs.dev.ui.ClientFrame;
import osrs.dev.util.JagConfigUtil;

public class Main {
    private static JagConfigUtil config;
    public static void main(String[] args) throws Exception {
        config = new JagConfigUtil(60);
        Modder.mod(config.getGamePack());
        Loader loader = new Loader(config.getAppletParameters(), config.getGameParameters());
        loader.run();
        new ClientFrame(loader.getApplet());
    }
}