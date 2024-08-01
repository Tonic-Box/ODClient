package osrs.dev.mixins;

import osrs.dev.annotations.Inject;
import osrs.dev.annotations.Mixin;
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
}
