package osrs.dev.client;

import lombok.Getter;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.URL;
import java.util.HashMap;

public abstract class ClientLoader implements AppletStub {
    @Getter
    protected Applet applet;
    protected HashMap<String,String> gameParameters = new HashMap<>();

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public URL getDocumentBase() {
        return this.getCodeBase();
    }

    @Override
    public String getParameter(String param) {
        return this.gameParameters.get(param);
    }

    @Override
    public AppletContext getAppletContext() {
        return null;
    }

    @Override
    public void appletResize(int width, int height) {
    }

    /**
     * Tasking for when we close a tab and shut down a client applet instance.
     * @throws Exception exception
     */
    public abstract void Shutdown() throws Exception;
}
