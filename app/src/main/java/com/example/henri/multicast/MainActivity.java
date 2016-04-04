package com.example.henri.multicast;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.util.ArrayList;

import fi.iki.elonen.ServerRunner;

public class MainActivity extends AppCompatActivity {

    // Init variables
    private final String homeDir        = "DataDaddy";
    private final int port              = 3003;
    private final String multicastGroup = "224.0.0.251";
    private final int httpPort          = 8080;

    // Http
    private HttpServer http;

    // Multicast
    private WifiManager.MulticastLock multicastLock;
    private MulticastService mMulticastService;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (multicastLock != null) {
            multicastLock.release();
            multicastLock = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // HTTP SERVER
        try {
            http = new HttpServer(httpPort);
            http.setHomeDir(homeDir);
            http.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // MULTICAST
        mMulticastService = new MulticastService(multicastGroup, port);
        multicastLock = mMulticastService.start(wifi);

        // PAGER
        ViewPager pager = (ViewPager) findViewById(R.id.viewPager);
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager(), mMulticastService, homeDir, http, pager));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
