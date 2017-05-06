package com.example.user_16.skhuglocalitandroidproject.BookDream;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user_16.skhuglocalitandroidproject.R;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MatchActivity extends AppCompatActivity {
    private Button confirmBtn,denyBtn;
    private TextView tx;
    private AcceptMatchAsyncThread backgroundAcceptMatchThread;
    private CancelMatchAsyncThread backgroundCancelMatchThread;


    final Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            Bundle b = msg.getData();

            if (b.getString("present_condition") != null) {
                if (b.getString("result").equals("success")) {
                    Toast.makeText(getApplicationContext(), "메시지 전송 되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "이미 처리 하셨거나 에러가 발생했습니다. 잠시 후 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);
        tx = (TextView) findViewById(R.id.match_message_tx);
        final Intent intent = getIntent();
        tx.setText(intent.getStringExtra("giveUser") +"님이\n"+ intent.getStringExtra("title")+" 책을 제공한다고 합니다.\n"
                + "일시 : "+intent.getStringExtra("date") +" 시간 : "+ intent.getStringExtra("time") +"\n 장소 : " +intent.getStringExtra("where") +"\n"
                + " 내용 : " + intent.getStringExtra("content") + "\n 연락처 : " + intent.getStringExtra("phone")) ;

        confirmBtn = (Button) findViewById(R.id.confirm_btn);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                backgroundAcceptMatchThread = new AcceptMatchAsyncThread();
                backgroundAcceptMatchThread.execute(intent.getStringExtra("giveUser"), intent.getStringExtra("requestUser"),
                        intent.getStringExtra("title"), intent.getStringExtra("state"));
                finish();
            }
        });
        denyBtn = (Button) findViewById(R.id.deny_btn);
        denyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText etEdit = new EditText(MatchActivity.this);
                final android.support.v7.app.AlertDialog.Builder builder= new android.support.v7.app.AlertDialog.Builder(MatchActivity.this); //AlertDialog.Builder 객체 생성

                builder.setTitle("확인창");
                builder.setMessage("거절 사유를 작성해주세요!");
                builder.setView(etEdit);
                // OK 버튼 이벤트
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String reasonMsg = etEdit.getText().toString();
                        if (reasonMsg.equals("")) {
                             Toast.makeText(getApplicationContext(), "제대로 입력해주세요.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        backgroundCancelMatchThread = new CancelMatchAsyncThread();
                        backgroundCancelMatchThread.execute(intent.getStringExtra("giveUser"), intent.getStringExtra("requestUser"),
                                reasonMsg);
                        finish();
                    }
                });
                // Cancel 버튼 이벤트
                builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog dialog = builder.create();

                //Dialog의 바깥쪽을 터치했을 때 Dialog를 없앨지 설정
                dialog.setCanceledOnTouchOutside(false);//없어지지 않도록 설정

                //Dialog 보이기
                dialog.show();
            }
        });
    }

    public class AcceptMatchAsyncThread extends AsyncTask<String, String, String> {
        // Thread를 시작하기 전에 호출되는 함수
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Thread의 주요 작업을 처리 하는 함수
        // Thread를 실행하기 위해 excute(~)에서 전달한 값을 인자로 받습니다.
        protected String doInBackground(String...args) {
            URL url = null;
            HttpURLConnection conn = null;
            String urlStr = "";
            HashMap<String, String> dataMap = new HashMap<>();

            dataMap.put("giveUser", args[0]);
            dataMap.put("requestUser", args[1]);
            dataMap.put("title", args[2]);
            dataMap.put("state", args[3]);
            urlStr = "http://"+getString(R.string.ip_address)+":8080/SkhuGlocalitWebProject/bookdream/acceptMatch";
            try {
                url = new URL(urlStr);
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
        // doInBackground(~)의 리턴값을 인자로 받습니다.
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        // AsyncTask.cancel(true) 호출시 실행되어 thread를 취소 합니다.
        protected void onCancelled() {
            super.onCancelled();
        }
    }
    public class CancelMatchAsyncThread extends AsyncTask<String, String, String> {
        // Thread를 시작하기 전에 호출되는 함수
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Thread의 주요 작업을 처리 하는 함수
        // Thread를 실행하기 위해 excute(~)에서 전달한 값을 인자로 받습니다.
        protected String doInBackground(String...args) {
            URL url = null;
            HttpURLConnection conn = null;
            String urlStr = "";
            HashMap<String, String> dataMap = new HashMap<>();

            dataMap.put("giveUser", args[0]);
            dataMap.put("requestUser", args[1]);
            dataMap.put("reason", args[2]);
            urlStr = "http://"+getString(R.string.ip_address)+":8080/SkhuGlocalitWebProject/bookdream/cancelMatch";
            try {
                url = new URL(urlStr);
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
