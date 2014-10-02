package com.orange.net.interfaces;

import java.net.DatagramPacket;

public interface IDatagramPacketDecoder {
	void decode(final DatagramPacket packet);
}
