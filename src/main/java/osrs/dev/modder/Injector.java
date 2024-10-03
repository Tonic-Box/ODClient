package osrs.dev.modder;

import javassist.*;
import lombok.Getter;
import lombok.SneakyThrows;
import osrs.dev.annotations.mixin.*;
import osrs.dev.modder.model.Garbage;
import osrs.dev.modder.model.MappedType;
import osrs.dev.modder.model.Mapping;
import osrs.dev.modder.model.Mappings;
import osrs.dev.modder.model.source.MethodBuilder;
import osrs.dev.util.Introspection;
import osrs.dev.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class Injector
{
    private static final String MIXINS = "osrs.dev.mixins";
    private static final String RSAPI = "osrs.dev.api";
    @Getter
    private static final Map<Mapping, Pair<String,String>> fieldWriteHooks = new HashMap<>();

    /**
     * Takes in outr mixins and uses them as instructions for modifying the gamepack
     */
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

        //Instrument field hooks
        FieldHookInstrumenter.run();
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
        try
        {
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

            if(Introspection.has(method, Replace.class))
            {
                replace(target, method);
            }

            if(Introspection.has(method, FieldHook.class))
            {
                fieldHook(target, method);
            }
        }
        catch (Exception ex)
        {
            System.out.println("[InjectorException] " + method.getLongName());
            ex.printStackTrace();
        }

    }

    public static void fieldHook(CtClass target, CtMethod method)
    {
        try
        {
            String name = ((FieldHook) method.getAnnotation(FieldHook.class)).value();
            boolean after = ((FieldHook) method.getAnnotation(FieldHook.class)).after();
            Mapping field = Mappings.findByTag(name);
            field.setFieldHookAfter(after);
            CtMethod hookMethod = CtNewMethod.copy(method, target, null);
            target.addMethod(hookMethod);
            fieldWriteHooks.put(field, new Pair<>(target.getName(), hookMethod.getName()));
        }
        catch (Exception ex)
        {
            System.out.println("[@FieldHook] " + method.getLongName());
            ex.printStackTrace();
            //System.exit(0);
        }
    }

    private static void replace(CtClass target, CtMethod method) throws Exception
    {
        String replaceName = ((Replace) method.getAnnotation(Replace.class)).value();
        Mapping mapping = Mappings.findByTag(replaceName);

        CtMethod origMethod = mapping.getMethod();

        CtClass[] origParams = origMethod.getParameterTypes();
        CtClass[] replaceParams = method.getParameterTypes();

        if (origParams.length != replaceParams.length)
        {
            if (replaceParams.length != (origParams.length - 1) && method.getParameterTypes().length != 0)
            {
                System.out.println("<Mixin> Failed to instrument replacement method '" + method.getName() + "'");
                return;
            }
            if(method.getParameterTypes().length == 0)
            {
                for(CtClass ctClass : origParams)
                {
                    method.addParameter(ctClass);
                }
            }
            else
                method.addParameter(origParams[origParams.length - 1]);
        }

        CtMethod newMethod = CtNewMethod.copy(method, target, null);
        target.addMethod(newMethod);

        if (origMethod.getReturnType().getName().equals("void"))
        {
            origMethod.insertBefore("if(true) {" + newMethod.getName() + "($$); return;}");
        } else
        {
            origMethod.insertBefore("if(true) { return (" + origMethod.getReturnType().getName() + ") " + newMethod.getName() + "($$);}");
        }
    }

    private static void shadow(CtClass target, CtMethod method) throws Exception
    {
        target.defrost();
        String targName = ((Shadow) method.getAnnotation(Shadow.class)).value();
        boolean isMethod = ((Shadow) method.getAnnotation(Shadow.class)).method();
        Mapping mapping = Mappings.findByTag(targName);

        if(isMethod)
        {
            if(!mapping.getType().equals(MappedType.METHOD))
                return;

            CtMethod targetMethod = mapping.getMethod();
            if(targetMethod == null)
                return;

            StringBuilder callParams = new StringBuilder();
            StringBuilder methodParams = new StringBuilder();
            if(targetMethod.getParameterTypes() != null)
            {
                int i;
                for(i = 0; i < method.getParameterTypes().length; i++)
                {
                    methodParams.append(",").append(targetMethod.getParameterTypes()[i].getName()).append(" var").append(i);
                    callParams.append(",(").append(targetMethod.getParameterTypes()[i].getName()).append(")").append("var").append(i);
                }

                if(mapping.getGarbage() != null)
                {
                    callParams.append(",(").append(targetMethod.getParameterTypes()[i].getName()).append(")").append(mapping.getGarbage().getValue());
                }

                if(methodParams.toString().startsWith(","))
                    methodParams = new StringBuilder(methodParams.substring(1));
                if(callParams.toString().startsWith(","))
                    callParams = new StringBuilder(callParams.substring(1));
            }

            String clazz = mapping.getObfuscatedClass().equals(target.getName()) ? "" : mapping.getObfuscatedClass() + ".";

            //craft method we can access with built-in garb that targets out instrumented pair
            String returnType = method.getReturnType().getName();
            String accessMethod = new MethodBuilder()
                    .generateFromTemplate(method)
                    .Public()
                    .noModifier()
                    .notFinal()
                    .withName(method.getName())
                    .withArgs(methodParams.toString())
                    .withBody("{ " + (returnType.equals("void") ? "" : "return ") + clazz + targetMethod.getName() + "(" + callParams + "); }")
                    .get();

            System.out.println(accessMethod);
            CtMethod accessible = CtNewMethod.make(accessMethod, target);
            target.addMethod(accessible);
        }
        else
        {
            if(!mapping.getType().equals(MappedType.FIELD))
                return;

            CtMethod insert;
            String clazz = mapping.getObfuscatedClass().equals(target.getName()) ? "" : mapping.getObfuscatedClass() + ".";

            if(method.getParameterTypes().length != 0)
            {
                String garbage = Garbage.getGarbageSetter(mapping);
                String accessMethod = new MethodBuilder()
                        .generateFromTemplate(method)
                        .Public()
                        .noModifier()
                        .notFinal()
                        .withName(method.getName())
                        .withArgs(method.getParameterTypes()[0].getName() + " var1")
                        .withBody("{ " + clazz + mapping.getObfuscatedName() + "=var1 " + garbage + "; }")
                        .get();

                insert = CtNewMethod.make(accessMethod, target);
            }
            else
            {
                String garbage = Garbage.getGarbageGetter(mapping);
                String body = "{ return " + clazz + mapping.getObfuscatedName() + garbage + "; }";
                if(Modifier.isStatic(method.getModifiers()))
                {
                    insert = CtNewMethod.make("public static " + method.getReturnType().getName() + " " + method.getName() + "()" + body, target);
                }
                else
                {
                    insert = CtNewMethod.make("public " + method.getReturnType().getName() + " " + method.getName() + "()" + body, target);
                }
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
        try
        {
            target.defrost();
            String disableName = ((MethodHook) method.getAnnotation(MethodHook.class)).value();
            Mapping entry = Mappings.findByTag(disableName);

            CtMethod targetMethod = null;
            CtClass _class = Mappings.getClazz(entry.getObfuscatedClass());
            for(CtMethod elem : _class.getDeclaredMethods())
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
                args.setLength(args.length() - 1);
            }

            String caller = method.getName() + args + ")";
            if(!target.getName().equals(_class.getName()))
            {
                caller = target.getName() + "." + caller;
            }

            targetMethod.insertBefore("if(" + caller + ") { return " + ret + "; }");
        }
        catch (Exception ex)
        {
            System.out.println(method.getLongName());
            ex.printStackTrace();

        }
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
