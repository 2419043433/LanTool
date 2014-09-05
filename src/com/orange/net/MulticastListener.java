/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.orange.net;

/**
 *
 * @author niuyunyun
 */
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastListener {

    private int mPort;
    private String mIP;

    public MulticastListener(String ip, int port) {
        mIP = ip;
        mPort = port;
    }

    public void listen() {
        byte[] data = new byte[256];
        try {
            InetAddress address = InetAddress.getByName(mIP);
            System.out.print("is multi:" + address.isMulticastAddress());
            MulticastSocket ms = new MulticastSocket(mPort);
            ms.joinGroup(address);
            DatagramPacket packet = new DatagramPacket(data, data.length);
            ms.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            System.out.println(message);
            ms.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
