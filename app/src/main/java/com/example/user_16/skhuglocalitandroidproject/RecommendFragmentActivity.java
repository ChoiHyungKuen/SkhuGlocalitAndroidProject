package com.example.user_16.skhuglocalitandroidproject;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.HashMap;

/**
 * Created by user-16 on 2017-04-12.
 */

public class RecommendFragmentActivity extends FragmentActivity {

    private final long	FINSH_INTERVAL_TIME = 2000; // 2초안에 Back 버튼을 2번 누르면 앱 종료 -> 2초
    private long backPressedTime = 0;
    private SharedPreferences login_pref, map_pref;
    private SharedPreferences.Editor editor;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private LinearLayout fragment_list, fragment_map;
    private Fragment list_fragment, map_fragment, add_fragment, search_fragment;
    MarkCheckAsyncThread markCheckAsyncThread;
    private static boolean markable = false;

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

    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case 0:
                    try {
                        if (markCheckAsyncThread.getStatus() == AsyncTask.Status.RUNNING){
                            markCheckAsyncThread.cancel(true);
                        }
                    }catch (Exception e){ }
                    changeAddFragment();
                    break;
            }
        }
    };


    public static void setMakable(boolean m){
        markable = m;
    }

    public static boolean getMakable(){
        return markable;
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

    public void markCheck(){
        markCheckAsyncThread = new MarkCheckAsyncThread();
        markCheckAsyncThread.execute();
    }

    public class MarkCheckAsyncThread extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            int i = 0;
            while (true){
                try {
                    Log.d("스레드 실행",++i + "");
                    if(!markable){
                        break;
                    }
                    Thread.sleep(500);
                }catch (Exception e){
                    Log.e("스레드 에러",e.toString());
                }
            }
            handler.sendEmptyMessage(0);

            return "";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (markCheckAsyncThread.getStatus() == AsyncTask.Status.RUNNING){
                markCheckAsyncThread.cancel(true);
            }
        }catch (Exception e){

        }
    }

    /*
     뒤로가기 버튼을 2초내로 2번 누를 시 Application 종료
  */
    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if ( 0 <= intervalTime && FINSH_INTERVAL_TIME >= intervalTime ) {
            super.onBackPressed();
            final DBManager dbManager = new DBManager(getApplicationContext(), "app_data.db", null, 1);
            HashMap<String, String> data = dbManager.getMemberInfo();
            login_pref = getSharedPreferences("login_Info",MODE_PRIVATE);
            if(login_pref.getString("id","").equals("") && login_pref.getString("pw","").equals("") && data.size()!=0)
                dbManager.deleteAll();
            map_pref = getSharedPreferences("map_center",MODE_PRIVATE);
            editor = map_pref.edit();
            editor.clear();
            editor.commit();

        } else {
            backPressedTime = tempTime;
            Toast.makeText(RecommendFragmentActivity.this, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
