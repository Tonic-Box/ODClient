package osrs.dev.util;

import lombok.Getter;
import lombok.Setter;
import osrs.dev.api.RSClient;

import java.util.ArrayList;
import java.util.List;
@Getter
public class ClientManager
{
    private static List<RSClient> clients = new ArrayList<>();

    @Setter
    private static String currentClient = null;

    public static RSClient getClient()
    {
        return getClient(currentClient);
    }

    public static RSClient getClient(String clientId)
    {
        if(clientId == null)
            return null;

        return clients.stream().filter(c -> c.getClientID().equals(clientId)).findFirst().orElse(null);
    }
}
