package osrs.dev.mixins;

import osrs.dev.annotations.Inject;
import osrs.dev.annotations.Mixin;
import osrs.dev.annotations.Shadow;
import osrs.dev.api.RSClient;

import java.util.UUID;

@Mixin("Client")
public abstract class RSClientMixin implements RSClient
{
    @Inject
    private String uid;

    @Inject
    @Override
    public String getClientID()
    {
        if(uid == null)
        {
            uid = UUID.randomUUID().toString();
        }
        return uid;
    }

    @Shadow("clientField")
    public static RSClient getC() {
        return null;
    }

    @Inject
    @Override
    public RSClient getClient() {
        return getC();
    }
}
