package com.example.henri.multicast;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Jasu on 21.2.2016.
 */
public class MulticastService extends AppCompatActivity {

    private WifiManager.MulticastLock multicastLock;
    private MulticastServer server;

    private final String multicastGroup;
    private final int port;

    private final ItemAdded itemAdded;
    public interface ItemAdded {
        void itemAdded(String addr);
    }

    public MulticastService(final String multicastGroup, final int port, ItemAdded itemAdded) {
        this.multicastGroup = multicastGroup;
        this.port = port;
        this.itemAdded = itemAdded;
    }

    public WifiManager.MulticastLock start(WifiManager wifi) {
        multicastLock = wifi.createMulticastLock("multicastLock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();

        // Start server thread
        startServer();
        Log.d("Server", "Server started");

        // Join multicast group
        join();
        Log.d("Server", "Joined multicast group");

        return multicastLock;
    }

    private void startServer() {
        Log.d("Server", "Starting server...");
        server = new MulticastServer(port, multicastGroup, new MulticastServer.ItemAdded() {
            @Override
            public void itemAdded(final String address) {
                itemAdded.itemAdded(address);
            }
        });
        server.start();
    }

    public void join() {
        server.join();
    }
}
