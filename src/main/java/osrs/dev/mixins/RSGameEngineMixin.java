package osrs.dev.mixins;

import osrs.dev.annotations.mixin.Inject;
import osrs.dev.annotations.mixin.MethodHook;
import osrs.dev.annotations.mixin.Mixin;
import osrs.dev.api.RSGameEngine;

import java.applet.Applet;
import java.util.concurrent.ArrayBlockingQueue;

@Mixin("GameEngine")
public abstract class RSGameEngineMixin extends Applet implements RSGameEngine
{
    @Inject
    public ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1000);
    @Inject
    public static Thread currentThread;
    @Inject
    private static boolean disableRender = false;
    @Inject
    public static boolean shouldExit;

    @MethodHook("run")
    public static boolean runHook()
    {
        currentThread = Thread.currentThread();
        return false;
    }

    @Override
    @Inject
    public Thread getClientThread()
    {
        return currentThread;
    }

    @Override
    @Inject
    public boolean isClientThread()
    {
        return currentThread == Thread.currentThread();
    }

    @Override
    @Inject
    public void setDisableRender(boolean bool)
    {
        disableRender = bool;
    }

    @MethodHook("graphicsTick")
    public boolean gTickHook()
    {
        if(shouldExit)
        {
            this.destroy();
            return false;
        }
        return disableRender;
    }

    @MethodHook("clientTick")
    public boolean clientTick()
    {
        if(queue == null)
        {
            queue = new ArrayBlockingQueue<>(1000);
        }
        return false;
    }

    @Override
    @Inject
    public void addToInvokeQueue(Runnable r)
    {
        queue.add(r);
    }

    @Inject
    @Override
    public void setShouldExit(boolean bool)
    {
        shouldExit = bool;
    }

    @Inject
    @Override
    public boolean getShouldExit()
    {
        return shouldExit;
    }
}
