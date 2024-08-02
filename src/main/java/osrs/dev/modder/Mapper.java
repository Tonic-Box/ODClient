package osrs.dev.modder;

import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.bytecode.MethodInfo;
import osrs.dev.modder.model.Mappings;

public class Mapper
{

    public static void map()
    {
        for(CtClass clazz : Mappings.getClasses())
        {
            clazz.defrost();
            findClient(clazz);
            findClientField(clazz);
            findDoAction(clazz);
        }
    }

    private static void findClientField(CtClass clazz)
    {
        for(CtField field : clazz.getDeclaredFields())
        {
            try
            {
                if(field.getType().getName().equals("client"))
                {
                    Mappings.addField("clientField", field.getName(), field.getDeclaringClass().getName(), field.getFieldInfo2().getDescriptor(), field.getModifiers());
                    return;
                }
            }
            catch (Exception ignored) {}
        }
    }

    private static void findClient(CtClass clazz)
    {
        try
        {
            if(!clazz.getName().equals("client"))
                return;
            Mappings.addClass("Client", clazz.getName());

            CtClass gameEngine = clazz.getSuperclass();
            Mappings.addClass("GameEngine", gameEngine.getName());
            findGraphicsTick(gameEngine);
        }
        catch (Exception ignored) {
        }

    }

    private static void findGraphicsTick(CtClass clazz) throws Exception
    {
        for(CtMethod method : clazz.getDeclaredMethods())
        {
            int mod = method.getModifiers();
            if(Modifier.isAbstract(mod))
                continue;

            if(!method.getReturnType().getName().equals("void"))
                continue;

            if(Modifier.isStatic(mod))
                continue;

            if(method.getParameterTypes().length != 1)
                continue;

            int length = method.getMethodInfo2().getCodeAttribute().getCodeLength();
            if(length < 420 || length > 475)
                continue;

            Mappings.addMethod("graphicsTick", method.getName(), method.getDeclaringClass().getName(), method.getMethodInfo2().getDescriptor(), method.getModifiers());
        }
    }

    private static void findDoAction(CtClass clazz)
    {
        for(CtMethod method : clazz.getDeclaredMethods())
        {
            MethodInfo info = method.getMethodInfo2();
            if(!info.getDescriptor().startsWith("(IIIIIILjava/lang/String;Ljava/lang/String;II") || !info.getDescriptor().endsWith(")V"))
                continue;

            if(info.getCodeAttribute().getCodeLength() < 5000)
                continue;

            if(method.getName().length() > 2)
                continue;

            Mappings.addMethod("menuAction", method.getName(), clazz.getName(), info.getDescriptor(), method.getModifiers());
        }
    }
}
