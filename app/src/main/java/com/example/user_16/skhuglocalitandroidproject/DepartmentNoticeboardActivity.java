package com.example.user_16.skhuglocalitandroidproject;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class DepartmentNoticeboardActivity extends AppCompatActivity {
    private final long	FINSH_INTERVAL_TIME = 2000;         //2초안에 Back 버튼을 2번 누르면 앱 종료 -> 2초
    private long backPressedTime = 0;
    private TextView backBtn;
    private ListView noticeBoardListView = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private ListViewAdapter listViewAdapter = null;
    private InitDepartmentInfoAsyncThread backgroundInitDepartmentInfoThread;
    final Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            Bundle b = msg.getData();

            String[][] departmentInfo = (String[][]) msg.obj;
            for(int i=0; i<departmentInfo.length;i++) {
                String title=departmentInfo[i][0];
                String writer=departmentInfo[i][1];
                String date=departmentInfo[i][2];

                listViewAdapter.addItem(title, writer, date);
                listViewAdapter.dataChange();
            }

        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department_noticeboard_main);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.department_swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(refreshListener);
        noticeBoardListView = (ListView) findViewById(R.id.department_listView);
        noticeBoardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    DepartmentNoticeBoardListData data = (DepartmentNoticeBoardListData) parent.getItemAtPosition(position);
                    if(position!=0) {
                        Intent intent = new Intent(getApplicationContext(), DepartmentNoticeboardContentActivity.class);
                        intent.putExtra("title", data.mTitle);
                        intent.putExtra("writer", data.mWriter);
                        intent.putExtra("date", data.mCreatedDate);
                        Log.d("인텐트!!", intent.toString());
                        startActivity(intent);
                    }
            }

        });
        listViewAdapter = new ListViewAdapter(getApplicationContext());
        noticeBoardListView.setAdapter(listViewAdapter);

        listViewAdapter.addItem("제목", "작성자", "작성날짜");
        listViewAdapter.dataChange();
        backgroundInitDepartmentInfoThread = new InitDepartmentInfoAsyncThread();
        backgroundInitDepartmentInfoThread.execute();

        backBtn = (TextView)findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                DepartmentNoticeboardActivity.this.finish();    // 액티비티 종료
            }
        });
    }

    /*
       ViewHolder란, 이름 그대로 뷰들을 홀더에 꼽아놓듯이 보관하는 객체를 말한다.
       각각의 Row를 그려낼 때 그 안의 위젯들의 속성을 변경하기 위해 findViewById를 호출하는데,
       이것의 비용이 큰것을 줄이기 위해 사용한다. 여기서 게시판의 정보들을 ViewHolder를 이용해 삽입한다.
     */
    private class ViewHolder {
        public TextView mTitle;
        public TextView mWriter;
        public TextView mCreatedDate;
    }

    /*
        요청 프래그먼트의 리스트뷰를 관리하는 메소드
     */
    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<DepartmentNoticeBoardListData> mListData = new ArrayList<>();

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        // 총 몇개의 리스트가 있는지 반환
        @Override
        public int getCount() {
            return mListData.size();
        }

        // 사용자가 선택한 아이템을 반환
        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        // ID(몇 번째 아이템인지) 반환
        @Override
        public long getItemId(int position) {
            return position;
        }

        // 사용자가 선택한 아이템 데이터를 bookdream_request_item 형태에 맞춰 반환한다.
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.department_noticeboard_list_item, null);

                holder.mTitle = (TextView) convertView.findViewById(R.id.department_list_title);
                holder.mWriter = (TextView) convertView.findViewById(R.id.department_list_writer);
                holder.mCreatedDate = (TextView) convertView.findViewById(R.id.department_list_date);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            DepartmentNoticeBoardListData mData = mListData.get(position); // DListData로부터 해당 아이템의 데이터를 받아온다.

            holder.mTitle.setText(mData.mTitle);
            holder.mWriter.setText(mData.mWriter);
            holder.mCreatedDate.setText(mData.mCreatedDate);
            return convertView;
        }

        /*
            리스트에 아이템을 추가하는 메소드
        */
        public void addItem(String mTitle, String mWriter, String mCreatedDate) {
            DepartmentNoticeBoardListData addInfo = null;
            addInfo = new DepartmentNoticeBoardListData();
            addInfo.mTitle = mTitle;
            addInfo.mWriter = mWriter;
            addInfo.mCreatedDate = mCreatedDate;
            mListData.add(addInfo);
        }

        // 리스트를 새로고침 하는 메소드
        public void clear() {
            mListData.clear();
        }

        // 데이터가 바뀌었음을 DB에 알려주는 메소드
        public void dataChange() {
            listViewAdapter.notifyDataSetChanged();
        }
    }

    /*
        사용자가 리스트를 아래로 끌어당겨 새로고침을 하는 경우, 데이터를 새로고침 하는 메소드
     */
    SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            listViewAdapter.clear();
            listViewAdapter.addItem("제목", "작성자", "작성날짜");
            listViewAdapter.dataChange();
            backgroundInitDepartmentInfoThread = new InitDepartmentInfoAsyncThread();
            backgroundInitDepartmentInfoThread.execute();
        }
    };

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
                    Message msg = handler.obtainMessage();
                    Bundle b = new Bundle();
                    ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                    String[][] departmentInfo = (String[][]) ois.readObject();

                    msg.obj = departmentInfo;

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
            mSwipeRefreshLayout.setRefreshing(false); //  새로고침이 완료 되었음을 표시
        }

        // AsyncTask.cancel(true) 호출시 실행되어 thread를 취소 합니다.
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    // 뒤로가기 버튼을 2초내로 2번 누를 시 Application 종료
    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if ( 0 <= intervalTime && FINSH_INTERVAL_TIME >= intervalTime ) {
            super.onBackPressed();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }
}