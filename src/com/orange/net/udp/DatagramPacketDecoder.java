package com.orange.net.udp;

import java.net.DatagramPacket;

import com.orange.net.interfaces.IDatagramPacketDecoder;
import com.orange.net.interfaces.IStreamDecoder;

/*
 * #DatagramPacketHandler use to handle UDP #DatagramPacket, currently it
 * only get stream from #DatagramPacket and pass stream to inner stream handler.
 */
public class DatagramPacketDecoder implements IDatagramPacketDecoder{

	private IStreamDecoder mStreamHandler;
	public DatagramPacketDecoder(IStreamDecoder streamHandler)
	{
		mStreamHandler = streamHandler;
	}
	@Override
	public void decode(DatagramPacket packet) {
		byte[] data = packet.getData();
		int length = packet.getLength();
		assert(null != mStreamHandler);
		mStreamHandler.decode(data, 0, length);
	}

}
