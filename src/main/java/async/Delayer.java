package async;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * complete future delay scheduler
 * jdk8 CompletableFuture.delayedExecutor is not available,
 * so we use this class to simulate the delay
 * @see java.util.concurrent.CompletableFuture#delayedExecutor(long, TimeUnit)
 */
public class Delayer {
    private Delayer(){}

    static final class DaemonThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("CompletableFutureDelayScheduler");
            return t;
        }
    }

    private static final ScheduledThreadPoolExecutor delayer;

    static {
        (delayer = new ScheduledThreadPoolExecutor(
                1, new DaemonThreadFactory())).
                setRemoveOnCancelPolicy(true);
    }

    public static ScheduledFuture<?> delay(Runnable command, long delay,
                                    TimeUnit unit) {
        return delayer.schedule(command, delay, unit);
    }

    public static ScheduledThreadPoolExecutor getInstance() {
        return delayer;
    }
}
