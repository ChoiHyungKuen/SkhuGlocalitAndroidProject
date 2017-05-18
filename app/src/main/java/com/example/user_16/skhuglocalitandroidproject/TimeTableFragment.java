package com.example.user_16.skhuglocalitandroidproject;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user_16.skhuglocalitandroidproject.BookDream.GiveFragment;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;

import static android.content.Context.MODE_PRIVATE;


public class TimeTableFragment extends Fragment {
    private TextView authTx;
    private EditText authId, authPw;
    private Button authBtn;
    private SharedPreferences auth_pref;
    private SharedPreferences.Editor editor;
    private View rootView;
    private GetTimeTableInfoAsyncThread backgroundGetTimeTableInfoThread;
    final Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            Bundle b = msg.getData();
            if(b.get("state") != null && b.get("state").equals("fail")) {
                String message = (String) b.get("message");
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("경고!");
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.setIcon(R.drawable.alert);

                alert.setMessage(message);
                alert.show();

                //Toast.makeText(getContext(), "아이디나 비밀번호가 틀렸습니다. 잠시 후 시도해주세요.", Toast.LENGTH_LONG).show();
            } else {
                String[][] timetableInfo = (String[][]) msg.obj;

                succeedAuthStudent();
                final TableLayout table = new TableLayout(getActivity());
                table.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
                table.setStretchAllColumns(true);
                table.setShrinkAllColumns(true);
                TableRow rowTitle = new TableRow(getActivity());
                rowTitle.setGravity(Gravity.CENTER_HORIZONTAL);
                TableRow row = new TableRow(getActivity());
                TableRow rowDayLabels = new TableRow(getActivity());
                TableRow rowHighs = new TableRow(getActivity());
                TableRow rowLows = new TableRow(getActivity());
                TableRow rowConditions = new TableRow(getActivity());
                rowConditions.setGravity(Gravity.CENTER);

                TextView empty = new TextView(getActivity());

                // title column/row
                TextView title = new TextView(getActivity());
                title.setText(b.get("title") +" 시간표");

                title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(Typeface.SERIF, Typeface.BOLD);

                TableRow.LayoutParams params = new TableRow.LayoutParams();
                params.span = 6;

                rowTitle.addView(title, params);
                table.addView(rowTitle);
                String [] dayName = {" " , "월", "화","수","목","금"};
                row=new TableRow(getActivity());
                row.setLayoutParams( new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));
                for(int i=0; i<6; i++) {
                    TextView tx = new TextView(getActivity());
                    tx.setText(dayName[i]);
                    tx.setTypeface(Typeface.DEFAULT_BOLD);
                    tx.setGravity(Gravity.CENTER_HORIZONTAL);
                    row.addView(tx);
                }
                table.addView(row);
                for(int i=0; i<13; i++) {
                    row=new TableRow(getActivity());
                    //row.setWeightSum(12f);
                    //row.setLayoutParams( new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));

                    for(int j=0; j<6; j++) {
                        String s = "";
                        if(!timetableInfo[i][j].equals(" ")) {
                            StringTokenizer stz = new StringTokenizer(timetableInfo[i][j], "#");
                            if (stz.countTokens() >= 2) {
                                while (stz.hasMoreTokens()) {
                                    StringTokenizer stzz = new StringTokenizer(stz.nextToken(), "&");
                                    while (stzz.hasMoreTokens()) {
                                        s += stzz.nextToken() + "\n";
                                    }
                                }
                            } else {
                                while (stz.hasMoreTokens()) {
                                    StringTokenizer stzz = new StringTokenizer(stz.nextToken(), "&");
                                    while (stzz.hasMoreTokens()) {
                                        s += stzz.nextToken() + "\n";
                                    }
                                }
                            }
                        } else {
                            s= " ";
                        }
                        TextView tx = new TextView(getActivity());
                        if(j==0)
                            tx.setPadding(10, 0, 0, 0);
                        tx.setText(s);
                        tx.setTextSize(10f);
                        tx.setTypeface(Typeface.DEFAULT_BOLD);
                        if(j!=0 && !s.equals(" ")) {
                            Random rnd = new Random();
                            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                            tx.setBackgroundColor(color);
                        }
                        row.addView(tx);
                    }

                    table.addView(row);
                }

