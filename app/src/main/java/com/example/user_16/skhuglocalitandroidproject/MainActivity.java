
package com.example.user_16.skhuglocalitandroidproject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class MainActivity extends FragmentActivity {
    private final long	FINSH_INTERVAL_TIME = 2000; // 2초안에 Back 버튼을 2번 누르면 앱 종료 -> 2초
    private long backPressedTime = 0;

    private SharedPreferences auth_pref;
    private SharedPreferences login_pref, map_pref;
    private SharedPreferences.Editor editor;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private GetInfoFromForestAsyncThread backgroundGetInfoFromForestThread;
    private int[] imageResId = {
            R.drawable.image_home, R.drawable.image_board, R.drawable.image_table, R.drawable.image_food
    };
    final Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            Bundle b = msg.getData();
            if(b.get("state") != null && b.get("state").equals("fail")) {
                String message = (String) b.get("message");
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("경고!");
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       finish();
                    }
                });
                alert.setIcon(R.drawable.alert);

                alert.setMessage(message);
                alert.show();
            } else {
                succeedAuthStudent();
                Toast.makeText(getApplicationContext(), "인증되었습니다!", Toast.LENGTH_LONG).show();
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!isAuthStudent()) {
            showDialog();
        } else {
            // 이미 인증 되었으면 데이터 갱신
            final DBManager dbManager = new DBManager(getApplicationContext(), "app_data.db", null, 1);
            final HashMap <String, String> dataMap = dbManager.getMemberInfo();
            backgroundGetInfoFromForestThread = new GetInfoFromForestAsyncThread();
            backgroundGetInfoFromForestThread.execute(dataMap.get("id"));
        }
        FirebaseMessaging.getInstance().subscribeToTopic("news");
        FirebaseInstanceId.getInstance().getToken();
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("홈").setIcon(imageResId[0]));
        tabLayout.addTab(tabLayout.newTab().setText("게시판").setIcon(imageResId[1]));
        tabLayout.addTab(tabLayout.newTab().setText("시간표").setIcon(imageResId[2]));
        tabLayout.addTab(tabLayout.newTab().setText("추천맛집").setIcon(imageResId[3]));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Initializing ViewPager
        viewPager = (ViewPager) findViewById(R.id.pager);

        // Creating TabPagerAdapter adapter
        TabPagerAdapter pagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // Set TabSelectedListener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }
    public void setRecommendFragment(String longitude, String latitude){
        map_pref = getSharedPreferences("map_center", MODE_PRIVATE);
        editor = map_pref.edit();
        editor.putInt("longitude", Integer.parseInt(longitude));
        editor.putInt("latitude", Integer.parseInt(latitude));
        editor.putInt("zoomlevel", 14);
        editor.commit();
        viewPager.setCurrentItem(4,true);
    }

    public  boolean isAuthStudent() {
        auth_pref = getApplicationContext().getSharedPreferences("auth_Info", MODE_PRIVATE);
        if (!auth_pref.getString("auth", "").equals("")) {
            return true;
        } else {
            return false;
        }
    }
    public  void succeedAuthStudent() {
        Log.d("auth","초기화 작업!");
        auth_pref = getApplicationContext().getSharedPreferences("auth_Info",MODE_PRIVATE);
        editor = auth_pref.edit();
        editor.putString("auth", "success");
        editor.commit();
    }
    public void showDialog() {
        final DBManager dbManager = new DBManager(getApplicationContext(), "app_data.db", null, 1);
        final HashMap <String, String> dataMap = dbManager.getMemberInfo();
        LayoutInflater dialog = LayoutInflater.from(this);
        final View dialogLayout = dialog.inflate(R.layout.auth_dialog, null);
        final Dialog myDialog = new Dialog(this);

        myDialog.setTitle("Forest 회원 인증");
        myDialog.setContentView(dialogLayout);
        myDialog.setCancelable(false);
        myDialog.show();
        final TextView authId = (TextView) dialogLayout.findViewById(R.id.auth_forest_input_id);
        final EditText authPw = (EditText) dialogLayout.findViewById(R.id.auth_forest_input_pw);
        TextView contentTx = (TextView) dialogLayout.findViewById(R.id.auth_forest_tx);
        TextView requestBtn = (TextView)dialogLayout.findViewById(R.id.auth_request_btn);
        TextView cancelBtn = (TextView)dialogLayout.findViewById(R.id.auth_cancel_btn);
        authId.setText(dataMap.get("id"));
        contentTx.setText("서비스를 이용하기 위해서는 Forest 사이트로부터 인증 받아야 합니다. \n" +
                "학사 시스템 Forest 홈페이지의 아이디와 비밀번호를 입력하여 \n" +
                "아래의 인증버튼을 누르세요.");
        requestBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final DBManager dbManager = new DBManager(getApplicationContext(), "app_data.db", null, 1);
                final HashMap <String, String> dataMap = dbManager.getMemberInfo();
                backgroundGetInfoFromForestThread = new GetInfoFromForestAsyncThread();
                backgroundGetInfoFromForestThread.execute(authId.getText().toString(),
                    authPw.getText().toString());
                myDialog.dismiss();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                myDialog.cancel();
                finish();
            }
        });
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
            Toast.makeText(MainActivity.this, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public class GetInfoFromForestAsyncThread extends AsyncTask<String, String, String> {

        ProgressDialog pd=new ProgressDialog(MainActivity.this);
        // Thread를 시작하기 전에 호출되는 함수
        protected void onPreExecute() {
            super.onPreExecute();

            pd.setTitle("정보 받아오는 중...");
            pd.setMessage("잠시만 기다려주세요. \n 회원님의 데이터를 받아오고 있습니다.");
            pd.setCancelable(false);
            pd.show();
        }

        // Thread의 주요 작업을 처리 하는 함수
        // Thread를 실행하기 위해 excute(~)에서 전달한 값을 인자로 받습니다.
        protected String doInBackground(String... args) {
            URL url = null;
            HttpURLConnection conn = null;
            String urlStr = "";
            HashMap<String, String> dataMap = new HashMap<>();

            dataMap.put("id",args[0]);
            if(args.length > 1) {
                dataMap.put("pw", args[1]);
            }
            urlStr = "http://"+getString(R.string.ip_address)+":8080/ForestWebProject/auth/forest";
            try {
                url = new URL(urlStr);
                Log.d("test", urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                ObjectOutputStream oos =new ObjectOutputStream(conn.getOutputStream());
                oos.writeObject(dataMap);
                oos.flush();
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { // 서버가 받았다면
                    Message msg = handler.obtainMessage();
                    Bundle b = new Bundle();
                    ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                    HashMap<String, String> stateDataMap = (HashMap<String, String>)ois.readObject();
                    if(stateDataMap.get("fail")!=null) {    // 빈데이터면 아이디 비번 틀림
                        b.putString("state","fail");
                        b.putString("message",stateDataMap.get("fail"));
                    } else {
                        b.putString("state","success");
                    }
                    ois.close();
                    msg.setData(b);
                    handler.sendMessage(msg);
                }
                oos.close();
                conn.disconnect();
            } catch (Exception e) {
                Log.e("ERR", "CompletePrecentCondionInfoAsyncThread ERR : " + e);
            }

            return "";
        }

        // doInBackground(~)에서 호출되어 주로 UI 관련 작업을 하는 함수
        protected void onProgressUpdate(String... progress) {

        }

        // Thread를 처리한 후에 호출되는 함수
        // doInBackground(~)의 리턴값을 인자로 받습니다.
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(pd.isShowing()) pd.dismiss();
        }

        // AsyncTask.cancel(true) 호출시 실행되어 thread를 취소 합니다.
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}
