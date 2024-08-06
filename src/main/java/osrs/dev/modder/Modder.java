package osrs.dev.modder;

import javassist.*;
import osrs.dev.modder.model.Mappings;
import osrs.dev.util.modding.CodeUtil;
import java.util.*;
import java.util.jar.JarFile;

public class Modder
{
    private static final Queue<CtMethod> preloadQueue = new LinkedList<>();

    /**
     * loads the jar file into memory in the form of CtClasses, then runs mapping and injection on it
     * @param jar gamepack jar file
     */
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

        findUsedElements();
        Mapper.map();
        GarbageScanner.scan();
        Injector.inject();
    }

    /**
     * Pre-maps all the used methods into a set so that we can make sure
     * and only consider their contents in our mapping tasks.
     */
    private static void findUsedElements()
    {
        for(CtClass clazz : Mappings.getClasses())
        {
            if(clazz.getName().length() > 2 && !clazz.getName().equals("client"))
                continue;

            for(CtMethod method : clazz.getDeclaredMethods()) {
                if (Modifier.isAbstract(method.getModifiers()))
                    continue;

                if (!CodeUtil.fromSuper(method))
                    continue;

                if(!preloadQueue.contains(method))
                {
                    preloadQueue.add(method);
                }
            }
        }

        while(!preloadQueue.isEmpty())
        {
            CtMethod next = preloadQueue.remove();
            if(!Mappings.getUsedMethods().add(next.getLongName()))
                continue;
            CodeUtil.scanForMethodRefs(next, preloadQueue);
        }
    }
}
