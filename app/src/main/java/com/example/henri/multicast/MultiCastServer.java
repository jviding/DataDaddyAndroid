package com.example.henri.multicast;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by Jasu on 20.2.2016.
 */
public class MulticastServer {

    MulticastSocket socket;
    Thread thread;
    private final int port;
    private final String multicastGroup;

    private final ItemAdded itemAdded;
    public interface ItemAdded {
        void itemAdded(String addr);
    }

    MulticastServer(int port, String multicastGroup, ItemAdded itemAdded) {
        this.port = port;
        this.multicastGroup = multicastGroup;
        this.itemAdded = itemAdded;
        try {
            socket = new MulticastSocket(port);
            socket.setLoopbackMode(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.joinGroup(InetAddress.getByName(multicastGroup));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!thread.isInterrupted()) {
                    runServer();
                }
            }
        });
        thread.start();
    }

    public void stop() {
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void join() {
        Log.d("Join","Joining Multicast Group.");
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramPacket packet;
                byte[] joinMsg = new byte[1];
                joinMsg[0] = 1;
                try {
                    socket.send(new DatagramPacket(joinMsg, joinMsg.length, InetAddress.getByName(multicastGroup), port));
                    Log.d("Join", "Finding users.");
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void runServer() {
        // receive packet
        DatagramPacket packet;
        byte[] buffer = new byte[4096];
        packet = new DatagramPacket(buffer, 4096);
        try {
            socket.receive(packet);
            if (packet.getData()[0] == 1) {
                respond(packet.getAddress());
                Log.d("Join", "A new user has joined.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Store sender to arrayList
        itemAdded.itemAdded(packet.getAddress().toString());
    }

    private void respond(InetAddress addr) {
        byte[] resp = new byte[1];
        resp[0] = 0;
        try {
            socket.send(new DatagramPacket(resp, resp.length, addr, port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
