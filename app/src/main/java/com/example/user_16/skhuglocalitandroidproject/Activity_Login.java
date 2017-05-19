
package com.example.user_16.skhuglocalitandroidproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user_16.skhuglocalitandroidproject.BookDream.FirebaseInstanceIDService;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class Activity_Login extends AppCompatActivity {

    private EditText text_id, text_pw;
    private CheckBox auto_login;
    private SharedPreferences login_pref, init_pref;
    private SharedPreferences.Editor editor;

    private TextInputLayout TextInputLayout01, TextInputLayout02, TextInputLayout03, TextInputLayout04, TextInputLayout05;
    private EditText EditText_id, EditText_pw, EditText_pwTest, EditText_name, EditText_mail;
    private TextView text_check;
    private Boolean IDflag;
    private ArrayList<String> memberIdList = new ArrayList<>();

    private AddMemberAsyncThread addMemberAsyncThread;
    private IDCheckAsyncThread idCheckAsyncThread;
    private LoginAsyncThread loginAsyncThread;

    final Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.obj.toString()) {
                case "Login_Success" :
                    String token = FirebaseInstanceId.getInstance().getToken();
                    if(auto_login.isChecked()){
                        Log.d("자동로그인 들어왔어","!!!!!!!!!!!!!!!!!!!!!1");
                        Bundle loginInfo = msg.getData();
                        login_pref = getSharedPreferences("login_Info",MODE_PRIVATE);
                        editor = login_pref.edit();
                        editor.putString("id", loginInfo.getString("id"));
                        Log.d("저장한 아이디",login_pref.getString("id",""));
                        editor.putString("pw", loginInfo.getString("pw"));
                        Log.d("저장한 비번",login_pref.getString("pw",""));
                        editor.commit();
                    }
                    break;
                case "Login_Fail" :
                    Toast.makeText(getApplicationContext(), "학번이나 비밀번호를 잘못입력하셨습니다.", Toast.LENGTH_SHORT).show();
                    break;
                case "Join_Success" :
                    Toast.makeText(getApplicationContext(), "회원가입 완료", Toast.LENGTH_SHORT).show();
                    break;
                case "not exist":
                    IDflag = true;
                    Toast.makeText(getApplicationContext(),R.string.Check_Success,Toast.LENGTH_SHORT).show();
                    break;
                case "exist" :
                    IDflag = false;
                    Toast.makeText(getApplicationContext(),R.string.Check_Fail,Toast.LENGTH_SHORT).show();
                    break;
                case "Error" :
                    Toast.makeText(getApplicationContext(), "Error 발생", Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initProgram();

        text_id = (EditText)findViewById(R.id.text_id);
        text_pw = (EditText)findViewById(R.id.text_pw);

        auto_login = (CheckBox)findViewById(R.id.check_autoLogin);

        login_pref = getSharedPreferences("login_Info",MODE_PRIVATE);
        if(!login_pref.getString("id","").equals("") && !login_pref.getString("pw","").equals("")){
            Toast.makeText(getApplicationContext(), "자동로그인 되었습니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

    }

    /*
        어플리케이션 종료시 쓰레드의 종료를 요청하는 메소드
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (addMemberAsyncThread.getStatus() == AsyncTask.Status.RUNNING) {
                addMemberAsyncThread.cancel(true);
            }

            if (idCheckAsyncThread.getStatus() == AsyncTask.Status.RUNNING) {
                idCheckAsyncThread.cancel(true);
            }

            if (loginAsyncThread.getStatus() == AsyncTask.Status.RUNNING) {
                loginAsyncThread.cancel(true);
            }
        } catch (Exception e) {}
    }

    public void onClick_Login(View view) {
        String id = text_id.getText().toString();
        String pw = text_pw.getText().toString();

        if(id.length() == 9 && pw.length() > 0){

            init_pref = getSharedPreferences("init_Info",MODE_PRIVATE);
            loginAsyncThread = new LoginAsyncThread();
            loginAsyncThread.execute(id, pw);
        } else {
            Toast.makeText(getApplicationContext(), "아이디나 비밀번호를 제대로 입력해 주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClick_Join(View view) {
        LayoutInflater inflater = Activity_Login.this.getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.activity_join, null);

        TextInputLayout01 = (TextInputLayout) alertLayout.findViewById(R.id.TextInputLayout01);
        TextInputLayout02 = (TextInputLayout) alertLayout.findViewById(R.id.TextInputLayout02);
        TextInputLayout03 = (TextInputLayout) alertLayout.findViewById(R.id.TextInputLayout03);
        TextInputLayout04 = (TextInputLayout) alertLayout.findViewById(R.id.TextInputLayout04);
        TextInputLayout05 = (TextInputLayout) alertLayout.findViewById(R.id.TextInputLayout05);

        EditText_id = (EditText) alertLayout.findViewById(R.id.EditText_id);
        EditText_pw = (EditText) alertLayout.findViewById(R.id.EditText_pw);
        EditText_pwTest = (EditText) alertLayout.findViewById(R.id.EditText_pwTest);
        EditText_name = (EditText) alertLayout.findViewById(R.id.EditText_name);
        EditText_mail = (EditText) alertLayout.findViewById(R.id.EditText_mail);

        text_check = (TextView) alertLayout.findViewById(R.id.text_check);
        text_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                idCheckAsyncThread = new IDCheckAsyncThread();
                idCheckAsyncThread.execute(EditText_id.getText().toString());
                Log.d("아이디 서버로 보냄 테스트",EditText_id.getText().toString());
              /*  try {*//*
                    String input_id = EditText_id.getText().toString();
                    if(CheckID(input_id)){
                        IDflag = true;
                        Toast.makeText(getApplicationContext(),R.string.Check_Success,Toast.LENGTH_SHORT).show();
                    }
                    else {
                        IDflag = false;
                        Toast.makeText(getApplicationContext(),R.string.Check_Fail,Toast.LENGTH_SHORT).show();
                    }*//*
                } catch (Exception e) {
                    IDflag = false;
                    Toast.makeText(getApplicationContext(), R.string.E, Toast.LENGTH_SHORT).show();
                    Log.e("ERR", "ID CHECK ERR : " + e.getMessage());
                }*/
            }
        });

        AlertDialog.Builder buider = new AlertDialog.Builder(Activity_Login.this); //AlertDialog.Builder 객체 생성
        buider.setCancelable(false);
        buider.setView(alertLayout); //위에서 inflater가 만든 dialogView 객체 세팅 (Customize)

        buider.setPositiveButton("확인", null); // 회원 가입에 대한 리스너는 아래서 붙여준다.
        buider.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = buider.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface d) {

                Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean flag = true;
                        String dbId = EditText_id.getText().toString(),
                                dbPw = EditText_pw.getText().toString(),
                                dbName = EditText_name.getText().toString(),
                                dbEmail = EditText_mail.getText().toString();
                        try {
                            if(flag) {
                                flag = IDflag;
                            }

                            if(flag) {
                                if(dbId.length() == 9){
                                    TextInputLayout01.setErrorEnabled(false);
                                }
                                else {
                                    flag = false;
                                    TextInputLayout01.setError("학번 9자리를 입력해주세요");
                                    requestFocus(EditText_id);
                                }
                            }

                            if(flag) {
                                if(dbPw.length() > 0) {
                                    TextInputLayout02.setErrorEnabled(false);
                                }
                                else {
                                    flag = false;
                                    TextInputLayout02.setError("비밀번호를 입력해주세요");
                                    requestFocus(EditText_pw);
                                }
                            }

                            if(flag) {
                                if(EditText_pwTest.getText().toString().equals(dbPw)){
                                    TextInputLayout03.setErrorEnabled(false);
                                }
                                else {
                                    flag = false;
                                    TextInputLayout03.setError("비밀번호가 일치하지 않습니다");
                                    requestFocus(EditText_pwTest);
                                }
                            }

                            if(flag) {
                                if(dbName.length() > 0) {
                                    TextInputLayout04.setErrorEnabled(false);
                                }
                                else {
                                    flag = false;
                                    TextInputLayout04.setError("이름을 입력해주세요");
                                    requestFocus(EditText_name);
                                }
                            }

                            if(flag) {
                                if(dbEmail.contains("@")&&dbEmail.contains(".")){
                                    TextInputLayout05.setErrorEnabled(false);
                                }
                                else {
                                    flag = false;
                                    TextInputLayout05.setError("정확한 E-mail주소를 입력해주세요");
                                    requestFocus(EditText_mail);
                                }
                            }

                            if(flag) {
                                AddMemberDB(dbId, dbPw, dbName, dbEmail);
                                Toast.makeText(getApplicationContext(), "회원가입 중 \n 완료가 될 때 까지 대기해주세요", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), R.string.E, Toast.LENGTH_SHORT).show();
                            Log.e("ERR", "JOIN ERR : " + e.getMessage());
                        }
                    }
                });
            }
        });
        //Dialog의 바깥쪽을 터치했을 때 Dialog를 없앨지 설정
        dialog.setCanceledOnTouchOutside(false);//없어지지 않도록 설정

        //Dialog 보이기
        dialog.show();

    }
