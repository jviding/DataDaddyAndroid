package com.example.henri.multicast;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.example.henri.multicast.fragments.*;

/**
 * Created by jasu on 03.04.16.
 */
public class PagerAdapter extends FragmentPagerAdapter {

    private MulticastService mMulticastService;
    private HttpServer mHttpServer;
    private String homeDir;
    private ViewPager mViewPager;

    public PagerAdapter(FragmentManager fm, MulticastService ms, String hd, HttpServer hs, ViewPager vp) {
        super(fm);
        mMulticastService = ms;
        homeDir = hd;
        mHttpServer = hs;
        mViewPager = vp;
    }
    @Override
    public Fragment getItem(int pos) {
        switch (pos) {
            case 1: return SendFragment.newInstance(mMulticastService, homeDir, mHttpServer);
            case 2: return DownloadFragment.newInstance(homeDir, mHttpServer);
            default: return DefaultFragment.newInstance(mViewPager);
        }
    }
    @Override
    public int getCount() {
        return 3;
    }
}
