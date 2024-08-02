package osrs.dev.ui;

import osrs.dev.api.RSClient;
import osrs.dev.client.Loader;
import osrs.dev.util.ClientManager;
import osrs.dev.util.Logger;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;

public class ClientContainer extends JPanel
{
    private final Loader loader;
    private final String clientID;
    public ClientContainer(Loader loader)
    {
        this.loader = loader;
        setLayout(new BorderLayout());
        Applet applet = loader.getApplet();
        loader.run();
        add(applet, BorderLayout.CENTER);
        RSClient client = loader.getApi();
        ClientManager.getClients().add(client);
        clientID = client.getClientID();
        client.setDisableRender(ClientManager.getCurrentClient() != null);
    }

    public void swapToFront()
    {
        if(clientID.equals(ClientManager.getCurrentClient()))
            return;
        ClientManager.setCurrentClient(clientID);
        Logger.info("Swapped to: " + clientID);
    }

    public void shutdown()
    {
        ClientManager.getClients().removeIf(c -> c.getClientID().equals(clientID));
        try
        {
            loader.Shutdown();
        }
        catch (RuntimeException ignored) {}
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        System.gc();
        System.runFinalization();
    }
}
