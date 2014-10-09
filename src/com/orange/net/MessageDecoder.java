package com.orange.net;

import java.util.HashMap;
import java.util.Map;

import com.orange.net.interfaces.IMessage;
import com.orange.net.interfaces.IMessageHandler;
import com.orange.net.interfaces.IStreamDecoder;
import com.orange.net.util.MessageCodecUtil;

public class MessageDecoder implements IStreamDecoder {

	private Map<String, IMessageHandler> mHandlers;

	public MessageDecoder() {
		mHandlers = new HashMap<String, IMessageHandler>();
	}
	
	public void add(String key, IMessageHandler handler)
	{
		mHandlers.put(key, handler);
	}
	
	public void remove(String key)
	{
		mHandlers.remove(key);
	}

	@Override
	public void decode(byte[] buffer, int offset, int length) {
		System.out.println("messagelen:" + length);
		IMessage message = MessageCodecUtil.readMessage(buffer, offset, length);
		assert (null != message);
		if(null == message)
		{
			return;
		}
		String name = message.getClass().getName();
		IMessageHandler handler = mHandlers.get(name);
		assert(null != handler);
		handler.handleMessage(message);
	}

}
