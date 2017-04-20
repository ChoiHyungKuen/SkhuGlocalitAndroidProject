package com.example.user_16.skhuglocalitandroidproject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.LinearLayout;

/**
 * Created by user-16 on 2017-04-12.
 */

public class RecommendFragmentActivity extends FragmentActivity {

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    LinearLayout fragment_list, fragment_map;
    Fragment list_fragment, map_fragment, add_fragment, search_fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recommend_fragments);
        fragment_map = (LinearLayout)findViewById(R.id.fragment_map);
        fragment_list = (LinearLayout)findViewById(R.id.fragment_list);

        list_fragment = new RecommendListFragment();
        map_fragment = new RecommendMapFragment();
        add_fragment = new RecommendAddFragment();
        search_fragment = new RecommendSearchFragment();

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_map,map_fragment);
        fragmentTransaction.replace(R.id.fragment_list,list_fragment);
        fragmentTransaction.commit();

    }

    public void changeMapFragment(){
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_map,map_fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.commit();
    }

    public void changeAddFragment(){
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_map,add_fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.commit();
    }

    public void changeSearchFragment(){
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_map,search_fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.commit();
    }


}
