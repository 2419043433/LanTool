package com.orange.interfaces;

import java.util.concurrent.TimeUnit;

public interface ThreadDelegate {
	void post(Runnable runnable);

	void postDelayed(Runnable runnable, long delay, TimeUnit unit);

	void scheduleAtFixedRate(Runnable command, long initialDelay, long period,
			TimeUnit unit);
	ThreadDelegate create();
}
