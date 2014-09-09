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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MulticastSender {

    ScheduledExecutorService mExecutor = Executors.newScheduledThreadPool(1);

    public MulticastSender() {
    }

    private class HeartBeatRunable implements Runnable {

        private int mPort;
        private String mIP;
        private String mData;
        private InetAddress mAddress;

        public HeartBeatRunable(String ip, int port, String data) {
            mIP = ip;
            mPort = port;
            mData = data;
            try {
                mAddress = InetAddress.getByName(mIP);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                DatagramPacket packet = new DatagramPacket(mData.getBytes(),
                        mData.length(), mAddress, mPort);
                MulticastSocket ms = new MulticastSocket();
                ms.send(packet);
                ms.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void startHeartBeatMulticast(String ip, int port, String data) {
        mExecutor.scheduleAtFixedRate(new HeartBeatRunable(ip, port, data), 2, 1, TimeUnit.SECONDS);
    }
}
