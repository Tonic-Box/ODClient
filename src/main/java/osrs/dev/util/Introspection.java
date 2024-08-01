package osrs.dev.util;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.bytecode.ClassFile;
import lombok.SneakyThrows;
import osrs.dev.annotations.Mixin;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;

public class Introspection
{
    /**
     * Fetch a map of all valid pairings of mixin class and interface
     * @param mixinPackage package where the mixin classes are
     * @param interfacePackage package where the interface classes are
     * @return HashMap(Mixin,IFace)
     */
    public static HashMap<CtClass,CtClass> getPairs(String mixinPackage, String interfacePackage)
    {
        HashMap<CtClass,CtClass> pairs = new HashMap<>();

        //get all classes from out mixin scope and from out interface scope
        List<String> ignores = new ArrayList<>() {{ add("MixinInjector"); }};
        List<CtClass> interfaces = getClasses(interfacePackage, ignores);
        List<CtClass> mixins = getClasses(mixinPackage, ignores);

        if(interfaces.isEmpty() || mixins.isEmpty())
            return pairs;

        //loop through mixin classes gathered
        for(CtClass mixinClazz : mixins)
        {
            try
            {
                if(mixinClazz.getName().equals("MixinInjector"))
                    continue;

                //Annotation check
                if(!mixinClazz.hasAnnotation(Mixin.class))
                {
                    System.out.println("[Skipped] <" + mixinClazz.getName() + "> missing @Mixin annotation.");
                    continue;
                }

                //Annotation value check
                Mixin annot = (Mixin)mixinClazz.getAnnotation(Mixin.class);
                String tag = annot.value();
                if(tag.isEmpty())
                {
                    System.out.println("[Skipped] <" + mixinClazz.getName() + "> missing tag identifier in @Mixin annotation.");
                    continue;
                }

                //Interface implementation check
                CtClass[] ext = mixinClazz.getInterfaces();
                CtClass ifaceClazz = null;
                if(ext.length != 0)
                {
                    String ifaceName = ext[0].getName();
                    ifaceClazz = interfaces.stream().filter(c -> c.getName().equals(ifaceName)).findFirst().orElse(null);
                }

                //add it to our list
                pairs.put(mixinClazz, ifaceClazz);
            }
            catch (Exception ignored)
            {

            }
        }
        return pairs;
    }

    /**
     * get all classes from a package
     * @param packageName package name
     * @param ignores class names to ignore
     * @return list of CtClass objects from package
     */
    public static List<CtClass> getClasses(String packageName, List<String> ignores)
    {

        try
        {
            String jarPath = Introspection.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
            if(jarPath.endsWith(".jar"))
            {
                return Introspection.getClassesExternal(packageName, ignores);
            }
            else
            {
                return Introspection.getClassesIntelliJ(packageName, ignores);
            }

        }
        catch (Exception ignored)
        {
        }
        return new ArrayList<>();
    }

    /**
     * get all classes from a package (If running from intellij)
     * @param packageName package name
     * @param ignores class names to ignore
     * @return list of CtClass objects from package
     */
    public static List<CtClass> getClassesExternal(String packageName, List<String> ignores)
    {
        List<CtClass> classes = new ArrayList<>();
        try
        {
            String jarPath = Introspection.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
            String finalPackageName = packageName.replace(".", "/") + "/";
            File file = new File(jarPath);
            JarFile jar = new JarFile(file);
            Collections.list(jar.entries()).forEach(e -> {
                if(e.getName().startsWith(finalPackageName) && !e.getName().replace(finalPackageName, "").contains("/") && e.getName().endsWith(".class"))
                {
                    if(ignores == null || ignores.stream().noneMatch(s -> e.getName().contains(s)))
                    {
                        try {
                            classes.add(ClassPool.getDefault().makeClass(jar.getInputStream(e)));
                        } catch (IOException ignored) { }
                    }
                }
            });
        }
        catch (Exception ignored) {
        }
        return classes;
    }

    /**
     * get all classes from a package (If running from IntelliJ)
     * @param packageName package name
     * @param ignores class names to ignore
     * @return list of CtClass objects from package
     */
    public static List<CtClass> getClassesIntelliJ(String packageName, List<String> ignores) {
        List<CtClass> classes = new ArrayList<>();
        try
        {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(path);
            List<File> dirs = new ArrayList<>();
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                dirs.add(new File(resource.getFile()));
            }
            for (File directory : dirs) {
                classes.addAll(findClasses(directory, ignores));
            }
        }
        catch (Exception ignored) { }
        return classes;
    }

    private static List<CtClass> findClasses(File directory, List<String> ignores) {
        List<CtClass> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        if(files == null)
            return classes;
        for (File file : files) {
            if (file.getName().endsWith(".class") && (ignores == null || ignores.stream().noneMatch(s -> file.getName().contains(s)))) {
                try {
                    DataInputStream stream = new DataInputStream(new DataInputStream(new FileInputStream(file)));
                    ClassFile cf = new ClassFile(stream);
                    classes.add(ClassPool.getDefault().makeClass(cf));
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }
        return classes;
    }

    @SneakyThrows
    public static boolean hasInterface(CtClass clazz, CtClass iface)
    {
        return Arrays.stream(clazz.getInterfaces()).anyMatch(i -> i.getName().equals(iface.getName()));
    }

    /**
     * check if a field has a given annotation
     * @param field target field
     * @param annotation annotation type
     * @return boolean
     */
    public static boolean has(CtField field, Class<?> annotation)
    {
        return field.hasAnnotation(annotation);
    }

    public static boolean has(CtClass clazz, Class<?> annotation)
    {
        return clazz.hasAnnotation(annotation);
    }

    /**
     * check if a method has a given annotation
     * @param method target method
     * @param annotation annotation type
     * @return boolean
     */
    public static boolean has(CtMethod method, Class<?> annotation)
    {
        return method.hasAnnotation(annotation);
    }

    /**
     * get a classes super class
     * @param target target class
     * @return string name
     */
    public static String getSuper(CtClass target)
    {
        String spr;
        try {
            target.getSuperclass();
            spr = "SuperDoesExist";
        }
        catch (Exception ex)
        {
            spr = ex.getMessage().replace("cannot find ", "");
        }
        return spr;
    }
}
