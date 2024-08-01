package osrs.dev.api;

public interface RSClient
{
    String getClientID();
    void setDisableRender(boolean bool);
    RSClient getClient();
}
