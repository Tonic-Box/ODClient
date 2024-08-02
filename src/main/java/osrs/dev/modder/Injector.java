package osrs.dev.modder;

import javassist.*;
import lombok.SneakyThrows;
import osrs.dev.annotations.Inject;
import osrs.dev.annotations.MethodHook;
import osrs.dev.annotations.Mixin;
import osrs.dev.annotations.Shadow;
import osrs.dev.modder.model.MappedType;
import osrs.dev.modder.model.Mapping;
import osrs.dev.modder.model.Mappings;
import osrs.dev.util.Introspection;

import java.util.HashMap;
import java.util.Map;

public class Injector
{
    private static final String MIXINS = "osrs.dev.mixins";
    private static final String RSAPI = "osrs.dev.api";

    public static void inject()
    {
        HashMap<CtClass,CtClass> pairs = Introspection.getPairs(MIXINS, RSAPI);

        if(pairs.isEmpty())
            return;

        //make the target class extend out interface
        for(Map.Entry<CtClass,CtClass> entry : pairs.entrySet())
        {
            CtClass mixin = entry.getKey();
            try {
                CtClass[] ifaces = mixin.getInterfaces();

                String gamePackTagName = ((Mixin) mixin.getAnnotation(Mixin.class)).value();

                CtClass target = Mappings.getClazz(gamePackTagName);

                if(target != null)
                {
                    for(CtClass clazz : ifaces)
                        if(!Introspection.hasInterface(target, clazz))
                            target.addInterface(clazz);
                }
            }
            catch(Exception ignored)
            {
            }
        }

        //loop through the pairs
        for(Map.Entry<CtClass,CtClass> entry : pairs.entrySet())
        {
            //apply the mixin
            CtClass mixin = entry.getKey();
            CtClass iface = entry.getValue();

            try {
                String gamePackTagName = ((Mixin)mixin.getAnnotation(Mixin.class)).value();

                //check if the targeted class in game pack actually exists
                CtClass target = Mappings.getClazz(gamePackTagName);

                if(target == null)
                {
                    System.out.println("[Skipped] <" + mixin.getName() + "> GamePack class <" + gamePackTagName + "> does not exist.");
                    continue;
                }

                applyMixin(target, mixin, iface);
            }
            catch (Exception ex) {
                System.out.println("[Error] <" + mixin.getName() + "> " + ex.getMessage());
            }

            //Sweet sweet memory reduction
            ClassPool.doPruning = true;
        }
    }

    /**
     * Apply a mixin to a gamepack class
     * @param target the target class in the gamepack
     * @param mixin the mixin class
     * @param iface the interface class
     */
    public static void applyMixin(CtClass target, CtClass mixin, CtClass iface)
    {
        //make sure we are free to modify the target class
        target.defrost();
        mixin.defrost();
        if(iface != null)
            iface.defrost();

        //inject all fields from the mixin into the target class
        for(CtField field : mixin.getDeclaredFields())
        {
            target.defrost();
            processField(target, field);
        }

        //inject all methods from the mixin into the target class
        for(CtMethod method : mixin.getDeclaredMethods())
        {
            try
            {
                target.defrost();
                processMethod(target, method);
            }
            catch (Exception ignored)
            {
                ignored.printStackTrace();
            }
        }

        //defrost
        target.defrost();
    }

    @SneakyThrows
    private static void processField(CtClass target, CtField field)
    {
        target.defrost();
        if(Introspection.has(field, Inject.class))
        {
            String fieldModifiers = Modifier.toString(field.getModifiers());
            String fieldType = field.getType().getName();
            String fieldName = field.getName();
            CtField newField = CtField.make(fieldModifiers + " " + fieldType + " " + fieldName + ";", target);
            target.addField(newField);
        }


    }

    private static void processMethod(CtClass target, CtMethod method) throws Exception {
        target.defrost();
        if(Introspection.has(method, Inject.class))
        {
            injectMethod(target, method);
        }

        if(Introspection.has(method, MethodHook.class))
        {
            methodHook(target, method);
        }

        if(Introspection.has(method, Shadow.class))
        {
            shadow(target, method);
        }
    }

