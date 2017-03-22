
package com.example.user_16.skhuglocalitandroidproject;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


public class TabPagerAdapter extends FragmentStatePagerAdapter {

    // Count number of tabs
    private int tabCount;

    public TabPagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {

        // Returning the current tabs
        switch (position) {
            case 0:
                TabFragment01 tabFragment01 = new TabFragment01();
                return tabFragment01;
            case 1:
                TabFragment02 tabFragment02 = new TabFragment02();
                return tabFragment02;
            case 2:
                TabFragment03 tabFragment03 = new TabFragment03();
                return tabFragment03;
            case 3:
                TabFragment04 tabFragment04 = new TabFragment04();
                return tabFragment04;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}