package com.orange.net;

import com.orange.net.interfaces.IEncoder;
import com.orange.net.interfaces.IMessage;
import com.orange.net.util.MessageCodecUtil;

public class MessageEncoder implements IEncoder
{
    private IMessage mMessage;

    public MessageEncoder message(IMessage message)
    {
        mMessage = message;
        return this;
    }

    @Override
    public byte[] encode()
    {
        assert (null != mMessage);
        byte[] bytes = MessageCodecUtil.writeMessage(mMessage);
        mMessage = null;
        return bytes;
    }

}
