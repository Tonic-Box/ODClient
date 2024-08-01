package osrs.dev.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarUtil
{
    public static Map<String, byte[]> extractClasses(JarFile jarFile) throws IOException {
        Map<String, byte[]> classMap = new HashMap<>();

        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".class")) {
                String className = entry.getName().replace("/", ".").replaceAll(".class$", "");
                byte[] classBytes = readClassBytes(jarFile, entry);

                classMap.put(className, classBytes);
            }
        }

        return classMap;
    }

    private static byte[] readClassBytes(JarFile jarFile, JarEntry entry) throws IOException {
        InputStream is = jarFile.getInputStream(entry);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int bytesRead;
        byte[] data = new byte[4096];

        while ((bytesRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }

        return buffer.toByteArray();
    }
}