/*
    public boolean CheckID(String id){
        boolean check = false;
        if(memberIdList.size() == 0) {
            check = true;
        }
        else {
            for(String getid : memberIdList){
                if(getid.equals(id)){
                    check = false;
                }
                else {
                    check = true;
                }
            }
        }

        return check;
    }
*/
    public  void initProgram() {
            Log.d("init","초기화 작업!");
            init_pref = getSharedPreferences("init_Info",MODE_PRIVATE);
            if(init_pref.getString("init","").equals("")) {         // 처음 프로그램 깔면 실행해야 될 것들을 여기서 처리
                final DBManager dbManager = new DBManager(getApplicationContext(), "app_data.db", null, 1);
                HashMap<String, String> data = dbManager.getMemberInfo();
                if(data.size()!=0)
                    dbManager.deleteAll();
                String token = FirebaseInstanceId.getInstance().getToken();
                Log.d("init", "");
                editor = init_pref.edit();
                editor.putString("init", "init");
                Log.d("init", init_pref.getString("init", ""));
                editor.commit();
            }
    }
    /*
        사용자가 회원가입 시 잘못된 입력을 한 경우, 사용자에게 알려주기 위해 표시하는 메소드드
     */
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    public void AddMemberDB(String id, String pw, String name, String email) {
        addMemberAsyncThread = new AddMemberAsyncThread();
        addMemberAsyncThread.execute(id, pw, name, email);
    }

    public class AddMemberAsyncThread extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            URL url;
            HttpURLConnection conn = null;
            String urlStr = "";

            urlStr = "http://" + getString(R.string.ip_address)+ ":8080/SkhuGlocalitWebProject/member/addMemberInfo";

            try {
                url = new URL(urlStr);
                Log.d("URL","생성------"+urlStr);
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
                stringDataMap.put("id", args[0]);
                stringDataMap.put("pw", args[1]);
                stringDataMap.put("name", args[2]);
                stringDataMap.put("email", args[3]);
                ObjectOutputStream oos = new ObjectOutputStream(conn.getOutputStream());
                oos.writeObject(stringDataMap);
                oos.flush();
                oos.close();
                Log.d("응답메세지","실행중5---"+conn.getResponseCode());
                if (conn.getResponseCode() == 200) { // 서버가 받았다면
                    Message msg = handler.obtainMessage();
                    msg.obj = getString(R.string.JS);
                    handler.sendMessage(msg);
                }
                conn.disconnect();
            }catch (Exception e){
                Message msg = handler.obtainMessage();
                msg.obj = getString(R.string.E);
                Log.d("에러메세지",msg.obj.toString());
                handler.sendMessage(msg);
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
    public class IDCheckAsyncThread extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            URL url;
            HttpURLConnection conn = null;
            String urlStr = "";

            urlStr = "http://" + getString(R.string.ip_address)+ ":8080/SkhuGlocalitWebProject/member/compareId";

            try {
                url = new URL(urlStr);
                Log.d("URL","생성------"+urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                ObjectOutputStream oos = new ObjectOutputStream(conn.getOutputStream());
                ArrayList<String> idInfo = new ArrayList<>();
                idInfo.add(args[0]);
                oos.writeObject(idInfo);    // id를 서버로 보냅니다.
                oos.flush();
                Log.d("응답 메세지","실행중4----"+conn.getResponseCode());
                if(conn.getResponseCode() == 200){
                    ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                    ArrayList<String> resultInfo = (ArrayList<String>) ois.readObject();    // 서버로 부터 존재하는지 결과여부를 받습니다.
                    Message msg = handler.obtainMessage();
                    msg.obj = resultInfo.get(0);
                    handler.sendMessage(msg);
                }

                oos.close();
                conn.disconnect();
            }catch (Exception e){
                Message msg = handler.obtainMessage();
                msg.obj = getString(R.string.E);
                Log.d("에러메세지",msg.obj.toString());
                handler.sendMessage(msg);
                Log.e("ERR", "IdCheckAsyncThread ERR : " + e);

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

    public class LoginAsyncThread extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... args) {
            URL url;
            HttpURLConnection conn = null;
            String urlStr = "";
            final DBManager dbManager = new DBManager(getApplicationContext(), "app_data.db", null, 1);
            urlStr = "http://"+getString(R.string.ip_address)+":8080/SkhuGlocalitWebProject/member/login";
            try {
                String message = getString(R.string.LF);        // 기본 메시지 스트링은 로그인 실패로 초기화
                url = new URL(urlStr);
                Log.d("URL","생성------"+urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);
                conn.setDoOutput(true);
                HashMap <String,String> memberInfoDataMap = new HashMap<>();
                memberInfoDataMap.put("id",args[0]);
                memberInfoDataMap.put("pw",args[1]);
                /* 로그인 할 때 마다 로그인 된 정보가 서버에 가고 기존에 설치된 어플리케이션의 토큰에서
                   정보가 로그인된 정보로 계속 수정된다. */
                String token = FirebaseInstanceId.getInstance().getToken();
                memberInfoDataMap.put("token", token);
                ObjectOutputStream oos = new ObjectOutputStream(conn.getOutputStream());
                oos.writeObject(memberInfoDataMap);
                oos.flush();
                oos.close();
                Log.d("응답코드",conn.getResponseCode() +"");
                if(conn.getResponseCode() == 200){
                    Log.d("응답코드","들어옴");
                    ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                    // 서버로부터 로그인 결과를 받습니다.
                    HashMap<String, String> member = (HashMap<String, String>)ois.readObject();
                    ois.close();
                    Bundle loginInfo = new Bundle();
                    if(member.size() >0 ){  // 회원의 정보가 들어가 있으면 로그인 성공
                        /*
                            그러면 스마트폰자체의 DB에 그값을 저장합니다.
                         */
                        HashMap<String, String> data = dbManager.getMemberInfo();
                        if(data.size()==0)
                            dbManager.insert(member.get("id"), member.get("name"), member.get("email"));
                        // 그리고 MainActivity를 킵니다.
                        Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent1);

                        loginInfo.putString("id",member.get("id"));
                        Log.d("로그인 아이디 담기",loginInfo.getString("id"));
                        loginInfo.putString("pw",args[1]);
                        Log.d("로그인 비번 담기",loginInfo.getString("pw"));
                        message = getString(R.string.LS);   // 성공메시지로 변경
                        finish();
                    }
                    Message msg = handler.obtainMessage();
                    msg.obj = message;  // 로그인 되면 로그인 성공 메시지, 실패하면 실패 메시지가 넣어집니다.
                    msg.setData(loginInfo);
                    handler.sendMessage(msg);   // Toast띄우기 위해 핸들러로 전송
                    Log.d("로그인메시지",message+"보냄");

                    conn.disconnect();
                }
            } catch (Exception e) {
                Message msg = handler.obtainMessage();
                msg.obj = getString(R.string.E)+e;
                Log.d("에러메세지",msg.obj.toString());
                handler.sendMessage(msg);
                Log.e("ERR", "LoginAsyncThread ERR : " + e.getMessage());
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
}
