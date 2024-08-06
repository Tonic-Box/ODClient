package osrs.dev.mixins;

import osrs.dev.Main;
import osrs.dev.annotations.Inject;
import osrs.dev.annotations.Mixin;
import osrs.dev.annotations.Replace;
import osrs.dev.annotations.Shadow;
import osrs.dev.api.RSClient;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;
import java.util.UUID;

@Mixin("PlatformInfo")
public abstract class RSPlatformInfoMixin
{
    @Shadow("clientField")
    public abstract RSClient getClient();

    @Inject
    private File cachedUUIDFile;

    @Inject
    private Properties cachedUUIDProperties;

    @Replace("getDeviceId")
    public String getDeviceId(int os)
    {
        String cachedDeviceId = getCachedUUID();
        if (cachedDeviceId == null)
        {
            cachedDeviceId = UUID.randomUUID().toString();
            writeCachedUUID(cachedDeviceId);
        }
        return cachedDeviceId;
    }

    @Inject
    private String getCachedUUID()
    {
        if (cachedUUIDProperties == null)
        {
            cachedUUIDFile = new File(Main.ODCLIENT_HOME, "uuid-cached.properties");
            cachedUUIDProperties = new Properties();

            if (cachedUUIDFile.exists())
            {
                try (InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(cachedUUIDFile), StandardCharsets.UTF_8))
                {
                    cachedUUIDProperties.load(inputStreamReader);
                }
                catch (IOException ignored)
                {
                }
            }
        }

        return cachedUUIDProperties.getProperty(getClient().getUsername() != null && !getClient().getUsername().isEmpty() ? getClient().getUsername() : getClient().getCharacterId());
    }

    @Inject
    private void writeCachedUUID(String UUID)
    {
        cachedUUIDProperties.setProperty(getClient().getUsername() != null && !getClient().getUsername().isEmpty() ? getClient().getUsername() : getClient().getCharacterId(), UUID);
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(Files.newOutputStream(cachedUUIDFile.toPath()), StandardCharsets.UTF_8))
        {
            cachedUUIDProperties.store(outputStreamWriter, "Cached UUID");
        }
        catch (IOException e)
        {
        }
    }
}
