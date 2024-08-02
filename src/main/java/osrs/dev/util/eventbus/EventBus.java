package osrs.dev.util.eventbus;

import osrs.dev.annotations.Subscribe;
import osrs.dev.util.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventBus
{
    private static final Map<Object, List<Method>> subscribers = new ConcurrentHashMap<>();

    public static void register(Object obj)
    {
        if (!subscribers.containsKey(obj))
            subscribers.put(obj, new ArrayList<>());

        for (Method method : obj.getClass().getMethods())
        {
            if (!method.isAnnotationPresent(Subscribe.class))
                continue;

            method.setAccessible(true);

            if (!subscribers.get(obj).contains(method))
                subscribers.get(obj).add(method);
        }
    }

    public static void unregister(Object obj)
    {
        subscribers.remove(obj);
    }

    public static void post(Object sender, Object event)
    {
        for (Object key : subscribers.keySet())
        {
            for(Method method : subscribers.get(key))
            {
                if (method.getParameterTypes().length != 2)
                    continue;

                if (!method.getParameterTypes()[1].equals(event.getClass()))
                    continue;

                try
                {
                    method.invoke(key, sender, event);
                }
                catch (Exception ex)
                {
                    Logger.error(ex.getMessage(), ex);
                }
            }
        }
    }
}
