/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.orange.controller;

import com.orange.net.MulticastListener;
import com.orange.net.MulticastSender;
import com.orange.ui.UIManager;
import java.net.InetAddress;

/**
 *
 * @author niuyunyun
 */
public class Controller implements IController {

    private static final String kIP = "224.0.0.0";
    private static final int kPort = 5000;
    private MulticastSender mMulticastSender = new MulticastSender();
    private MulticastListener mMulticastListener = new MulticastListener(this);
    private UIManager mUIManager;

    public Controller() {
        mUIManager = new UIManager(this);
    }

    public void start() {
        mMulticastListener.startListen(kIP, kPort);
        String name = "";
        String ip = "";
        try {
            name = InetAddress.getLocalHost().getHostName();
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String text = "[name:" + name + "][ip:" + ip + "]";
        mMulticastSender.startHeartBeatMulticast(kIP, kPort, text);

        mUIManager.start();
    }

    @Override
    public void onMulticastMsg(String msg) {
        mUIManager.onMulticastMsg(msg);
    }
}
