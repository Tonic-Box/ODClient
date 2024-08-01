package osrs.dev.modder;

import javassist.ClassPool;
import javassist.CtClass;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;

public class Modder
{
    @Getter
    private static final List<CtClass> classes = new ArrayList<>();

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
                .forEach(classes::add);

        Mapper.map();
    }
}
