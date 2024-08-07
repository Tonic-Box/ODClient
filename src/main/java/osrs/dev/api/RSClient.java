package osrs.dev.api;

import osrs.dev.util.Logger;

import java.applet.AppletStub;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface RSClient extends RSGameEngine, AppletStub
{
    String getClientID();
    void setDisableRender(boolean bool);
    RSClient getClient();
    String getUsername();
    void setUsername(String username);
    void setPassword(String password);
    String getCharacterId();
    void doAction(int param0, int param1, int opcode, int identifier, int itemId, int worldViewId, String option, String target, int canvasX, int canvasY);

    /**
     * invoke on client thread with return
     * @param supplier runnable block
     * @return return value
     */
    default <T> T invoke(Supplier<T> supplier)
    {
        if (!isClientThread()) {
            CompletableFuture<T> future = new CompletableFuture<>();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        future.complete(supplier.get());
                    }
                    catch (Exception exception)
                    {
                        Logger.error("# [" + getClientID() + "] Error in invoked runnable");
                        exception.printStackTrace();
                    }
                }
            };
            invoke(runnable);
            return future.join();
        } else {
            return supplier.get();
        }
    }

    /**
     * invoke on client thread
     * @param r runnable block
     */
    default void invoke(Runnable r)
    {
        Runnable runnable = () -> {
            try
            {
                r.run();
            }
            catch (Exception exception)
            {
                Logger.error("# [" + getClientID() + "] Error in invoked runnable");
                exception.printStackTrace();
            }
        };
        if(isClientThread())
        {
            runnable.run();
            return;
        }
        addToInvokeQueue(runnable);
    }
}
