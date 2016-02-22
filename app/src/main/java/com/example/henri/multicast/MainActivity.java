package com.example.henri.multicast;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GestureDetectorCompat;
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

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RecyclerView.OnItemTouchListener {

    // Init variables
    private final String homeDir = "MulticastApp";
    private final int port = 3003;
    private final String multicastGroup = "239.1.1.1";

    // Multicast
    private MulticastService multicastService;
    private WifiManager.MulticastLock multicastLock;

    // ArrayLists
    private ArrayList users = new ArrayList();
    private ArrayList files = new ArrayList();

    // View
    private int viewing;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private GestureDetectorCompat mGestureDetector;

    // Files
    private FileService fileService;

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // TEST DATA
        // list IP addresses
        users.add("123.1.1.1");
        users.add("123.1.1.2");
        users.add("123.1.1.3");
        users.add("123.1.1.4");
        users.add("123.1.1.5");

        // FILE MANAGER
        fileService = new FileService(homeDir);

        // VIEW
        setUpRecyclerView();
        setListView(users);
        // view ip addresses
        viewing = 0;

        // MULTICAST
        startMulticast(wifi);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                multicastService.join();
                Log.d("View", "Button pressed");

                files = fileService.createFileView(Environment.getExternalStorageDirectory()+File.separator+homeDir);
                viewing = 1;
                setListView(files);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        mGestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public void setListView(ArrayList<String> list) {
        mAdapter = new ListAdapter(list);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void setUpRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mGestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
                int adapterPosition = mRecyclerView.getChildAdapterPosition(view);
                if (viewing == 0) {
                    Log.d("View", users.get(adapterPosition).toString());
                } else {
                    Log.d("View", files.get(adapterPosition).toString());
                }
                return super.onSingleTapConfirmed(e);
            }
        });
        mRecyclerView.addOnItemTouchListener(this);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    public void startMulticast(WifiManager wifi) {
        multicastService = new MulticastService(multicastGroup, port, new MulticastService.ItemAdded() {
            @Override
            public void itemAdded(final String address) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!users.contains(address)) {
                            users.add(address);
                            if (viewing == 0) {
                                mAdapter.notifyItemInserted(users.size() - 1);
                            }
                        }
                    }
                });
            }
        });
        multicastLock = multicastService.start(wifi);
    }
}
