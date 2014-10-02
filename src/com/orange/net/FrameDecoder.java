package com.orange.net;

import java.nio.ByteBuffer;

import com.orange.net.interfaces.IStreamDecoder;
/*
 * [ length | frame  |
 * |-- 4 ---|-length-|
 */
public class FrameDecoder implements IStreamDecoder {

	ByteBuffer mReceiveBuffer;
	private IStreamDecoder mNextHandler;

	public FrameDecoder(IStreamDecoder nextHandler) {
		mNextHandler = nextHandler;
		// Currently set the buffer size as 1M, The buffer size should larger
		// than the largest
		// message size
		mReceiveBuffer = ByteBuffer.wrap(new byte[1024 * 1024]);
	}

	@Override
	public void decode(byte[] buffer, int offset, int length) {
		// length
		mReceiveBuffer.put(buffer, 0, length);
		mReceiveBuffer.flip();
		while (true) {
			if (mReceiveBuffer.remaining() < 4) {
				break;
			}

			mReceiveBuffer.mark();
			int messageLength = mReceiveBuffer.getInt();
			if (mReceiveBuffer.remaining() < messageLength) {
				mReceiveBuffer.reset();
				break;
			}
			
			if(null != mNextHandler)
			{
				mNextHandler.decode(mReceiveBuffer.array(), mReceiveBuffer.position(), messageLength);
			}
			
			mReceiveBuffer.position(mReceiveBuffer.position() + messageLength);
		}
		
		mReceiveBuffer.compact();
	}


}