package osrs.dev.client;

import javassist.CtClass;
import lombok.Getter;
import lombok.Setter;
import osrs.dev.modder.model.Mappings;
import osrs.dev.util.JagConfigUtil;
import java.applet.Applet;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class Loader extends Stub
{
    @Getter
    @Setter
    private OSClassLoader osClassLoader;
    private final Dimension GAME_FIXED_SIZE;
    private final HashMap<String,String> appletParameters = new HashMap<>();
    public Loader(JagConfigUtil config) throws Exception
    {
        this.osClassLoader = new OSClassLoader(Mappings.getClasses(), "ODClient");
        this.appletParameters.putAll(config.getAppletParameters());
        this.gameParameters.putAll(config.getGameParameters());
        String mainClass = this.appletParameters.get("initial_class");
        this.GAME_FIXED_SIZE = config.getAppletMinDimension();
        if(mainClass == null)
        {
            return;
        }
        mainClass = mainClass.substring(0, mainClass.indexOf(46));
        applet = (Applet)(osClassLoader.loadClass(mainClass)).newInstance();
        this.applet.setStub(this);
    }

    public void run() {
        System.setProperty("jagex.disableBouncyCastle", "true");
        this.applet.resize(GAME_FIXED_SIZE);
        this.applet.setPreferredSize(GAME_FIXED_SIZE);
        this.applet.setMinimumSize(GAME_FIXED_SIZE);
        this.applet.init();
        this.applet.start();
    }

    @Override
    public void Shutdown() {

    }

    @Override
    public URL getCodeBase() {
        try {
            return new URL(this.appletParameters.get("codebase"));
        } catch (MalformedURLException exception) {
            return null;
        }
    }

    @Getter
    public static class OSClassLoader extends ClassLoader
    {
        private final List<CtClass> classes;

        public OSClassLoader(List<CtClass> classes, String name)
        {
            super(name, Loader.class.getClassLoader());
            this.classes = classes;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException
        {
            return getLoadedClass(name);
        }

        public Class<?> getLoadedClass(String name) throws ClassNotFoundException
        {
            Class<?> loadedClass = this.findLoadedClass(name);
            if (loadedClass != null) {
                return loadedClass;
            }

            CtClass clazz = getClasses().stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
            if(clazz == null)
            {
                return super.loadClass(name);
            }
            try {
                byte[] bytes =  clazz.toBytecode();
                if (bytes != null && bytes.length > 0) {
                    loadedClass = defineClass(name, bytes, 0, bytes.length);
                    if (loadedClass != null) {
                        return loadedClass;
                    }
                }
            }
            catch (Exception ignored) {}

            return super.loadClass(name);
        }
    }
}
