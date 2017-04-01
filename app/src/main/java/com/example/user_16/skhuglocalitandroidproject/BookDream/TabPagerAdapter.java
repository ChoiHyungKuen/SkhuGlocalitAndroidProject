
package com.example.user_16.skhuglocalitandroidproject.BookDream;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.user_16.skhuglocalitandroidproject.NoticeBoardListFragment;
import com.example.user_16.skhuglocalitandroidproject.TabFragment03;
import com.example.user_16.skhuglocalitandroidproject.TabFragment04;


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
                RequestFragment bookDreamRequestFragment = new RequestFragment();
                return bookDreamRequestFragment;
            case 1:
                GiveFragment bookDreamGiveFragment = new GiveFragment();
                return bookDreamGiveFragment;
            case 2:
                InformationFragment informationFragment = new InformationFragment();
                return informationFragment;
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