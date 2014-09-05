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

public class MulticastSender {

    private int mPort;
    private String mIP;

    public MulticastSender(String ip, int port) {
        mIP = ip;
        mPort = port;
    }

    public void send(String data) {
        try {
            InetAddress address = InetAddress.getByName(mIP);
            DatagramPacket packet = new DatagramPacket(data.getBytes(),
                    data.length(), address, mPort);
            MulticastSocket ms = new MulticastSocket();
            ms.send(packet);
            ms.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
