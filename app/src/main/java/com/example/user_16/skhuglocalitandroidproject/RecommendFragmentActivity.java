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

import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by user-16 on 2017-04-12.
 */

public class RecommendFragmentActivity extends FragmentActivity {

    private final long FINSH_INTERVAL_TIME = 2000; // 2초안에 Back 버튼을 2번 누르면 앱 종료 -> 2초
    private long backPressedTime = 0;
    private SharedPreferences login_pref, map_pref, add_pref;
    private SharedPreferences.Editor editor;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private LinearLayout fragment_list, fragment_map;
    private Fragment list_fragment, map_fragment, add_fragment, search_fragment;

    private MarkCheckAsyncThread markCheckAsyncThread;
    private RecommendAddAsyncThread recommendAddAsyncThread;

    private static boolean markable = false;
    private static boolean marking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recommend_fragments);


        fragment_map = (LinearLayout) findViewById(R.id.fragment_map);
        fragment_list = (LinearLayout) findViewById(R.id.fragment_list);

        list_fragment = new RecommendListFragment();
        map_fragment = new RecommendMapFragment();
        add_fragment = new RecommendAddFragment();
        search_fragment = new RecommendSearchFragment();

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_map, map_fragment);
        fragmentTransaction.replace(R.id.fragment_list, list_fragment);
        fragmentTransaction.commit();

    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0:
                    try {
                        if (markCheckAsyncThread.getStatus() == AsyncTask.Status.RUNNING) {
                            markCheckAsyncThread.cancel(true);
                        }
                    } catch (Exception e) {
                    }
                    changeAddFragment();
                    break;
                case 1:
                    Toast.makeText(RecommendFragmentActivity.this, "추천 등록 완료", Toast.LENGTH_SHORT).show();
                    changeMapFragment();
            }
        }
    };


    public static void setMakable(boolean m) {
        markable = m;
    }
    public static boolean getMakable() {
        return markable;
    }

    public static void setMaking(boolean m) { marking = m; }
    public static boolean getMaking() {
        return marking;
    }


    public void changeMapFragment() {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_map, map_fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.commit();
    }

    public void changeAddFragment() {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_map, add_fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.commit();
    }

    public void changeSearchFragment() {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_map, search_fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.commit();
    }

    public void markCheck() {
        markCheckAsyncThread = new MarkCheckAsyncThread();
        markCheckAsyncThread.execute();
    }

    public void addRecommend() {
        add_pref = getSharedPreferences("recommend_Info", MODE_PRIVATE);
        String category = add_pref.getString("category", "");
        String title = add_pref.getString("title", "");
        String callNumber = add_pref.getString("callNumber", "");
        String delivery = add_pref.getString("delivery", "");
        String review = add_pref.getString("review", "");
        String longitude = add_pref.getString("longitude", "");
        String latitude = add_pref.getString("latitude", "");
        Log.d("추천등록", category + " : " + title + " : " + callNumber + " : " + delivery + " : " + review + " : " + longitude + " : " + latitude);
        editor = add_pref.edit();
        editor.clear();
        editor.commit();
        recommendAddAsyncThread = new RecommendAddAsyncThread();
        recommendAddAsyncThread.execute(category, title, callNumber, delivery, review, longitude, latitude);
    }

    public class MarkCheckAsyncThread extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            while (true) {
                try {
                    if (!markable) {
                        break;
                    }
                    Thread.sleep(500);
                } catch (Exception e) {
                    Log.e("스레드 에러", e.toString());
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

    public class RecommendAddAsyncThread extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            URL url;
            HttpURLConnection conn = null;
            String urlStr = "";

            urlStr = "http://192.168.123.199:8080/ServerProject/Recommend/RecommendAddList"; //집
//            urlStr = "http://172.30.41.141:8080/ServerProject/Recommend/RecommendAddList";  // 학교
//            urlStr = "http://192.168.35.59:8080/ServerProject/Recommend/RecommendAddList";     //더안

            try {
                url = new URL(urlStr);
                Log.d("URL", "생성------" + urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                HashMap<String, String> stringDataMap = new HashMap<String, String>();
                stringDataMap.put("category", args[0]);
                stringDataMap.put("title", args[1]);
                stringDataMap.put("callNumber", args[2]);
                stringDataMap.put("delivery", args[3]);
                stringDataMap.put("review", args[4]);
                stringDataMap.put("longitude", args[5]);
                stringDataMap.put("latitude", args[6]);
                ObjectOutputStream oos = new ObjectOutputStream(conn.getOutputStream());
                oos.writeObject(stringDataMap);
                oos.flush();
                oos.close();
                Log.d("응답메세지", "실행중5---" + conn.getResponseCode());
                if (conn.getResponseCode() == 200) { // 서버가 받았다면
                    handler.sendEmptyMessage(1);
                }
                conn.disconnect();
            } catch (Exception e) {
//                Message msg = handler.obtainMessage();
//                msg.obj = R.string.E;
//                Log.d("에러메세지",msg.obj.toString());
//                handler.sendMessage(msg);
                Log.e("ERR", "AddMemberAsyncThread ERR : " + e);

            }

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
            if (markCheckAsyncThread.getStatus() == AsyncTask.Status.RUNNING) {
                markCheckAsyncThread.cancel(true);
            }
            if (recommendAddAsyncThread.getStatus() == AsyncTask.Status.RUNNING) {
                recommendAddAsyncThread.cancel(true);
            }
        } catch (Exception e) {

        }
    }

    /*
     뒤로가기 버튼을 2초내로 2번 누를 시 Application 종료
  */
    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINSH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
            final DBManager dbManager = new DBManager(getApplicationContext(), "app_data.db", null, 1);
            HashMap<String, String> data = dbManager.getMemberInfo();
            login_pref = getSharedPreferences("login_Info", MODE_PRIVATE);
            if (login_pref.getString("id", "").equals("") && login_pref.getString("pw", "").equals("") && data.size() != 0)
                dbManager.deleteAll();
            map_pref = getSharedPreferences("map_center", MODE_PRIVATE);
            editor = map_pref.edit();
            editor.clear();
            editor.commit();

        } else {
            backPressedTime = tempTime;
            Toast.makeText(RecommendFragmentActivity.this, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
