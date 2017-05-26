
package com.example.user_16.skhuglocalitandroidproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;

import static android.content.Context.MODE_PRIVATE;


public class HomeFragment extends Fragment {
    private LinearLayout attendanceLayout;
    private TextView btn_logout;
    private SharedPreferences login_pref, map_pref;
    private SharedPreferences auth_pref;
    SharedPreferences.Editor editor;
    private GetAttendanceInfoAsyncThread backgroundGetAttendanceInfoThread;
    final Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            Bundle b = msg.getData();

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
            backgroundGetAttendanceInfoThread = new GetAttendanceInfoAsyncThread();
            backgroundGetAttendanceInfoThread.execute(dbManager.getMemberInfo().get("id"));
        }
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
}
