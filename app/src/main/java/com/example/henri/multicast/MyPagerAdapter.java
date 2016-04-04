package com.example.henri.multicast;

import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

/**
 * Created by jasu on 03.04.16.
 */
public class MyPagerAdapter extends PagerAdapter {

    public MyPagerAdapter(FragmentManager fm, MulticastService ms, String hd, HttpServer hs, ViewPager vp) {
        super(fm, ms, hd, hs, vp);
    }
}
