package com.example.henri.multicast;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Jasu on 21.2.2016.
 */
public class MulticastService extends AppCompatActivity {

    private WifiManager.MulticastLock multicastLock;
    private MulticastServer server;

    private final String multicastGroup;
    private final int port;

    private ItemAdded itemAdded;
    public interface ItemAdded {
        void itemAdded(String addr);
    }

    public MulticastService(final String multicastGroup, final int port) {
        this.multicastGroup = multicastGroup;
        this.port = port;
    }

    public void setCallbackForNewUser(ItemAdded itemAdded) {
        this.itemAdded = itemAdded;
    }

    public WifiManager.MulticastLock start(WifiManager wifi) {
        multicastLock = wifi.createMulticastLock("multicastLock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();
        return multicastLock;
    }

    public void startServer() {
        Log.d("MULTICAST", "Starting server...");
        server = new MulticastServer(port, multicastGroup, new MulticastServer.ItemAdded() {
            @Override
            public void itemAdded(final String address) {
                itemAdded.itemAdded(address);
            }
        });
        server.start();
        Log.d("MULTICAST", "Server started");
        join();
    }

    public void join() {
        server.join();
        Log.d("MULTICAST", "Joined multicast group");
    }
}
