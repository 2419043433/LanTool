package com.orange.base.thread;

import java.util.HashMap;
import java.util.Map;

import com.orange.interfaces.ThreadDelegate;

public class Threads {
	public enum Type {
		UI, IO_Network, IO_File,
	}

	private static Map<Type, ThreadDelegate> mThreads = new HashMap<Threads.Type, ThreadDelegate>();

	public static void init() {
		mThreads.put(Type.UI, new UIThread());
		mThreads.put(Type.IO_Network, new IOThread());
		mThreads.put(Type.IO_File, new IOThread());
	}

	public static ThreadDelegate forThread(Type type) {
		return mThreads.get(type);
	}
}
