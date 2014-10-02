package com.orange.base.thread;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.orange.interfaces.ThreadDelegate;

public class IOThread implements ThreadDelegate {
	ScheduledExecutorService mExecutor;

	public IOThread() {
		mExecutor = Executors.newScheduledThreadPool(1);
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
