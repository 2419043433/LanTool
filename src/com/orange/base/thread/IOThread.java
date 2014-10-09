package com.orange.base.thread;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.orange.interfaces.ThreadDelegate;

public class IOThread implements ThreadDelegate {
	ScheduledExecutorService mExecutor;
    /**
     * The default thread factory
     */
    static class DefaultThreadFactoryEx implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactoryEx() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                                  Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" +
                          poolNumber.getAndIncrement() +
                         "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                                  namePrefix + threadNumber.getAndIncrement(),
                                  0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            
        	System.out.println("create new thread:[id:" + t.getId() + "][name:" + t.getName() + "]");
            return t;
        }
    }
	public IOThread() {
		mExecutor = Executors.newSingleThreadScheduledExecutor(new DefaultThreadFactoryEx());
	}

	public void postDelayed(Runnable runnable, long delay, TimeUnit unit) {
		mExecutor.schedule(runnable, delay, unit);
	}

	@Override
	public void post(Runnable runnable) {
		mExecutor.execute(runnable);
	}

	@Override
	public void scheduleAtFixedRate(Runnable command, long initialDelay,
			long period, TimeUnit unit) {
		mExecutor.scheduleAtFixedRate(command, initialDelay, period, unit);
	}

	@Override
	public ThreadDelegate create() {
		return new IOThread();
	}
}
