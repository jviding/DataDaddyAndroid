package com.example.henri.multicast.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.henri.multicast.R;

/**
 * Created by jasu on 03.04.16.
 */
public class DefaultFragment extends Fragment {

    private static ViewPager mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.default_frag, container, false);

        Button sendFilesBtn = (Button) v.findViewById(R.id.button_page_send);
        sendFilesBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mViewPager.setCurrentItem(1);
            }
        });

        Button downloadFilesBtn = (Button) v.findViewById(R.id.button_page_download);
        downloadFilesBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mViewPager.setCurrentItem(2);
            }
        });

        return v;
    }

    public static DefaultFragment newInstance(ViewPager vp) {
        mViewPager = vp;
        DefaultFragment f = new DefaultFragment();
        return f;
    }

}
