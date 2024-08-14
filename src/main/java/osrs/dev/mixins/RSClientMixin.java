package osrs.dev.mixins;

import osrs.dev.annotations.mixin.*;
import osrs.dev.api.RSClient;
import osrs.dev.util.eventbus.EventBus;
import osrs.dev.util.eventbus.events.GameTick;

import java.util.UUID;

@Mixin("Client")
public abstract class RSClientMixin extends RSGameEngineMixin implements RSClient
{
    @Inject
    private String uid;

    @Inject
    private static boolean shouldProcessTick;

    @Inject
    private static int vx$serverTick = 0;

    @Inject
    public int gametick = 0;

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

    @MethodHook("doCycle")
    public boolean doCycle() {
        if(shouldProcessTick)
        {
            shouldProcessTick = false;
            gametick = gametick + 1;
            EventBus.post(this, new GameTick(gametick));
        }
        if (queue.peek() != null) {
            queue.poll().run();
        }

        return false;
    }

    @FieldHook("serverCycle")
    public static boolean onServerTicksChanged(int cycle)
    {
        if (cycle == vx$serverTick + 1)
        {
            shouldProcessTick = true;
        }
        vx$serverTick = cycle;
        return false;
    }

    @Shadow("clientField")
    @Override
    public abstract RSClient getClient();

    @Shadow("Login_username")
    @Override
    public abstract String getUsername();

    @Shadow("Login_username")
    @Override
    public abstract void setUsername(String username);

    @Shadow("Login_password")
    @Override
    public abstract void setPassword(String password);
    @Shadow("JX_CHARACTER_ID")
    @Override
    public abstract String getCharacterId();
}
