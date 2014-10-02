package com.orange.net.interfaces;

public interface IMessageHandler {
	/*
	 * return true if the specified msg is handled
	 */
	boolean handleMessage(IMessage msg);
}
