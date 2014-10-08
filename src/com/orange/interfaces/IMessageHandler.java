package com.orange.interfaces;

import com.orange.base.Params;

public interface IMessageHandler {
	boolean handleMessage(MessageId id, Params param, Params result);
}
