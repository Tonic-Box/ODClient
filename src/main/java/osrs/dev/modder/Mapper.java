package osrs.dev.modder;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import java.util.HashMap;
import java.util.Map;

public class Mapper
{
    private static final Map<String,String> classMapping = new HashMap<>();
    private static final Map<String,String> fieldMapping = new HashMap<>();
    private static final Map<String,MethodMapping> methodMapping = new HashMap<>();

    public static void map()
    {
        for(CtClass clazz : Modder.getClasses())
        {
            clazz.defrost();
            findClient(clazz);
        }
    }

    private static void findClient(CtClass clazz)
    {
        try
        {
            if(!clazz.getName().equals("client"))
                return;

            classMapping.put("Client", clazz.getName());

            CtClass gameEngine = clazz.getSuperclass();
            classMapping.put("GameEngine", gameEngine.getName());
            findGraphicsTick(gameEngine);
        }
        catch (Exception ignored) {
            ignored.printStackTrace();
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

            methodMapping.put("graphicsTick", new MethodMapping("graphicsTick", method.getName(), clazz.getName(), method.getSignature(), -1));
        }
    }
}