                table.setBackgroundResource(R.drawable.row_border);
/*                table.addView(rowTitle);
                table.addView(rowDayLabels);
                table.addView(rowHighs);
                table.addView(rowLows);*/
                ScrollView sv = new ScrollView(getActivity());
                sv.addView(table);
                LinearLayout layout = new LinearLayout(getActivity());

                layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(sv);
                ViewGroup viewGroup =(ViewGroup) getView();
                viewGroup.removeAllViews();
                viewGroup.addView(layout);
            }

        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.timetable_fragment, container, false);
        authTx = (TextView) rootView.findViewById(R.id.auth_timetable_tx);
        authId = (EditText) rootView.findViewById(R.id.auth_timetable_id_edit);
        authPw  = (EditText) rootView.findViewById(R.id.auth_timetable_pw_edit);
        authBtn = (Button) rootView.findViewById(R.id.auth_timetable_btn);
        authTx.setText("forest 사이트로부터 시간표를 받아옵니다. \n"
                    +"학사 시스템 forest 홈페이지의 아이디와 비밀번호를 입력하여 \n"
                    +"아래의 인증버튼을 누르세요.");
        authBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DBManager dbManager = new DBManager(getContext(), "app_data.db", null, 1);
                final HashMap <String, String> dataMap = dbManager.getMemberInfo();
                backgroundGetTimeTableInfoThread = new GetTimeTableInfoAsyncThread();
                backgroundGetTimeTableInfoThread.execute("auth", authId.getText().toString(),
                        authPw.getText().toString(), dataMap.get("id"));
            }
        });
        if(isAuthStudent()) {

            final DBManager dbManager = new DBManager(getContext(), "app_data.db", null, 1);
            final HashMap <String, String> dataMap = dbManager.getMemberInfo();
            backgroundGetTimeTableInfoThread = new GetTimeTableInfoAsyncThread();
            backgroundGetTimeTableInfoThread.execute(dataMap.get("id"));

        }
        return rootView;
    }
    public  boolean isAuthStudent() {
        auth_pref = getActivity().getSharedPreferences("auth_Info", MODE_PRIVATE);
        if (!auth_pref.getString("auth", "").equals("")) {

            editor = auth_pref.edit();
            editor.putString("auth", "");
            editor.commit();
            return true;
        } else {
            return false;
        }
    }
    public  void succeedAuthStudent() {
        Log.d("auth","초기화 작업!");
            auth_pref = getActivity().getSharedPreferences("auth_Info",MODE_PRIVATE);
            editor = auth_pref.edit();
            editor.putString("auth", "success");
            editor.commit();
    }
    public class GetTimeTableInfoAsyncThread extends AsyncTask<String, String, String> {

        ProgressDialog pd=new ProgressDialog(getActivity());
        // Thread를 시작하기 전에 호출되는 함수
        protected void onPreExecute() {
            super.onPreExecute();

            pd.setTitle("정보 받아오는 중...");
            pd.setMessage("잠시만 기다려주세요. \n 시간표 데이터를 받아오고 있습니다.");
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

            if(args.length==1) {
                dataMap.put("user", args[0]);
            } else {
                dataMap.put("auth", args[0]);
                dataMap.put("id",args[1]);
                dataMap.put("pw", args[2]);
                dataMap.put("user", args[3]);
            }
            urlStr = "http://"+getString(R.string.ip_address)+":8080/ForestWebProject/parse/timetable";
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
                        b.putString("title", stateDataMap.get("title"));
                        String[][] timetableInfo = (String[][]) ois.readObject();
                        msg.obj = timetableInfo;
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

