/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.orange.net;

/**
 *
 * @author niuyunyun
 */
import com.orange.controller.IController;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingWorker;

public class MulticastListener {

    ScheduledExecutorService mExecutor = Executors.newScheduledThreadPool(1);
    private IController mController;
    public MulticastListener(IController controller)
    {
        mController = controller;
    }
    

    private class StartListenRunable implements Runnable {

        private int mPort;
        private String mIP;

        public StartListenRunable(String ip, int port) {
            mIP = ip;
            mPort = port;
        }

        @Override
        public void run() {
            byte[] data = new byte[256];
            try {
                InetAddress address = InetAddress.getByName(mIP);
                // System.out.print("is multi:" + address.isMulticastAddress());
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

    private class ListenWorker extends SwingWorker<String, Void> {
        //replace with futureTask

        private IController mController = null;
        private String mIP;
        private int mPort;

        ListenWorker(IController controller, String ip, int port) {
            mController = controller;
            mIP = ip;
            mPort = port;
        }

        @Override
        protected String doInBackground() throws Exception {
            String message = "";
            byte[] data = new byte[256];
            try {
                InetAddress address = InetAddress.getByName(mIP);
                // System.out.print("is multi:" + address.isMulticastAddress());
                MulticastSocket ms = new MulticastSocket(mPort);
                ms.joinGroup(address);
                DatagramPacket packet = new DatagramPacket(data, data.length);
                ms.receive(packet);
                message = new String(packet.getData(), 0, packet.getLength());
                ms.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return message;
        }

        protected void done(String msg) {
            mController.onMulticastMsg(msg);
        }
    };

    public class ListenTask extends java.util.TimerTask {
        @Override
        public void run() {
                  String msg = "";
            byte[] data = new byte[256];
            try {
                InetAddress address = InetAddress.getByName(mIP);
                // System.out.print("is multi:" + address.isMulticastAddress());
                MulticastSocket ms = new MulticastSocket(mPort);
                ms.joinGroup(address);
                DatagramPacket packet = new DatagramPacket(data, data.length);
                ms.receive(packet);
                msg = new String(packet.getData(), 0, packet.getLength());
                ms.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            mController.onMulticastMsg(msg);
        }
    }
    private Timer mTimer = new Timer();
    private String mIP;
    private int mPort;
    public void startListen(String ip, int port) {
        mIP = ip;
        mPort = port;
        mTimer.schedule(new ListenTask(), 1000,1000);

       // mExecutor.schedule(new StartListenRunable(ip, port), 0, TimeUnit.MILLISECONDS);
        //mExecutor.scheduleAtFixedRate(new StartListenRunable(ip, port), 2, 1, TimeUnit.SECONDS);
    }
}
