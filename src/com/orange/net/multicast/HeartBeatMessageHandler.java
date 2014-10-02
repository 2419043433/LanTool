package com.orange.net.multicast;

import com.orange.net.interfaces.IMessage;
import com.orange.net.interfaces.IMessageHandler;
import com.orange.net.util.MessageCodecUtil;


public class HeartBeatMessageHandler implements IMessageHandler{

	public interface UIThreadDelegate
	{
		void onHeartBeatMessageReceived(HeartBeatMessage msg);
	}
	
	private UIThreadDelegate mDelegate;
	
	public HeartBeatMessageHandler(UIThreadDelegate delegate)
	{
		mDelegate = delegate;
	}

	private void handleHeartBeatMessageOnUIThread(HeartBeatMessage msg)
	{
		mDelegate.onHeartBeatMessageReceived(msg);
	}

	@Override
	public boolean handleMessage(IMessage msg) {
		HeartBeatMessage message = MessageCodecUtil.convert(msg);
		if(null == message)
		{
			return false;
		}
		handleHeartBeatMessageOnUIThread(message);
		return true;
	}


}
