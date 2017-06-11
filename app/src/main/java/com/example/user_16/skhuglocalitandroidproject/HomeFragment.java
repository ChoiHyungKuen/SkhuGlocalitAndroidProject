
package com.example.user_16.skhuglocalitandroidproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user_16.skhuglocalitandroidproject.BookDream.GiveListData;
import com.example.user_16.skhuglocalitandroidproject.BookDream.RequestListData;
import com.example.user_16.skhuglocalitandroidproject.FreeNoticeBoard.FreeNoticeBoard_ListData;
import com.example.user_16.skhuglocalitandroidproject.FreeNoticeBoard.FreeNoticeBoard_Main;
import com.example.user_16.skhuglocalitandroidproject.InfoNoticeBoard.InfoNoticeBoard_ListData;
import com.example.user_16.skhuglocalitandroidproject.InfoNoticeBoard.InfoNoticeBoard_Main;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;


public class HomeFragment extends Fragment {
    private LinearLayout attendanceLayout;
    private TextView btn_logout;
    private SharedPreferences login_pref, map_pref;
    private SharedPreferences auth_pref;
    SharedPreferences.Editor editor;
    private GetAttendanceInfoAsyncThread backgroundGetAttendanceInfoThread;

    private TextView home_bookDream, home_bookDream2, home_freeBoard, home_infoBoard, home_departmentBoard;
    private free_InitAsyncThread free_backgroundInitThread;             // 자유게시판 데이터 받아오기
    private info_InitAsyncThread info_backgroundInitThread;             // 정보게시판 데이터 받아오기
    private book_InitAsyncThread book_backgroundInitThread;             // 북드림(요청) 데이터 받아오기
    private dream_InitAsyncThread dream_backgroundInitThread;           // 북드림(드림) 데이터 받아오기
    private InitDepartmentInfoAsyncThread depart_backgroundInitThread;  // 학과게시판 데이터 받아오기

    private ListView recommendFavoriteListView = null;
    private ListViewAdapter listViewAdapter = null;

    final Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            Bundle b = msg.getData();

            if(msg.what == 0){
                String[][] attendanceInfo = (String[][]) msg.obj;

                final TableLayout table = new TableLayout(getActivity());
                table.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
                table.setShrinkAllColumns(true);
                table.setStretchAllColumns(true);
                TableRow rowTitle = new TableRow(getActivity());
                rowTitle.setGravity(Gravity.CENTER_HORIZONTAL);
                TableRow row = new TableRow(getActivity());

                // title column/row
                TextView title = new TextView(getActivity());
                title.setText("출석 현황");

                title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(Typeface.SERIF, Typeface.BOLD);

                TableRow.LayoutParams params = new TableRow.LayoutParams();
                params.span = 7;

                rowTitle.addView(title, params);
                table.addView(rowTitle);


                for(int i=0; i<attendanceInfo.length; i++) {
                    row=new TableRow(getActivity());
                    //row.setWeightSum(12f);
                    //row.setLayoutParams( new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));
                    int subjectCnt =1;  // 75분 수업인경우 한셀에 두개과목이 들어감
                    for(int j=0; j<7; j++) {
                        String s = attendanceInfo[i][j];
                        TextView tx = new TextView(getActivity());
                        tx.setText(s);
                        tx.setTextSize(12f);
                        tx.setTypeface(Typeface.DEFAULT_BOLD);
                        row.addView(tx);
                    }

                    table.addView(row);
                }

                table.setBackgroundResource(R.drawable.row_border);
                ScrollView sv = new ScrollView(getActivity());
                sv.addView(table);
                attendanceLayout.addView(sv);
            /*

            LinearLayout layout = new LinearLayout(getActivity());

            layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(sv);
            ViewGroup viewGroup =(ViewGroup) getView();
            viewGroup.removeAllViews();
            viewGroup.addView(layout);*/
            }

