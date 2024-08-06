package osrs.dev.client;

import javassist.CtClass;
import lombok.Getter;
import lombok.Setter;
import osrs.dev.api.RSClient;
import osrs.dev.modder.model.Mappings;
import osrs.dev.util.JagConfigUtil;
import osrs.dev.util.Profiler;

import java.applet.Applet;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/**
 * This class is used for loading and managing a client instance
 */
public class Loader extends Stub
{
    @Getter
    @Setter
    private OSClassLoader osClassLoader;
    private final Dimension GAME_FIXED_SIZE;
    private final HashMap<String,String> appletParameters = new HashMap<>();
    @Getter
    private boolean shuttingDown = false;

    /**
     * create a new client loader
     * @param config jagex config
     * @throws Exception exception
     */
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

    /**
     * start up the game applet
     */
    public void run() {
        System.setProperty("jagex.disableBouncyCastle", "true");
        this.applet.resize(GAME_FIXED_SIZE);
        this.applet.setPreferredSize(GAME_FIXED_SIZE);
        this.applet.setMinimumSize(GAME_FIXED_SIZE);
        this.applet.init();
        this.applet.start();
    }

    /**
     * get the game applet cast to our RSClient api interface
     * @return api object
     */
    public RSClient getApi()
    {
        return ((RSClient) applet).getClient();
    }

    /**
     * shutdown and cleanup the client instance
     */
    @Override
    public void Shutdown() {
        shuttingDown = true;
        Profiler profiler = Profiler.start("ShutDown Applet");
        appletParameters.clear();
        gameParameters.clear();
        getApi().setShouldExit(true);
        try { Thread.sleep(1000); } catch (Exception ignored) { }
        gameParameters = null;
        applet = null;
        osClassLoader = null;
        profiler.stopMS();
    }

    @Override
    public URL getCodeBase() {
        try {
            return new URL(this.appletParameters.get("codebase"));
        } catch (MalformedURLException exception) {
            return null;
        }
    }

    /**
     * The classloader for loading the applet instance
     */
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
