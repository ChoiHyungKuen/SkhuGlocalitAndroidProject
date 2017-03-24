
package com.example.user_16.skhuglocalitandroidproject.BookDream;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.user_16.skhuglocalitandroidproject.NoticeBoardListFragment;
import com.example.user_16.skhuglocalitandroidproject.TabFragment01;
import com.example.user_16.skhuglocalitandroidproject.TabFragment03;
import com.example.user_16.skhuglocalitandroidproject.TabFragment04;


public class BookDreamTabPagerAdapter extends FragmentStatePagerAdapter {

    // Count number of tabs
    private int tabCount;

    public BookDreamTabPagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {

        // Returning the current tabs
        switch (position) {
            case 0:
                BookDreamRequestFragment bookDreamRequestFragment = new BookDreamRequestFragment();
                return bookDreamRequestFragment;
            case 1:
                NoticeBoardListFragment noticeBoardListFragment = new NoticeBoardListFragment();
                return noticeBoardListFragment;
            case 2:
                TabFragment03 tabFragment03 = new TabFragment03();
                return tabFragment03;
            case 3:
                TabFragment04 tabFragment04 = new TabFragment04();
                return tabFragment04;
            case 4:
                TabFragment04 tabFragment01 = new TabFragment04();
                return tabFragment01;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}