            // 자유게시판
            if(msg.what == 1){
                addItem_freeboard(b.getString("title"));
            }
            // 정보게시판
            if(msg.what == 2){
                addItem_infoboard(b.getString("title"));
            }
            // 북드림 게시판(요청)
            if(msg.what == 3){
                addItem_bookdream(b.getString("title"));
            }
            // 학과 게시판
            if(msg.what == 4){
                String[][] departmentInfo = (String[][]) msg.obj;
                    String title = departmentInfo[0][0];
                    addItem_department(title);
            }
            // 북드림 게시판(드림)
            if(msg.what == 5){
                addItem_dream(b.getString("title"));
            }
        }

    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.home_fragment, container, false);
        final DBManager dbManager = new DBManager(getContext(), "app_data.db", null, 1);

        //로그아웃 기능
        btn_logout = (TextView) rootView.findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //DB헬퍼에 있는 정보와 login_Info 삭제
                login_pref = getContext().getSharedPreferences("login_Info", MODE_PRIVATE);
                Log.d("회원테이블",dbManager.getMemberInfo().toString());   //지우면 에러
                dbManager.deleteAll();
                //dbManager.delete(login_pref.getString("id",""));
                Log.d("test", login_pref.getString("id",""));
                editor = login_pref.edit();
                editor.clear();
                editor.commit();
                map_pref = getContext().getSharedPreferences("map_center",MODE_PRIVATE);
                editor = map_pref.edit();
                editor.clear();
                editor.commit();

                Toast.makeText(getActivity(), "로그아웃 되었습니다.\n 다시 로그인 해주세요.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getContext(), Activity_Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });
        attendanceLayout = (LinearLayout) rootView.findViewById(R.id.attendance_layout);
        if(isAuthStudent()) {

            auth_pref = getActivity().getSharedPreferences("auth_Info", MODE_PRIVATE);


            backgroundGetAttendanceInfoThread = new GetAttendanceInfoAsyncThread();
            backgroundGetAttendanceInfoThread.execute(dbManager.getMemberInfo().get("id"));
        }
        recommendFavoriteListView = (ListView) rootView.findViewById(R.id.favoriteList);
        listViewAdapter = new ListViewAdapter(getActivity());
        recommendFavoriteListView.setAdapter(listViewAdapter);
        HashMap<Integer, HashMap<String, String>> favoritesList = dbManager.getFavoritesList();
        HashMap<String, String> favorites;
        String title, branch, longitude, latitude, up, down;
        for (int i = 0; i < favoritesList.size(); i++){
            favorites = favoritesList.get(i);
            title = favorites.get("title");
            branch = favorites.get("branch");
            longitude = favorites.get("longitude");
            latitude = favorites.get("latitude");
            up = favorites.get("up");
            down = favorites.get("down");
            listViewAdapter.addItem(title, branch, longitude, latitude, up, down);
        }
        recommendFavoriteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RecommendSearchListData recommendListData = (RecommendSearchListData)listViewAdapter.getItem(position);
                String longitude = recommendListData.sLongitude;
                String latitude = recommendListData.sLatitude;
                ((MainActivity)getActivity()).setRecommendFragment(longitude, latitude);
            }
        });
        free_backgroundInitThread = new free_InitAsyncThread();            // 자유게시판 DB로부터 데이터를 새로 받아온다.
        free_backgroundInitThread.execute();
        info_backgroundInitThread = new info_InitAsyncThread();            // 정보게시판 DB로부터 데이터를 새로 받아온다.
        info_backgroundInitThread.execute();
        book_backgroundInitThread = new book_InitAsyncThread();            // 북드림(요청) DB로부터 데이터를 새로 받아온다.
        book_backgroundInitThread.execute();
        dream_backgroundInitThread = new dream_InitAsyncThread();          // 북드림(드림) DB로부터 데이터를 새로 받아온다.
        dream_backgroundInitThread.execute();
        depart_backgroundInitThread = new InitDepartmentInfoAsyncThread(); // 학과 게시판 DB로부터 데이터를 새로 받아온다.
        depart_backgroundInitThread.execute();

        //북드림(요청)
        home_bookDream = (TextView) rootView.findViewById(R.id.home_bookDream);
        home_bookDream.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), com.example.user_16.skhuglocalitandroidproject.BookDream.MainActivity.class);
                getActivity().startActivity(intent);
            }
        });
        //북드림(드림)
        home_bookDream2 = (TextView) rootView.findViewById(R.id.home_bookDream2);
        home_bookDream2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), com.example.user_16.skhuglocalitandroidproject.BookDream.MainActivity.class);
                getActivity().startActivity(intent);
            }
        });
        //자유게시판
        home_freeBoard = (TextView) rootView.findViewById(R.id.home_freeBoard);
        home_freeBoard.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FreeNoticeBoard_Main.class);
                getActivity().startActivity(intent);
            }
        });
        //정보게시판
        home_infoBoard = (TextView) rootView.findViewById(R.id.home_infoBoard);
        home_infoBoard.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), InfoNoticeBoard_Main.class);
                getActivity().startActivity(intent);
            }
        });
        //학과게시판
        home_departmentBoard = (TextView) rootView.findViewById(R.id.home_departmentBoard);
        home_departmentBoard.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), DepartmentNoticeboardActivity.class);
                getActivity().startActivity(intent);
            }
        });

        return rootView;
    }
    public  boolean isAuthStudent() {
        auth_pref = getActivity().getSharedPreferences("auth_Info", MODE_PRIVATE);
        if (!auth_pref.getString("auth", "").equals("")) {

           /* editor = auth_pref.edit();
            editor.putString("auth", "");
            editor.commit();*/
            return true;
        } else {
            return false;
        }
    }
    public class GetAttendanceInfoAsyncThread extends AsyncTask<String, String, String> {

        // Thread를 시작하기 전에 호출되는 함수
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Thread의 주요 작업을 처리 하는 함수
        // Thread를 실행하기 위해 excute(~)에서 전달한 값을 인자로 받습니다.
        protected String doInBackground(String... args) {
            URL url = null;
            HttpURLConnection conn = null;
            String urlStr = "";
            HashMap<String, String> dataMap = new HashMap<>();

            dataMap.put("user", args[0]);

            urlStr = "http://"+getString(R.string.ip_address)+":8080/ForestWebProject/parse/attendance";
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
                    Log.d("ois","왔다!");
                    Message msg = handler.obtainMessage();
                    msg.what = 0;
                    Bundle b = new Bundle();
                    ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                    String[][] attendanceInfo = (String[][]) ois.readObject();

                    Log.d("ois",attendanceInfo.length+"");
                    for(int i=0; i<attendanceInfo.length; i++) {
                        for(int j=0; j<7; j++)
                            Log.d("ois",attendanceInfo[i][j]);
                    }
                    msg.obj = attendanceInfo;

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
        }

        // AsyncTask.cancel(true) 호출시 실행되어 thread를 취소 합니다.
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    /*----------------------------------------------------------------------------------------------
        홈 화면 - 자유게시판 한 줄 게시판 내용 받아오기
    ----------------------------------------------------------------------------------------------*/
    private class free_InitAsyncThread extends AsyncTask<Void, String, String> {
        // Thread를 시작하기 전에 호출되는 함수
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Thread의 주요 작업을 처리 하는 함수
        // Thread를 실행하기 위해 excute(~)에서 전달한 값을 인자로 받습니다.
        protected String doInBackground(Void...args) {
            URL url = null;
            HttpURLConnection conn = null;
            String urlStr = "";

            urlStr = "http://"+getString(R.string.ip_address)+":8080/SkhuGlocalitWebProject/FreeNoticeBoard_Init";

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
                int responseCode = conn.getResponseCode();
                Log.d("D", responseCode+"");
                if (responseCode == HttpURLConnection.HTTP_OK) {    // 송수신이 잘되면 - 데이터를 받은 것입니다.
                    Log.d("coded", "자유게시판 데이터 들어옴");
                    ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                    HashMap<String, HashMap<String,String>> dataMap = (HashMap<String, HashMap<String,String>>)ois.readObject();
                    ois.close();

                    int i = dataMap.size()-1;
                        HashMap<String, String> map = dataMap.get(i+"");
                        Message msg_free = handler.obtainMessage();
                        msg_free.what = 1;
                        Bundle b = new Bundle();
                        b.putString("title", map.get("title"));
                        msg_free.setData(b);
                        handler.sendMessage(msg_free);
                }
                conn.disconnect();
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error 발생", Toast.LENGTH_SHORT).show();
                Log.e("ERR", "InitAsyncThread ERR : " + e.getMessage());
            }
            return "";
        }

        // doInBackground(~)에서 호출되어 주로 UI 관련 작업을 하는 함수
        protected void onProgressUpdate(String... progress) {}

        // Thread를 처리한 후에 호출되는 함수
        // doInBackground(~)의 리턴값을 인자로 받습니다.
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        // AsyncTask.cancel(true) 호출시 실행되어 thread를 취소 합니다.
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    /*----------------------------------------------------------------------------------------------
        홈 화면 - 정보게시판 한 줄 게시판 내용 받아오기
    ----------------------------------------------------------------------------------------------*/
    private class info_InitAsyncThread extends AsyncTask<Void, String, String> {
        // Thread를 시작하기 전에 호출되는 함수
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Thread의 주요 작업을 처리 하는 함수
        // Thread를 실행하기 위해 excute(~)에서 전달한 값을 인자로 받습니다.
        protected String doInBackground(Void...args) {
            URL url = null;
            HttpURLConnection conn = null;
            String urlStr = "";

            urlStr = "http://"+getString(R.string.ip_address)+":8080/SkhuGlocalitWebProject/InfoNoticeBoard_Init";

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
                int responseCode = conn.getResponseCode();
                Log.d("D", responseCode+"");
                if (responseCode == HttpURLConnection.HTTP_OK) {    // 송수신이 잘되면 - 데이터를 받은 것입니다.
                    Log.d("coded", "정보게시판 데이터 들어옴");
                    ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                    HashMap<String, HashMap<String,String>> dataMap = (HashMap<String, HashMap<String,String>>)ois.readObject();
                    ois.close();

                        int i = dataMap.size()-1;
                        HashMap<String, String> map = dataMap.get(i+"");
                        Message msg_info = handler.obtainMessage();
                        msg_info.what = 2;
                        Bundle b = new Bundle();
                        b.putString("title", map.get("title"));
                        msg_info.setData(b);
                        handler.sendMessage(msg_info);
                }
                conn.disconnect();
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error 발생", Toast.LENGTH_SHORT).show();
                Log.e("ERR", "InitAsyncThread ERR : " + e.getMessage());
            }
            return "";
        }

        // doInBackground(~)에서 호출되어 주로 UI 관련 작업을 하는 함수
        protected void onProgressUpdate(String... progress) {}

        // Thread를 처리한 후에 호출되는 함수
        // doInBackground(~)의 리턴값을 인자로 받습니다.
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        // AsyncTask.cancel(true) 호출시 실행되어 thread를 취소 합니다.
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    /*----------------------------------------------------------------------------------------------
        홈 화면 - 북 드림(요청) 한 줄 게시판 내용 받아오기
    ----------------------------------------------------------------------------------------------*/
    public class book_InitAsyncThread extends AsyncTask<Void, String, String> {
        // Thread를 시작하기 전에 호출되는 함수
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Thread의 주요 작업을 처리 하는 함수
        // Thread를 실행하기 위해 excute(~)에서 전달한 값을 인자로 받습니다.
        protected String doInBackground(Void...args) {
            URL url = null;
            HttpURLConnection conn = null;
            String urlStr = "";

            urlStr = "http://"+getString(R.string.ip_address)+":8080/SkhuGlocalitWebProject/bookdream/initRequestInfo";

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
                int responseCode = conn.getResponseCode();
                Log.d("D", responseCode+"");
                if (responseCode== HttpURLConnection.HTTP_OK) {    // 송수신이 잘되면 - 데이터를 받은 것
                    Log.d("coded", "들어옴");
                    ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                    HashMap<String, HashMap<String, String>> dataMap = (HashMap<String, HashMap<String, String>>) ois.readObject();
                    ois.close();

                    int i = dataMap.size()-1;
                    HashMap<String, String> stringDataMap = dataMap.get(i + "");
                    Message msg_book = handler.obtainMessage();
                    msg_book.what = 3;
                    Bundle b = new Bundle();
                    b.putString("title", stringDataMap.get("title"));
                    msg_book.setData(b);
                    handler.sendMessage(msg_book);
                }
                conn.disconnect();
            } catch (Exception e) {
                //Toast.makeText(getActivity(), "Error 발생", Toast.LENGTH_SHORT).show();
                Log.e("ERR", "InitAsyncThread ERR : " + e);
            }
            return "";
        }

        // doInBackground(~)에서 호출되어 주로 UI 관련 작업을 하는 함수
        protected void onProgressUpdate(String... progress) {}

        // Thread를 처리한 후에 호출되는 함수
        // doInBackground(~)의 리턴값을 인자로 받는다.
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        // AsyncTask.cancel(true) 호출시 실행되어 thread를 취소 합니다.
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    /*----------------------------------------------------------------------------------------------
        홈 화면 - 학과 게시판 한 줄 게시판 내용 받아오기
    ----------------------------------------------------------------------------------------------*/
    public class InitDepartmentInfoAsyncThread extends AsyncTask<String, String, String> {

        // Thread를 시작하기 전에 호출되는 함수
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Thread의 주요 작업을 처리 하는 함수
        // Thread를 실행하기 위해 excute(~)에서 전달한 값을 인자로 받습니다.
        protected String doInBackground(String... args) {
            URL url = null;
            HttpURLConnection conn = null;
            String urlStr = "";
            HashMap<String, String> dataMap = new HashMap<>();

            urlStr = "http://"+getString(R.string.ip_address)+":8080/ForestWebProject/departmentNoticeboard/init";
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
                    Message msg_depart = handler.obtainMessage();
                    msg_depart.what = 4;
                    Bundle b = new Bundle();
                    ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                    String[][] departmentInfo = (String[][]) ois.readObject();

                    msg_depart.obj = departmentInfo;

                    ois.close();
                    msg_depart.setData(b);
                    handler.sendMessage(msg_depart);
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
        }

        // AsyncTask.cancel(true) 호출시 실행되어 thread를 취소 합니다.
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    /*----------------------------------------------------------------------------------------------
        홈 화면 - 북드림(드림) 한 줄 게시판 내용 받아오기
    ----------------------------------------------------------------------------------------------*/
    public class dream_InitAsyncThread extends AsyncTask<Void, String, String> {
        // Thread를 시작하기 전에 호출되는 함수
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Thread의 주요 작업을 처리 하는 함수
        // Thread를 실행하기 위해 excute(~)에서 전달한 값을 인자로 받습니다.
        protected String doInBackground(Void...args) {
            URL url = null;
            HttpURLConnection conn = null;
            String urlStr = "";
            urlStr = "http://"+getString(R.string.ip_address)+":8080/SkhuGlocalitWebProject/bookdream/initGiveInfo";
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
                int responseCode = conn.getResponseCode();
                Log.d("D", responseCode+"");
                if (responseCode == HttpURLConnection.HTTP_OK) {    // 송수신이 잘되면 - 데이터를 받은 것입니다.
                    Log.d("coded", "들어옴");
                    ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                    HashMap<String, HashMap<String,String>> dataMap = (HashMap<String, HashMap<String,String>>)ois.readObject();
                    ois.close();

                        int i = dataMap.size()-1;
                        HashMap<String, String> map = dataMap.get(i+"");
                        Message msg_dream = handler.obtainMessage();
                        msg_dream.what = 5;
                        Bundle b = new Bundle();
                        b.putString("title" , map.get("title"));
                        msg_dream.setData(b);
                        handler.sendMessage(msg_dream);

                }
                conn.disconnect();
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Error 발생", Toast.LENGTH_SHORT).show();
                Log.e("ERR", "InitAsyncThread ERR : " + e.getMessage());
            }

            return "";
        }

        // doInBackground(~)에서 호출되어 주로 UI 관련 작업을 하는 함수
        protected void onProgressUpdate(String... progress) {}

        // Thread를 처리한 후에 호출되는 함수
        // doInBackground(~)의 리턴값을 인자로 받습니다.
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        // AsyncTask.cancel(true) 호출시 실행되어 thread를 취소 합니다.
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    /*------------------------------------------
        DB에서 받아온 데이터를 추가하는 메소드
    --------------------------------------------*/
    // 자유 게시판
    public void addItem_freeboard(String fTitle) {
        FreeNoticeBoard_ListData addInfo = null;
        addInfo = new FreeNoticeBoard_ListData();

        ArrayList<FreeNoticeBoard_ListData> free_ListData = new ArrayList<>();

        //디비에서 받아온 데이터 저장
        addInfo.fTitle = fTitle; //제목
        free_ListData.add(addInfo);

        home_freeBoard.setText(fTitle);
        Log.d("홈화면 자유게시판 addItem","완료");
    }
    // 정보게시판
    public void addItem_infoboard(String infoTitle) {
        InfoNoticeBoard_ListData addInfo = null;
        addInfo = new InfoNoticeBoard_ListData();

        ArrayList<InfoNoticeBoard_ListData> info_ListData = new ArrayList<>();

        //디비에서 받아온 데이터 저장
        addInfo.infoTitle = infoTitle; //제목
        info_ListData.add(addInfo);

        home_infoBoard.setText(infoTitle);
        Log.d("홈화면 정보게시판 addItem","완료");
    }
    // 북드림 게시판(요청)
    public void addItem_bookdream(String bookTitle){
        RequestListData addInfo = null;
        addInfo = new RequestListData();

        ArrayList<RequestListData> book_ListData = new ArrayList<>();

        //디비에서 받아온 데이터 저장
        addInfo.mTitle = bookTitle;
        book_ListData.add(addInfo);

        home_bookDream.setText(bookTitle);
        Log.d("홈화면 북드림(요청) addItem","완료");
    }
    // 북드림 게시판(드림)
    public void addItem_dream(String bookTitle){
        GiveListData addInfo = null;
        addInfo = new GiveListData();

        ArrayList<GiveListData> dream_ListData = new ArrayList<>();

        //디비에서 받아온 데이터 저장
        addInfo.mTitle = bookTitle;
        dream_ListData.add(addInfo);

        home_bookDream2.setText(bookTitle);
        Log.d("홈화면 북드림(드림) addItem","완료");
    }
    // 학과 게시판
    public void addItem_department(String departTitle){
        DepartmentNoticeBoardListData addInfo = null;
        addInfo = new DepartmentNoticeBoardListData();

        ArrayList<DepartmentNoticeBoardListData> book_ListData = new ArrayList<>();

        //디비에서 받아온 데이터 저장
        addInfo.mTitle = departTitle;
        book_ListData.add(addInfo);

        home_departmentBoard.setText(departTitle);
        Log.d("홈화면 학과게시판 addItem","완료");
    }

    /**
     * 리스트 뷰 관련
     */
    private class ViewHolder {
        public TextView sTitle;
        public TextView sBranch;
        public TextView sUp;
        public TextView sDown;
    }

    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<RecommendSearchListData> searchListData = new ArrayList<>();

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        // 총 몇개의 리스트가 있는지 반환
        @Override
        public int getCount() {
            return searchListData.size();
        }

        // 사용자가 선택한 아이템을 반환
        @Override
        public Object getItem(int position) {
            return searchListData.get(position);
        }

        // ID(몇 번째 아이템인지) 반환
        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.recommend_search_item, null);

                holder.sTitle = (TextView) convertView.findViewById(R.id.search_title);
                holder.sBranch = (TextView) convertView.findViewById(R.id.search_branch);
                holder.sUp = (TextView) convertView.findViewById(R.id.search_up);
                holder.sDown = (TextView) convertView.findViewById(R.id.search_down);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            RecommendSearchListData searchData = searchListData.get(position); // DListData로부터 해당 아이템의 데이터를 받아온다.

            holder.sTitle.setText(searchData.sTitle);
            holder.sBranch.setText(searchData.sBranch);
            holder.sUp.setText(searchData.sUp);
            holder.sDown.setText(searchData.sDown);
            return convertView;
        }

        /*
            리스트에 아이템을 추가하는 메소드
        */
        public void addItem(String title, String branch, String longitude, String latitude, String up, String down) {
            RecommendSearchListData addInfo = new RecommendSearchListData();
            addInfo.sTitle = title;
            addInfo.sBranch = branch;
            addInfo.sLongitude = longitude;
            addInfo.sLatitude = latitude;
            addInfo.sUp = up;
            addInfo.sDown = down;
            searchListData.add(addInfo);
        }

        // 리스트를 새로고침 하는 메소드
        public void clear() {
            searchListData.clear();
        }

        // 데이터가 바뀌었음을 DB에 알려주는 메소드
        public void dataChange() {
            listViewAdapter.notifyDataSetChanged();
        }
    }
}
