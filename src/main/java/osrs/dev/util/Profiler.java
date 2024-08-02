package osrs.dev.util;

import java.time.Duration;
import java.time.Instant;

public class Profiler
{
    private String task;
    private Instant startup;

    /**
     * start the profiler timer
     * @param task identifier of task we are profiling
     */
    public static Profiler start(String task)
    {
        Profiler profiler = new Profiler();
        profiler.PStart(task);
        return profiler;
    }

    /**
     * internal start()
     * @param task task name
     */
    private void PStart(String task)
    {
        this.task = task;
        startup = Instant.now();
    }

    /**
     * internal stop()
     */
    public void stop()
    {
        Logger.info("[" + task + "] Took " + Duration.between(startup, Instant.now()).getSeconds() + " seconds.");
        System.out.println("[" + task + "] Took " + Duration.between(startup, Instant.now()).getSeconds() + " seconds.");
    }

    public void stopMS()
    {
        Logger.info("[" + task + "] Took " + Duration.between(startup, Instant.now()).toMillis() + " ms.");
        System.out.println("[" + task + "] Took " + Duration.between(startup, Instant.now()).toMillis() + " ms.");
    }
}
