package osrs.dev.api;

public interface RSGameEngine
{
    void setDisableRender(boolean bool);
    void setShouldExit(boolean bool);
    boolean getShouldExit();
    void addToInvokeQueue(Runnable r);

    /**
     * check if we are on the client thread
     * @return bool
     */
    boolean isClientThread();

    /**
     * get the clientLoaders thread
     * @return
     */
    Thread getClientThread();
}
