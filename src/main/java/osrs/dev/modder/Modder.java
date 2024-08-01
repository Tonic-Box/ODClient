package osrs.dev.modder;

import javassist.ClassPool;
import osrs.dev.modder.model.Mappings;
import java.util.Collections;
import java.util.jar.JarFile;

public class Modder
{
    public static void mod(JarFile jar)
    {
        Collections.list(jar.entries())
                .stream()
                .filter(je -> je.getName().endsWith(".class"))
                .map(je -> {
                    try
                    {
                        return ClassPool.getDefault().makeClass(jar.getInputStream(je));
                    }
                    catch (Exception ex)
                    {
                        throw new RuntimeException(ex);
                    }
                })
                .forEach(clazz -> Mappings.getClasses().add(clazz));

        Mapper.map();
        Injector.inject();
    }
}
