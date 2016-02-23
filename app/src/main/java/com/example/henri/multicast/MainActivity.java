package com.example.henri.multicast;

import android.content.Context;
import android.graphics.Color;
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
import android.widget.LinearLayout;

import java.io.File;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RecyclerView.OnItemTouchListener {

    // Init variables
    private final String homeDir = "MulticastApp";
    private final int port = 3003;
    private final String multicastGroup = "239.1.1.1";

    // Helper variables
    private String path;
    private ArrayList<String> selectedUsers;
    private ArrayList<String> selectedFiles;

    // Multicast
    private MulticastService multicastService;
    private WifiManager.MulticastLock multicastLock;

    // ArrayLists
    private ArrayList users = new ArrayList();
    private ArrayList files = new ArrayList();

    // Recycler View
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private GestureDetectorCompat mGestureDetector;

    // Button groups
    private LinearLayout usersBtns;
    private LinearLayout filesBtns;

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

        // INIT
        selectedUsers = new ArrayList<String>();
        selectedFiles = new ArrayList<String>();

        // FILE MANAGER
        path = "";
        fileService = new FileService(homeDir);
        files = fileService.createFileView(path);

        // MULTICAST
        startMulticast(wifi);

        // BUTTONS
        FloatingActionButton btnBack = (FloatingActionButton) findViewById(R.id.btn_back);
        FloatingActionButton btnSend = (FloatingActionButton) findViewById(R.id.btn_send);
        FloatingActionButton btnAddF = (FloatingActionButton) findViewById(R.id.btn_add_files);
        FloatingActionButton btnRefr = (FloatingActionButton) findViewById(R.id.btn_refresh);
        setBtnBack(btnBack);
        setBtnSend(btnSend);
        setBtnAddF(btnAddF);
        setBtnRefr(btnRefr);
        usersBtns = (LinearLayout) findViewById(R.id.users_btns);
        usersBtns.setActivated(true);
        filesBtns = (LinearLayout) findViewById(R.id.files_btns);
        filesBtns.setActivated(false);
        filesBtns.setVisibility(filesBtns.GONE);

        // VIEW
        setUpRecyclerView();
        setListView(users);
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

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        mGestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {}

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}

    // RECYCLER VIEW SETUP

    /* Set recycler view up */
    private void setUpRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mGestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
                int adapterPosition = mRecyclerView.getChildAdapterPosition(view);
                if (usersBtns.isActivated()) {
                    updateUsersView(view, adapterPosition);
                    updateTitleBarUsers();
                } else if (filesBtns.isActivated()) {
                    fileOrDirClicked(view, adapterPosition, path + File.separator + files.get(adapterPosition).toString());
                    updateTitleBarFiles();
                }
                return super.onSingleTapConfirmed(e);
            }
        });
        mRecyclerView.addOnItemTouchListener(this);
        mRecyclerView.setHasFixedSize(true); // improves performance
        mLayoutManager = new LinearLayoutManager(this); // using linear layout manager
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    /* Set view with selected files */
    public void setActiveFiles() {
        Log.d("count", mAdapter.getItemCount()+"");

        for (int i=0; i < mAdapter.getItemCount(); i++) {
            View v = (View) mRecyclerView.findViewHolderForAdapterPosition(0).itemView;
            v.setBackgroundColor(Color.GRAY);
            Log.d("Löyty: ", v.toString());
        }
    }

    /* Replace the list currently displayed with a new one */
    public void setListView(ArrayList<String> list) {
        mAdapter = new ListAdapter(list);
        mRecyclerView.setAdapter(mAdapter);
        /*if (filesBtns.isActivated()) {
            setActiveFiles();
        }*/
    }

    // RECYCLER VIEW

    /* Update recycler view of users */
    private void updateUsersView(View view, int adapterPosition) {
        if (view.isActivated()) {
            view.setActivated(false);
            view.setBackgroundColor(Color.TRANSPARENT);
            selectedUsers.remove(users.get(adapterPosition).toString());

        } else {
            view.setActivated(true);
            view.setBackgroundColor(Color.GRAY);
            selectedUsers.add(users.get(adapterPosition).toString());
        }
    }

    /* Check if dir or file is clicked and perform respectively */
    private void fileOrDirClicked(View view, int adapterPosition, String pathToClicked) {
        if (fileService.checkIfDir(pathToClicked)) {
            path = pathToClicked;
            files = fileService.createFileView(path);
            setListView(files);
        } else {
            updateFileView(view, adapterPosition, pathToClicked);
        }
    }

    /* Update recycler view of files */
    private void updateFileView(View view, int adapterPosition, String pathToFile) {
        if (view.isActivated()) {
            view.setActivated(false);
            view.setBackgroundColor(Color.TRANSPARENT);
            selectedFiles.remove(pathToFile);
        } else {
            view.setActivated(true);
            view.setBackgroundColor(Color.GRAY);
            selectedFiles.add(pathToFile);
        }
    }

    private void resetView() {
        setListView(users);
        path = "";
        files = fileService.createFileView(path);
        usersBtns.setVisibility(usersBtns.VISIBLE);
        filesBtns.setVisibility(filesBtns.GONE);
        usersBtns.setActivated(true);
        filesBtns.setActivated(false);
        selectedFiles.clear();
        selectedUsers.clear();
        setTitle(getResources().getString(R.string.app_name));
    }

    // TITLE UPDATES

    /* Update title users part*/
    private void updateTitleBarUsers() {
        if (selectedUsers.size() == 1) {
            setTitle("Send to: " + selectedUsers.get(0));
        } else if (selectedUsers.size() == users.size()) {
            setTitle("Send to: All");
        } else if (selectedUsers.size() == 0) {
            setTitle(R.string.app_name);
        } else {
            setTitle("Send to: " + selectedUsers.size() + " users");
        }
    }

    /* Update title files part */
    private void updateTitleBarFiles() {
        updateTitleBarUsers();
        if (selectedFiles.size() == 1) {
            setTitle(getTitle() + " - " + selectedFiles.size() + " file");
        } else {
            setTitle(getTitle() + " - " + selectedFiles.size() + " files");
        }
    }

    // MULTICAST

    /* Start multicast server */
    private void startMulticast(WifiManager wifi) {
        multicastService = new MulticastService(multicastGroup, port, new MulticastService.ItemAdded() {
            @Override
            public void itemAdded(final String address) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!users.contains(address)) {
                            users.add(address);
                            if (usersBtns.isActivated()) {
                                mAdapter.notifyItemInserted(users.size() - 1);
                            }
                        }
                    }
                });
            }
        });
        multicastLock = multicastService.start(wifi);
    }

    // BUTTONS

    /* Configure Send button */
    private void setBtnSend(FloatingActionButton fab) {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedFiles.size() > 0) {
                    Log.d("Send", "Sending " + selectedFiles.toString() + "\nTo " + selectedUsers.toString());
                    resetView();
                }
            }
        });
    }

    /* Configure Refresh button */
    private void setBtnRefr(FloatingActionButton fab) {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                multicastService.join();
            }
        });
    }

    /* Configure Back button*/
    private void setBtnBack(FloatingActionButton fab) {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                path = fileService.pathOneStepDown(path);
                files = fileService.createFileView(path);
                setListView(files);
            }
        });
    }

    /* Configure Add Files button*/
    private void setBtnAddF(FloatingActionButton fab) {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedUsers.size() > 0) {
                    usersBtns.setVisibility(usersBtns.GONE);
                    filesBtns.setVisibility(filesBtns.VISIBLE);
                    usersBtns.setActivated(false);
                    filesBtns.setActivated(true);
                    setListView(files);
                }
            }
        });
    }
}
