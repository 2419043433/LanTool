package com.orange.net;

import com.orange.net.interfaces.IEncoder;

public class FrameEncoder implements IEncoder {

	@Override
	public byte[] encode() {
		// TODO Auto-generated method stub
		return null;
	}
//	 IEncoder mMessageEncoder;
//	 private ByteBuffer mBuffer;
//	
//	 public FrameEncoder(IEncoder messageEncoder) {
//	 mMessageEncoder = messageEncoder;
//	 }
//	
//	 @Override
//	 public byte[] encode() {
//	 int length = getEstimateLength();
//	 if(null == mBuffer || length > mBuffer.capacity())
//	 {
//	 mBuffer = ByteBuffer.allocateDirect(length);
//	 }
//	 mBuffer.clear();
//	
//	 return mBuffer.array();
//	 }

}
