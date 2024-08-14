package osrs.dev.modder;

import javassist.*;
import osrs.dev.annotations.mapping.MappingSet;
import osrs.dev.annotations.mapping.Definition;
import osrs.dev.modder.model.Mappings;
import osrs.dev.util.Introspection;
import osrs.dev.util.SignatureUtil;
import java.lang.reflect.Method;
import java.util.*;

public class Mapper
{
    private static final Set<Method> classMappings = new HashSet<>();
    private static final Set<Method> methodMappings = new HashSet<>();
    private static final Set<Method> fieldMappings = new HashSet<>();
    private static final Set<Method> afterClassMappings = new HashSet<>();
    private static final Set<Method> afterMethodMappings = new HashSet<>();
    private static final Set<Method> afterFieldMappings = new HashSet<>();
    private static final Map<String,String[]> targets = new HashMap<>();
    private static final Map<String,String[]> afterTargets = new HashMap<>();


    /**
     * handles finding our mappings
     */
    public static void map()
    {
        loadMappingDefinitions();

        //round 1
        for(CtClass clazz : Mappings.getClasses())
        {
            clazz.defrost();

            classMappings.forEach(m -> callClassScanner(m, clazz));
            for(CtMethod method : clazz.getDeclaredMethods())
            {
                if(!Mappings.getUsedMethods().contains(method.getLongName()))
                    continue;
                methodMappings.forEach(m -> callMethodScanner(m, method));
            }

            for(CtField field : clazz.getDeclaredFields())
            {
                fieldMappings.forEach(m -> callFieldScanner(m, field));
            }
        }

        //round 2
        for(CtClass clazz : Mappings.getClasses())
        {
            clazz.defrost();

            afterClassMappings.forEach(m -> callClassScanner(m, clazz));
            for(CtMethod method : clazz.getDeclaredMethods())
            {
                if(!Mappings.getUsedMethods().contains(method.getLongName()))
                    continue;
                afterMethodMappings.forEach(m -> callMethodScanner(m, method));
            }

            for(CtField field : clazz.getDeclaredFields())
            {
                afterFieldMappings.forEach(m -> callFieldScanner(m, field));
            }
        }

        boolean pass = true;

        for(var entry : targets.entrySet())
        {
            String source = entry.getKey();
            for(String mapping : entry.getValue())
            {
                if(Mappings.findByTag(mapping) != null)
                    continue;

                System.err.println("[Missing Mapping (pre)] " + source + " > \"" + mapping + "\"");
                pass = false;
            }
        }

        for(var entry : afterTargets.entrySet())
        {
            String source = entry.getKey();
            for(String mapping : entry.getValue())
            {
                if(Mappings.findByTag(mapping) != null)
                    continue;

                System.err.println("[Missing Mapping (post)] " + source + " > \"" + mapping + "\"");
                pass = false;
            }
        }

        if(!pass)
        {
            System.exit(0);
        }

        classMappings.clear();
        methodMappings.clear();
        fieldMappings.clear();
        afterClassMappings.clear();
        afterMethodMappings.clear();
        afterFieldMappings.clear();
        targets.clear();
        afterTargets.clear();
    }

    private static void loadMappingDefinitions()
    {
        List<Class<?>> mappingDefinitions = Introspection.getClasses("osrs.dev.mappings", new ArrayList<>());
        for(Class<?> clazz : mappingDefinitions)
        {
            if(!clazz.isAnnotationPresent(MappingSet.class))
                continue;
            for(Method method : clazz.getDeclaredMethods())
            {
                if(!method.isAnnotationPresent(Definition.class))
                    continue;

                if(method.getParameterTypes().length != 1)
                    continue;

                Definition definition = method.getAnnotation(Definition.class);
                if(definition != null)
                {
                    if(definition.secondary())
                    {
                        if(scannerTypeOf(method, CtClass.class))
                        {
                            afterClassMappings.add(method);
                            afterTargets.put(clazz.getName() + "::" + method.getName() + SignatureUtil.getMethodSignature(method), definition.targets());
                        }
                        else if(scannerTypeOf(method, CtMethod.class))
                        {
                            afterMethodMappings.add(method);
                            afterTargets.put(clazz.getName() + "::" + method.getName() + SignatureUtil.getMethodSignature(method), definition.targets());
                        }
                        else if(scannerTypeOf(method, CtField.class))
                        {
                            afterFieldMappings.add(method);
                            afterTargets.put(clazz.getName() + "::" + method.getName() + SignatureUtil.getMethodSignature(method), definition.targets());
                        }
                    }
                    else
                    {
                        if(scannerTypeOf(method, CtClass.class))
                        {
                            classMappings.add(method);
                            targets.put(clazz.getSimpleName() + "::" + method.getName() + SignatureUtil.getMethodSignature(method), definition.targets());
                        }
                        else if(scannerTypeOf(method, CtMethod.class))
                        {
                            methodMappings.add(method);
                            targets.put(clazz.getSimpleName() + "::" + method.getName() + SignatureUtil.getMethodSignature(method), definition.targets());
                        }
                        else if(scannerTypeOf(method, CtField.class))
                        {
                            fieldMappings.add(method);
                            targets.put(clazz.getSimpleName() + "::" + method.getName() + SignatureUtil.getMethodSignature(method), definition.targets());
                        }
                    }
                }
            }
        }
    }

    private static boolean scannerTypeOf(Method method, Class<?> type) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 1) {
            return type.isAssignableFrom(parameterTypes[0]);
        }
        return false;
    }

    /**
     * Invokes a static void method with a CtClass as the parameter.
     *
     * @param method The static method to be invoked.
     * @param ctClass The CtClass instance to be passed as the parameter.
     */
    private static void callClassScanner(Method method, CtClass ctClass) {
        try {
            // Ensure the method is static
            if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                throw new IllegalArgumentException("The provided method is not static.");
            }

            // Invoke the static void method with the CtClass parameter
            method.invoke(null, ctClass);

        } catch (Exception ignored) {
        }
    }

    /**
     * Invokes a static void method with a CtMethod as the parameter.
     *
     * @param method The static method to be invoked.
     * @param ctMethod The CtMethod instance to be passed as the parameter.
     */
    private static void callMethodScanner(Method method, CtMethod ctMethod) {
        try {
            // Ensure the method is static
            if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                throw new IllegalArgumentException("The provided method is not static.");
            }

            // Invoke the static void method with the CtMethod parameter
            method.invoke(null, ctMethod);

        } catch (Exception ignored) {
            System.err.println("[MappingException] Exception thrown in " + method.getName());
        }
    }

    private static void callFieldScanner(Method method, CtField ctField) {
        try {
            // Ensure the method is static
            if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                throw new IllegalArgumentException("The provided method is not static.");
            }

            // Invoke the static void method with the CtMethod parameter
            method.invoke(null, ctField);

        } catch (Exception ignored) {
        }
    }
}
