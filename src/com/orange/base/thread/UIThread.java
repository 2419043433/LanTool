package com.orange.base.thread;

import java.lang.reflect.InvocationTargetException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import com.orange.interfaces.ThreadDelegate;

public class UIThread implements ThreadDelegate {
	Timer mTimer;

	public UIThread() {
		mTimer = new Timer();
	}

	private class ExecutedInUIThreadTask extends TimerTask {
		private Runnable mInnerRunnable;

		ExecutedInUIThreadTask(Runnable runnable) {
			mInnerRunnable = runnable;
		}

		@Override
		public void run() {
			try {
				SwingUtilities.invokeAndWait(mInnerRunnable);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}

	}

	public void postDelayed(Runnable runnable, long delay, TimeUnit unit) {
		mTimer.schedule(new ExecutedInUIThreadTask(runnable),
				unit.toMillis(delay));
	}

	@Override
	public void post(Runnable runnable) {
		try {
			SwingUtilities.invokeAndWait(runnable);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void scheduleAtFixedRate(Runnable runnable, long initialDelay,
			long period, TimeUnit unit) {
		mTimer.scheduleAtFixedRate(new ExecutedInUIThreadTask(runnable),
				initialDelay, unit.toMicros(period));
	}

	@Override
	public ThreadDelegate create() {
		throw new UnsupportedOperationException(
				"UIThread should never be created");
	}
}
