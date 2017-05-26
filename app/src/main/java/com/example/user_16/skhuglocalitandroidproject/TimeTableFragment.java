package com.example.user_16.skhuglocalitandroidproject;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
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
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;

import static android.content.Context.MODE_PRIVATE;
import static com.example.user_16.skhuglocalitandroidproject.R.id.textView;


public class TimeTableFragment extends Fragment {
    private TextView authTx;
    private EditText authId, authPw;
    private Button authBtn;
    private SharedPreferences auth_pref, color_pref;
    private SharedPreferences.Editor editor;
    private View rootView;
    private GetTimeTableInfoAsyncThread backgroundGetTimeTableInfoThread;
    final Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            Bundle b = msg.getData();

            String[][] timetableInfo = (String[][]) msg.obj;

            final TableLayout table = new TableLayout(getActivity());
            table.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));

            TableRow rowTitle = new TableRow(getActivity());
            rowTitle.setGravity(Gravity.CENTER_HORIZONTAL);
            TableRow row = new TableRow(getActivity());

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
            //row.setBackgroundResource(R.drawable.row_border);
            for(int i=0; i<6; i++) {

                TextView tx = new TextView(getActivity());
                tx.setText(dayName[i]);
                tx.setTypeface(Typeface.DEFAULT_BOLD);
                tx.setGravity(Gravity.CENTER_HORIZONTAL);
                tx.setBackgroundResource(R.drawable.row_border);
                if(i==0)
                    tx.setWidth(155);
                else
                    tx.setWidth(260);
                row.addView(tx);
            }
            table.addView(row);
            int[] colors = new int[5];
            for(int i=0; i<5; i++) {
                Random rnd = new Random();
                int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                colors[i] = color;
            }
            for(int i=0; i<13; i++) {
                row=new TableRow(getActivity());
                //row.setWeightSum(12f);
                //row.setLayoutParams( new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));
                int subjectCnt =1;  // 75분 수업인경우 한셀에 두개과목이 들어감
                for(int j=0; j<6; j++) {

                    String s = "";
                    if(!timetableInfo[i][j].equals(" ")) {
                        StringTokenizer stz = new StringTokenizer(timetableInfo[i][j], "#");
                        if (stz.countTokens() >= 2) {
                            subjectCnt=2;
                            while (stz.hasMoreTokens()) {
                                StringTokenizer stzz = new StringTokenizer(stz.nextToken(), "&");
                                while (stzz.hasMoreTokens()) {
                                    s += stzz.nextToken() + "\n";
                                }
                                s+="\n";
                            }
                        } else {
                            subjectCnt=1;
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
                    final TextView tx = new TextView(getActivity());
                    tx.setText(s);
                    if(j==0) {
                        tx.setPadding(10, 0, 0, 0);
                        tx.setTextSize(12f);
                        tx.setWidth(155);
                        tx.setTextColor(Color.BLACK);

                    } else {
                        tx.setPadding(3, 0, 5, 0);
                        tx.setWidth(260);
                        tx.setTextSize(9.5f);
                        tx.setHeight(230*subjectCnt);
                        tx.setTextColor(Color.WHITE);
                        if(!s.equals(" ")) {
                            tx.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(final View view) {
                                ColorPickerDialogBuilder
                                        .with(getContext())
                                        .setTitle("Choose color")
                                        .initialColor(Color.RED)
                                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                                        .density(12)
                                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                                            @Override
                                            public void onColorSelected(int selectedColor) {
                                                Toast.makeText(getContext(), "onColorSelected: 0x" + Integer.toHexString(selectedColor), Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .setPositiveButton("ok", new ColorPickerClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                                view.setBackgroundColor(selectedColor);
                                                setBackgroundColor((String) ((TextView)view).getHint(), selectedColor);
                                            }
                                        })
                                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        })
                                        .build()
                                        .show();
                                return true;
                                }
                        });
                        }
                    }
                    tx.setTypeface(Typeface.DEFAULT_BOLD);
                    if(j!=0 && !s.equals(" ")) {
                        tx.setHint(i+"%"+j);
                        if(getBackgroundColor(i+"%"+j) == 0) {
                            tx.setBackgroundColor(colors[j-1]);
                            setBackgroundColor(i+"%"+j, colors[j-1]);
                        } else {
                            tx.setBackgroundColor(getBackgroundColor(i+"%"+j));
                        }
                    }
                    row.addView(tx);
                }

                table.addView(row);
            }

            table.setBackgroundResource(R.drawable.row_border);
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

    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.timetable_fragment, container, false);
        final DBManager dbManager = new DBManager(getContext(), "app_data.db", null, 1);
        final HashMap <String, String> dataMap = dbManager.getMemberInfo();
        if(isFirstAccess()) {
            initTimeTable();
        }
        if(isAuthStudent()) {
            backgroundGetTimeTableInfoThread = new GetTimeTableInfoAsyncThread();
            backgroundGetTimeTableInfoThread.execute(dataMap.get("id"));
        }
        return rootView;
    }
    public  boolean isAuthStudent() {
        auth_pref = getActivity().getSharedPreferences("auth_Info", MODE_PRIVATE);
        if (!auth_pref.getString("auth", "").equals("")) {
/*
            editor = auth_pref.edit();
            editor.putString("auth", "");
            editor.commit();*/
            return true;
        } else {
            return false;
        }
    }
    public  boolean isFirstAccess() {
        auth_pref = getActivity().getSharedPreferences("auth_Info", MODE_PRIVATE);
        if (auth_pref.getString("access", "").equals("")) {
            return true;
        } else {
            return false;
        }
    }
    public void initTimeTable() {
        auth_pref = getActivity().getSharedPreferences("auth_Info", MODE_PRIVATE);
        editor = auth_pref.edit();
        editor.putString("access", "OK");
        editor.commit();
        saveAllBackgroundColors();
    }
    public void saveAllBackgroundColors() {
        Log.d("auth","초기화 작업!");
        color_pref = getActivity().getSharedPreferences("color_Info",MODE_PRIVATE);
        editor = color_pref.edit();
        for(int i=0; i<13; i++) {
            for(int j=0; j<5; j++) {
                editor.putString("color_"+i+"_"+j, 0+"");
                editor.commit();
            }
        }
    }
    public int getBackgroundColor(String key) {
        Log.d("auth","초기화 작업!");
        color_pref = getActivity().getSharedPreferences("color_Info",MODE_PRIVATE);

        StringTokenizer stz = new StringTokenizer(key, "%");
        String i=stz.nextToken(), j =stz.nextToken();
        String colorCode = color_pref.getString("color_"+i+"_"+j , "");
        if(colorCode.equals(""))
            return 0;

        return Integer.parseInt(colorCode);
    }
    public void setBackgroundColor(String key, int color) {
        Log.d("auth","초기화 작업!");
        color_pref = getActivity().getSharedPreferences("color_Info",MODE_PRIVATE);
        StringTokenizer stz = new StringTokenizer(key, "%");
        String i=stz.nextToken(), j =stz.nextToken();
        editor = color_pref.edit();
        editor.putString("color_"+i+"_"+j, color+"");
        editor.commit();
    }
    public class GetTimeTableInfoAsyncThread extends AsyncTask<String, String, String> {

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
                    b.putString("title", stateDataMap.get("title"));
                    String[][] timetableInfo = (String[][]) ois.readObject();
                    msg.obj = timetableInfo;

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

