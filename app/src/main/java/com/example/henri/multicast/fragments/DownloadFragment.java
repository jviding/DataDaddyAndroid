package com.example.henri.multicast.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.henri.multicast.HttpServer;
import com.example.henri.multicast.ItemClickSupport;
import com.example.henri.multicast.ListAdapter;
import com.example.henri.multicast.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jasu on 03.04.16.
 */
public class DownloadFragment extends Fragment {

    // Downloadables
    private Map<String, String> downloadablesHelper;
    private ArrayList<String> downloadables;
    private ArrayList<String> selected;

    // Http
    private static HttpServer mHttpServer;

    // Helpers
    private static String homeDir;

    // Header
    private TextView header;

    // Recycler View
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.download_frag, container, false);
        header = (TextView) v.findViewById(R.id.toolbar_downloads);
        final Handler mainHandler = new Handler(v.getContext().getMainLooper());

        // INIT
        downloadablesHelper = new HashMap<String, String>();
        downloadables = new ArrayList<String>();
        selected = new ArrayList<String>();

        // BUTTON
        Button btn = (Button) v.findViewById(R.id.button_download_file);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                for (String one : selected) {
                    mHttpServer.downloadFileWithGet(downloadablesHelper.get(one), one);
                    mRecyclerView.getChildAt(downloadables.indexOf(one)).setBackgroundColor(Color.TRANSPARENT);
                    downloadables.remove(one);
                    downloadablesHelper.remove(one);
                }
                selected.clear();
                mAdapter.notifyDataSetChanged();
                header.setText("Download files");
            }
        });

        // HTTP
        setHttpCallback(mainHandler);

        // RECYCLER VIEW
        setUpRecyclerView(v);

        return v;
    }

    public static DownloadFragment newInstance(String hd, HttpServer hs) {
        homeDir = hd;
        mHttpServer = hs;
        DownloadFragment f = new DownloadFragment();
        Bundle b = new Bundle();
        return f;
    }

    // HTTP

    /* Set callback */
    private void setHttpCallback(final Handler handler) {
        mHttpServer.setCallbackForNewDownloadable(new HttpServer.ItemAdded() {
            @Override
            public void itemAdded(final String downloadable, final String address) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!downloadables.contains(downloadable)) {
                            downloadables.add(downloadable);
                            downloadablesHelper.put(downloadable, address);
                            mAdapter.notifyItemInserted(downloadables.size() - 1);
                        }
                    }
                });
            }
        });
    }

    // RECYCLER VIEW SETUP

    /* Create recycler view */
    private void setUpRecyclerView(View v) {
        mRecyclerView = (RecyclerView) v.findViewById(R.id.my_recycler_view_downloads);
        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                View view = mRecyclerView.getChildAt(position);
                if (view.isActivated()) {
                    view.setActivated(false);
                    view.setBackgroundColor(Color.TRANSPARENT);
                    selected.remove(downloadables.get(position));
                } else {
                    view.setActivated(true);
                    view.setBackgroundColor(Color.GRAY);
                    selected.add(downloadables.get(position));
                }
                updateTitleBar();
            }
        });
        mRecyclerView.setHasFixedSize(true); // improves performance
        mLayoutManager = new LinearLayoutManager(v.getContext()); // using linear layout manager
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ListAdapter(downloadables, null);
        mRecyclerView.setAdapter(mAdapter);
    }

    // TITLE UPDATE

    /* Change title accordingly */
    private void updateTitleBar() {
        if (selected.size() == 0) {
            header.setText("Download files");
        } else {
            String from = downloadablesHelper.get(selected.get(0));
            for (String one : selected) {
                if (!from.equals(downloadablesHelper.get(one))) {
                    if (selected.size() == 1) {
                        header.setText("Download 1 file from many");
                    } else {
                        header.setText("Download "+selected.size()+" files from many");
                    }
                    return;
                }
            }
            if (selected.size() == 1) {
                header.setText("Download 1 file from "+from);
            } else {
                header.setText("Download "+selected.size()+" files from "+from);
            }
        }
    }

}
