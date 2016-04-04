package com.example.henri.multicast.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.henri.multicast.FileService;
import com.example.henri.multicast.HttpServer;
import com.example.henri.multicast.ItemClickSupport;
import com.example.henri.multicast.ListAdapter;
import com.example.henri.multicast.MainActivity;
import com.example.henri.multicast.MulticastServer;
import com.example.henri.multicast.MulticastService;
import com.example.henri.multicast.R;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by jasu on 03.04.16.
 */
public class SendFragment extends Fragment implements RecyclerView.OnItemTouchListener {

    // ArrayLists
    private ArrayList<String> users = new ArrayList<String>();
    private ArrayList<String> files = new ArrayList<String>();

    // Multicast
    private static MulticastService mMulticastService;

    // Http
    private static HttpServer mHttpServer;

    // Helper variables
    private static String homeDir;
    private String path;
    private ArrayList<String> selectedUsers;
    private ArrayList<String> selectedFiles;

    // Recycler View
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    // Button groups
    private LinearLayout usersBtns;
    private LinearLayout filesBtns;

    // Files
    private FileService fileService;

    // Header
    private TextView header;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.send_frag, container, false);
        header = (TextView) v.findViewById(R.id.toolbar_sends);
        final Handler mainHandler = new Handler(v.getContext().getMainLooper());

        // INIT
        selectedUsers = new ArrayList<String>();
        selectedFiles = new ArrayList<String>();

        // FILE MANAGER
        path = "";
        fileService = new FileService(homeDir);
        files = fileService.createFileView(path);

        // BUTTONS
        FloatingActionButton btnBack = (FloatingActionButton) v.findViewById(R.id.btn_back);
        FloatingActionButton btnSend = (FloatingActionButton) v.findViewById(R.id.btn_send);
        FloatingActionButton btnAddF = (FloatingActionButton) v.findViewById(R.id.btn_add_files);
        FloatingActionButton btnRefr = (FloatingActionButton) v.findViewById(R.id.btn_refresh);
        setBtnBack(btnBack);
        setBtnSend(btnSend);
        setBtnAddF(btnAddF);
        setBtnRefr(btnRefr);
        usersBtns = (LinearLayout) v.findViewById(R.id.users_btns);
        usersBtns.setActivated(true);
        filesBtns = (LinearLayout) v.findViewById(R.id.files_btns);
        filesBtns.setActivated(false);
        filesBtns.setVisibility(filesBtns.GONE);

        // MULTICAST
        setUpMulticast(mainHandler);

        // VIEW
        setUpRecyclerView(v);
        setListView(users);

        return v;
    }

    public static SendFragment newInstance(MulticastService ms, String hd, HttpServer hs) {
        mMulticastService = ms;
        homeDir = hd;
        mHttpServer = hs;
        SendFragment f = new SendFragment();
        Bundle b = new Bundle();
        return f;
    }

    // MULTICAST SET UP

    /* Set callback for updating users list and start multicasting */
    public void setUpMulticast(final Handler handler) {
        mMulticastService.setCallbackForNewUser(new MulticastService.ItemAdded() {
            @Override
            public void itemAdded(final String address) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!users.contains(cleanIPAddr(address))) {
                            users.add(cleanIPAddr(address));
                            if (usersBtns.isActivated()) {
                                mAdapter.notifyItemInserted(users.size() - 1);
                            }
                        }
                    }
                });
            }
        });
        mMulticastService.startServer();
    }

    /* Clean possible slash from the IP */
    private String cleanIPAddr(String ip) {
        if (ip.charAt(0) == '/') {
            return ip.substring(1);
        }
        return ip;
    }

    // RECYCLER VIEW SET UP

    /* Create recycler view */
    private void setUpRecyclerView(View v) {
        mRecyclerView = (RecyclerView) v.findViewById(R.id.my_recycler_view);
        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                View view = mRecyclerView.getChildAt(position);
                if (usersBtns.isActivated()) {
                    updateUsersView(view, position);
                    updateTitleBarUsers();
                } else if (filesBtns.isActivated()) {
                    fileOrDirClicked(view, position, files.get(position).toString());
                    updateTitleBarFiles();
                }
            }
        });
        mRecyclerView.setHasFixedSize(true); // improves performance
        mLayoutManager = new LinearLayoutManager(v.getContext()); // using linear layout manager
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    /* Replace the list currently displayed with a new one */
    private void setListView(ArrayList<String> list) {
        if (filesBtns.isActivated()) {
            mAdapter = new ListAdapter(list, selectedFiles);
        } else {
        mAdapter = new ListAdapter(list, null);
        }
        mRecyclerView.setAdapter(mAdapter);
    }

    /* Set view with selected files */
    private void setActiveFiles() {
        Log.d("count", mAdapter.getItemCount() + "");

        for (int i=0; i < mAdapter.getItemCount(); i++) {
            View v = (View) mRecyclerView.findViewHolderForAdapterPosition(0).itemView;
            v.setBackgroundColor(Color.GRAY);
        }
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
        header.setText("Send Files");
    }

    // TITLE UPDATES

    /* Update title users part*/
    private void updateTitleBarUsers() {
        if (selectedUsers.size() == 1) {
            header.setText("Send to: " + selectedUsers.get(0));
        } else if (selectedUsers.size() == users.size()) {
            header.setText("Send to: All");
        } else if (selectedUsers.size() == 0) {
            header.setText("Send Files");
        } else {
            header.setText("Send to: " + selectedUsers.size() + " users");
        }
    }

    /* Update title files part */
    private void updateTitleBarFiles() {
        updateTitleBarUsers();
        if (selectedFiles.size() == 1) {
            header.setText(header.getText() + " - " + selectedFiles.size() + " file");
        } else {
            header.setText(header.getText() + " - " + selectedFiles.size() + " files");
        }
    }

    // BUTTONS

    /* Configure Send button */
    private void setBtnSend(FloatingActionButton fab) {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedFiles.size() > 0) {
                    for (String user : selectedUsers) {
                        for (String file : selectedFiles) {
                            mHttpServer.sendFilesWithPut(user, file);
                        }
                    }
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
                mMulticastService.join();
            }
        });
    }

    /* Configure Back button*/
    private void setBtnBack(FloatingActionButton fab) {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (path.equals("") || path.equals(File.separator)) {
                    resetView();
                } else {
                    path = fileService.pathOneStepDown(path);
                    files = fileService.createFileView(path);
                    setListView(files);
                }
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
                    setListView(fileService.createFileView(path));
                }
            }
        });
    }

    // For implemented recycler class
    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {return false;}
    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {}
    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}

}
