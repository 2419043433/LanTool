package com.orange.base.thread;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.orange.interfaces.ThreadDelegate;

public class IOThreadPool implements ThreadDelegate {
	ScheduledThreadPoolExecutor mExecutor;

	public IOThreadPool() {
		mExecutor = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1);
		mExecutor.setMaximumPoolSize(1);
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
		return new IOThreadPool();
	}
}
