package osrs.dev.modder;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import osrs.dev.modder.model.Garbage;
import osrs.dev.modder.model.Mapping;
import osrs.dev.modder.model.Mappings;
import osrs.dev.util.Pair;

import java.util.Map;

public class FieldHookInstrumenter
{
    public static void run()
    {
        try
        {
            for(Map.Entry<Mapping, Pair<String,String>> entry : Injector.getFieldWriteHooks().entrySet())
            {
                try
                {
                    CtField field = Mappings.getClazz(entry.getKey().getObfuscatedClass()).getDeclaredField(entry.getKey().getObfuscatedName());
                    CtClass target = Mappings.getClazz(entry.getValue().getKey());
                    CtMethod method = target.getDeclaredMethod(entry.getValue().getValue());
                    target.defrost();
                    //if(Settings.isDevMode())
                    System.out.println("[FieldHook] " + target.getName() + " | " + field.getDeclaringClass().getName() + "." + field.getName() + " -> " + method.getName());
                    for (CtClass clazz : Mappings.getClasses())
                    {
                        clazz.defrost();
                        for(CtBehavior behavior : clazz.getDeclaredBehaviors())
                        {
                            if(!Modifier.isAbstract(behavior.getModifiers()) && behavior.getName().length() < 4)
                            {
                                if(!field.getType().getName().contains("["))
                                    instrumentWriteAccess(behavior, method, field, entry.getKey());
                                else {
                                    //doArrays2(entry, behavior);
                                    //insertHook(behavior, field, method);
                                }
                            }
                        }
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    private static <T extends CtBehavior> void instrumentWriteAccess(T target, CtMethod setter, CtField field, Mapping entry) throws CannotCompileException
    {
        try
        {
            target.instrument(new ExprEditor() {
                public void edit(FieldAccess fieldAccess) throws CannotCompileException {
                    String fieldName = fieldAccess.getFieldName();
                    String clazzName = fieldAccess.getClassName();
                    CtClass clazz = Mappings.getClazz(clazzName);

                    if(!fieldName.equals(field.getName()))
                        return;

                    if(!clazzName.equals(field.getDeclaringClass().getName()) && !clazz.subclassOf(field.getDeclaringClass()))
                        return;

                    String garbVal = Garbage.getGarbageGetter(entry);

                    if (fieldAccess.isWriter() && !field.getFieldInfo().getDescriptor().contains("[") && !entry.isFieldHookAfter())
                    {
                        String setterMethodCall;
                        if(Modifier.isStatic(setter.getModifiers()))
                            setterMethodCall = "if(!" + setter.getDeclaringClass().getName() + "." + setter.getName() + "($1" + garbVal + ")) { $0." + fieldName + " = $1; }";
                        else
                            setterMethodCall = "if(!$0." + setter.getName() + "($1" + garbVal + ")) { $0." + fieldName + " = $1; }";

                        fieldAccess.replace(setterMethodCall);
                    }
                    else if(fieldAccess.isWriter() && !field.getFieldInfo().getDescriptor().contains("[") && entry.isFieldHookAfter())
                    {
                        String setterMethodCall;
                        if(Modifier.isStatic(setter.getModifiers()))
                            setterMethodCall = "$0." + fieldName + " = $1; " + setter.getDeclaringClass().getName() + "." + setter.getName() + "($1" + garbVal + ");";
                        else
                            setterMethodCall = "$0." + fieldName + " = $1; $0." + setter.getName() + "($0." + fieldName + garbVal + ");";
                        fieldAccess.replace(setterMethodCall);
                    }
                }
            });
        }
        catch (Exception ex)
        {
            System.out.println("> " + setter.getLongName() + " / " + target.getLongName());
            ex.printStackTrace();
            System.exit(0);
        }
    }
}
