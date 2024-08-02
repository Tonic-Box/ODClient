package osrs.dev.mixins;

import osrs.dev.annotations.Inject;
import osrs.dev.annotations.MethodHook;
import osrs.dev.annotations.Mixin;
import osrs.dev.annotations.Shadow;
import osrs.dev.api.RSClient;
import osrs.dev.util.Logger;

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
    @Override
    public abstract RSClient getClient();
}