    private static void shadow(CtClass target, CtMethod method) throws Exception
    {
        target.defrost();
        String targName = ((Shadow) method.getAnnotation(Shadow.class)).value();
        boolean isMethod = ((Shadow) method.getAnnotation(Shadow.class)).method();

        if(isMethod)
        {
            //NYI
        }
        else
        {
            Mapping mapping = Mappings.findByTag(targName);
            if(!mapping.getType().equals(MappedType.FIELD))
                return;

            CtMethod insert;
            String clazz = mapping.getObfuscatedClass().equals(target.getName()) ? "" : mapping.getObfuscatedClass();
            String body = "{ return " + clazz + "." + mapping.getObfuscatedName() + "; }";
            if(Modifier.isStatic(method.getModifiers()))
            {
                insert = CtNewMethod.make("public static " + method.getReturnType().getName() + " " + method.getName() + "()" + body, target);
            }
            else
            {
                insert = CtNewMethod.make("public " + method.getReturnType().getName() + " " + method.getName() + "()" + body, target);
            }

            target.addMethod(insert);
        }
    }

    /**
     * disable (no-op) a method
     *
     * @param target target class
     * @param method abstract method or boolean method with logic to chose to disable or not
     */
    public static void methodHook(CtClass target, CtMethod method) throws Exception
    {
        target.defrost();
        String disableName = ((MethodHook) method.getAnnotation(MethodHook.class)).value();
        Mapping entry = Mappings.findByTag(disableName);

        CtMethod targetMethod = null;
        for(CtMethod elem : target.getDeclaredMethods())
        {
            if(!elem.getName().equals(entry.getObfuscatedName()))
                continue;

            if(!elem.getMethodInfo2().getDescriptor().equals(entry.getDescriptor()))
                continue;

            targetMethod = elem;
            break;
        }

        injectMethod(target, method);
        target.defrost();

        String ret = getReturn(method);

        StringBuilder args = new StringBuilder("(");

        int length = method.getParameterTypes().length;
        for(int i = 1; i <= length; i++)
        {
            args.append("$").append(i).append(",");
        }

        if(args.toString().endsWith(","))
        {
            args = new StringBuilder(args.substring(0, args.length() - 1));
        }

        targetMethod.insertBefore("if(" + method.getName() + args.toString() + ")) { return " + ret + "; }");
    }

    public static void injectMethod(CtClass target, CtMethod method) throws Exception
    {
        target.defrost();
        String spr = Introspection.getSuper(target);
        if (!spr.equals("SuperDoesExist"))
        {
            CtClass superClass = Mappings.getClazz(spr);
            if (superClass == null)
                return;
            superClass.defrost();
            CtClass cc = target.getClassPool().makeClass(superClass.getClassFile());
            cc.toClass(target.getClass().getClassLoader(), target.getClass().getProtectionDomain());
        }
        CtMethod newMethod = CtNewMethod.copy(
                method,
                target, null);
        target.addMethod(newMethod);
    }

    private static String getReturn(CtMethod method) throws Exception
    {
        String ret = "";

        if(method.getReturnType().getName().equals("void"))
        {
            return "";
        }

        if(method.getReturnType().isPrimitive())
        {
            switch (method.getReturnType().getName())
            {
                case "int":
                    ret = "0";
                    break;
                case "boolean":
                    ret = "true";
                    break;
                case "long":
                    ret = "0L";
                    break;
                case "double":
                    ret = "0.0D";
                    break;
                case "float":
                    ret = "0.0F";
                    break;
                case "short":
                    ret = "(short) 0";
                    break;
                case "byte":
                    ret = "(byte) 0";
                    break;
                case "char":
                    ret = "(char) 0";
                    break;
            }
        }
        else
        {
            ret = "null";
        }
        return ret;
    }
}
