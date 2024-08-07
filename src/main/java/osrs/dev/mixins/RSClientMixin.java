package osrs.dev.mixins;

import osrs.dev.annotations.Inject;
import osrs.dev.annotations.MethodHook;
import osrs.dev.annotations.Mixin;
import osrs.dev.annotations.Shadow;
import osrs.dev.api.RSClient;
import osrs.dev.util.Logger;
import osrs.dev.util.eventbus.EventBus;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

@Mixin("Client")
public abstract class RSClientMixin extends RSGameEngineMixin implements RSClient
{
    @Inject
    private String uid;

    @Inject
    private boolean shouldProcessTick;

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
        if (queue.peek() != null) {
            queue.poll().run();
        }

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

    @Shadow(value = "menuAction", method = true)
    @Override
    public abstract void doAction(int param0, int param1, int opcode, int identifier, int itemId, int worldViewId, String option, String target, int canvasX, int canvasY);
}
