package osrs.dev.util;

import lombok.Getter;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.jar.JarFile;

@Getter
public class JagConfigUtil
{
    protected final Map<String, String> config;
    protected final JarFile gamePack;
    protected final HashMap<String, String> gameParameters = new HashMap<>();
    protected final HashMap<String, String> appletParameters = new HashMap<>();

    public JagConfigUtil(int world) throws IOException {
        config = readJavConfig(world);
        gamePack = fetchGamePack();
        filterParameters(world);
    }

    private Map<String, String> readJavConfig(int world)
    {
        Map<String, String> configMap = new HashMap<>();
        try (Scanner scanner = new Scanner(new URL("http://oldschool" + world + ".runescape.com/jav_config.ws").openStream())) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split("=", 3);
                configMap.put(parts[0] + (parts.length == 3 ? parts[1] : ""), parts[parts.length - 1]);
            }
        } catch (IOException ignored) {
        }
        return configMap;
    }

    private void filterParameters(int world) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("http://oldschool" + world + ".runescape.com/jav_config.ws").openStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int index = line.indexOf("=");
                if (index == -1) continue;

                String segment = line.substring(0, index);
                if (segment.equals("msg")) continue;

                int index1 = line.indexOf("=", index + 2);
                if (segment.equals("param")) {
                    gameParameters.put(line.substring(index + 1, index1), line.substring(index1 + 1));
                } else {
                    appletParameters.put(segment, line.substring(index + 1));
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public Dimension getAppletMinDimension()
    {
        int width = Integer.parseInt(config.get("applet_minwidth"));
        int height = Integer.parseInt(config.get("applet_minheight"));
        return new Dimension(width, height);
    }

    public JarFile fetchGamePack() throws IOException {
        int world = Integer.parseInt(config.get("param12")) - 300;
        URL jarUrl = new URL("jar:http://oldschool" + world + ".runescape.com/" + getValue("initial_jar") + "!/");
        return ((JarURLConnection) jarUrl.openConnection()).getJarFile();
    }

    public String getValue(String key)
    {
        return config.get(key);
    }

    @Override
    public String toString()
    {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, String> entry : config.entrySet()) {
            out.append(entry.getKey()).append(" => ").append(entry.getValue()).append("\n");
        }
        return out.toString();
    }
}
