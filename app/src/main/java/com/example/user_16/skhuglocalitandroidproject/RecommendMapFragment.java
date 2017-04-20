package com.example.user_16.skhuglocalitandroidproject;

/**
 * Created by user-16 on 2017-04-11.
 */

import android.app.Activity;

import com.example.user_16.skhuglocalitandroidproject.NaverMap.ActivityHostFragment;

public class RecommendMapFragment extends ActivityHostFragment {

    @Override
    protected Class<? extends Activity> getActivityClass() {
        return MapViewer.class;
    }

}
