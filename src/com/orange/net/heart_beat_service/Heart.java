package com.orange.net.heart_beat_service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import com.orange.net.MessageEncoder;
import com.orange.system.SystemInfo;
import com.orange.system.SystemInfo.Keys;

public class Heart
{
    private InetAddress mAddress;
    private DatagramPacket mPacket;
    private MulticastSocket mSocket;
    private MessageEncoder mMessageEncoder = new MessageEncoder();

    public Heart(int port, int controlPort)
    {
        try
        {
            mAddress = InetAddress.getLocalHost();
            HeartBeatMessage heartBeatMessage = new HeartBeatMessage(mAddress, SystemInfo.getInstance().getString(Keys.GUID), controlPort);
            byte[] bytes = mMessageEncoder.message(heartBeatMessage).encode();
            mPacket = new DatagramPacket(bytes, bytes.length, mAddress, port);
            mSocket = new MulticastSocket();
        }
        catch (Exception e)
        {
            if (null != mSocket)
            {
                mSocket.close();
            }
            e.printStackTrace();
        }
    }

    public void startBeat()
    {
        try
        {
            mSocket.send(mPacket);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void stopBeat()
    {
        mSocket.close();
    }
}
