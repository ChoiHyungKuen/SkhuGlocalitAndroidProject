package com.example.user_16.skhuglocalitandroidproject.BookDream;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.user_16.skhuglocalitandroidproject.DBManager;
import com.example.user_16.skhuglocalitandroidproject.R;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class GiveMatchMessageActivity extends AppCompatActivity {
    DatePicker datePicker;
    TimePicker timePicker;
    EditText edit_where , edit_content, edit_phone;
    GiveMatchAsyncThread backgroundMatchThread;
    Button acceptBtn, cancelBtn;
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();

            if (b.getString("result").equals("success")) {
                Toast.makeText(getApplicationContext(), "BOOK:DREAM을 완료했습니다.", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), "이미 처리 하셨거나 에러가 발생했습니다. 잠시 후 다시 시도해주세요", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookdream_give_presentcondition_dialog);
        final Intent intent = getIntent();
        datePicker = (DatePicker) findViewById(R.id.datePicker);
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        edit_where = (EditText) findViewById(R.id.edit_where);
        edit_content = (EditText) findViewById(R.id.edit_content);
        edit_phone = (EditText) findViewById(R.id.edit_phone);
        edit_phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        acceptBtn = (Button) findViewById(R.id.accept_btn);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        acceptBtn.setVisibility(View.VISIBLE);
        cancelBtn.setVisibility(View.VISIBLE);
        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 선배가 설정한 약속 날짜를 datePicker로부터 저장
                String date = String.format("%d - %d - %d", datePicker.getYear(), datePicker.getMonth()+1, datePicker.getDayOfMonth());
                // 선배가 설정한 약속 시간을 timePicker로부터 저장
                String time = "";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    time = String.format("%d 시 %d 분", timePicker.getHour(), timePicker.getMinute());
                } else {
                    time = String.format("%d 시 %d 분", timePicker.getCurrentHour(), timePicker.getCurrentMinute());
                }

                Log.d("접속", "접속dd ");
                // 선배의 정보와 드림 메세지(선배 이름, 연락처, 약속 장소 등)을 보냄

                        /*
                        String[] demandUserInfo = view_user.getText().toString().split(" ");
                        backgroundCompletePrecentConditionThread = new CompletePrecentCondionInfoAsyncThread();
                        backgroundCompletePrecentConditionThread.execute(
                                gNo+"",gTitle,demandUserInfo[0], demandUserInfo[1],
                                date, time, edit_where.getText().toString(),
                                edit_content.getText().toString(), edit_phone.getText().toString());
                final DBManager dbManager = new DBManager(getApplicationContext(), "app_data.db", null, 1);
                final HashMap<String, String> dataMap = dbManager.getMemberInfo();
                String user = dataMap.get("id") + " " + dataMap.get("name");*/
                backgroundMatchThread = new GiveMatchAsyncThread();
                backgroundMatchThread.execute(
                        intent.getStringExtra("title"), intent.getStringExtra("giveUser"), intent.getStringExtra("requestUser"),
                        date, time, edit_where.getText().toString(),
                        edit_content.getText().toString(), edit_phone.getText().toString(),"code");
                finish();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
    class GiveMatchAsyncThread extends AsyncTask<String, String, String> {
        // Thread를 시작하기 전에 호출되는 함수
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Thread의 주요 작업을 처리 하는 함수
        // Thread를 실행하기 위해 excute(~)에서 전달한 값을 인자로 받는다.
        protected String doInBackground(String...args) {
            URL url = null;
            HttpURLConnection conn = null;
            String urlStr = "";

            HashMap<String, String> dataMap = new HashMap<>();

            dataMap.put("title", args[0]);
            dataMap.put("giveUser", args[1]);
            dataMap.put("requestUser", args[2]);
            dataMap.put("date", args[3]);
            dataMap.put("time", args[4]);
            dataMap.put("where",args[5]);
            dataMap.put("content", args[6]);
            dataMap.put("phone", args[7]);
            dataMap.put("state", args[8]);
            urlStr = "http://"+getString(R.string.ip_address)+":8080/SkhuGlocalitWebProject/bookdream/giveMatch";
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
                    ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                    HashMap<String, String> stringDataMap = (HashMap<String, String>)ois.readObject();
                    ois.close();
                    Message msg = handler.obtainMessage();
                    Bundle b = new Bundle();
                    Log.d("test_cnt",stringDataMap.size()+"");
                    if(stringDataMap .size() == 0){
                        b.putString("result", "fail");
                    } else {
                        b.putString("result", "success");
                    }
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
        protected void onProgressUpdate(String... progress) {}

        // Thread를 처리한 후에 호출되는 함수
        // doInBackground(~)의 리턴값을 인자로 받는다
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        // AsyncTask.cancel(true) 호출시 실행되어 thread를 취소한다.
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}

