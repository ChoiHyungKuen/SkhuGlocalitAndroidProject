package com.example.user_16.skhuglocalitandroidproject.FreeNoticeBoard;

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

import com.example.user_16.skhuglocalitandroidproject.DBManager;
import com.example.user_16.skhuglocalitandroidproject.R;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class FreeNoticeBoard_Main extends AppCompatActivity {

    private final long	FINSH_INTERVAL_TIME = 2000; //2초안에 Back 버튼을 2번 누르면 앱 종료 -> 2초
    private long backPressedTime = 0;

    private ListView freeboard_ListView = null; //자유게시판의 리스트 뷰
    private ListViewAdapter listAdapter = null; //ListViewAdapter클래스

    private SwipeRefreshLayout mSwipeRefreshLayout = null;  //새로고침

    private InitAsyncThread backgroundInitThread;           //초기화 스레드
    private RemoveAsyncThread backgroundRemoveThread;       //삭제 스레드

    TextView freeboard_writeBtn;//글쓰기 버튼
    TextView backBtn;           //뒤로가기 버튼
    static boolean a = false;

    /*스레드
   후배가 요청 게시판에 글을 올린 경우, DB와 상호작용 하는 핸들러
*/
    final Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            Bundle b = msg.getData();

            if (b.getString("present_condition") != null) {
                if (b.getString("result").equals("success")) {
                    Toast.makeText(getApplicationContext(), "BOOK:DREAM을 완료했습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "이미 처리 하신 상태입니다.", Toast.LENGTH_SHORT).show();
                }
            }
            else if(b.getString("status") !=null ) {
                if (b.getString("status").equals("write_faile")) {
                    Toast.makeText(getApplicationContext(), "파일을 추가하지 못 했습니다. 조금 이따 다시 시도해주세요!",Toast.LENGTH_SHORT).show();
                }
                else{ //수정해

                }
            }
        }
    }; //핸들러 끝

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.freeboard_main);

        freeboard_writeBtn = (TextView) findViewById(R.id.freeboard_writeBtn);  //글쓰기 버튼
        freeboard_writeBtn.setOnClickListener(writtingClickListener);           //글쓰기 버튼 리스너
        backBtn = (TextView)findViewById(R.id.backBtn);
        backBtn.setOnClickListener(backBtnClickListener);

        freeboard_ListView = (ListView)findViewById(R.id.freeboard_listView);   //게시판 리스트 뷰

        //리스트 뷰 클릭시 게시된 글 액티비티로 인텐트
        freeboard_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //해당 액티비티로 인텐트
                Intent intent = new Intent(getApplicationContext(), FreeNoticeBoard_View.class);
                startActivity(intent);
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);  //새로고침
        mSwipeRefreshLayout.setOnRefreshListener(refreshListener);

        //리스트 뷰에 아이템 추가 (리스트 뷰에 작성한 게시글 추가)
        listAdapter = new ListViewAdapter(getApplicationContext());
            freeboard_ListView.setAdapter(listAdapter);
            final DBManager dbManager = new DBManager(getApplicationContext(), "app_data.db", null, 1); //SQLiteOpenHelper를 사용한 DBManager호출
            final HashMap<String, HashMap<String, String>> boardInfo = dbManager.getBoard();    //dbManager.getBoard()메소드로 게시된 글의 내용을 얻어와 해시맵 boardinfo에 저장

            for (int i = 0; i < boardInfo.size(); i++) { //인덱스0부터 boardInfo의 길이까지 루프 돌면서 리스트뷰에 addItem.
                HashMap<String, String> boardText = boardInfo.get(i + ""); //boardInfo의 i번째 해시맵을 boardtext에 저장 후 boardtext를 addItem
                listAdapter.addItem(boardText.get("name"), boardText.get("context"), boardText.get("date"));   //리스트뷰에 아이템 추가
                    Log.d("가져온 게시판 글", boardText.toString());
            }
        listAdapter.dataChange();

        /*
        --차후에 수정 / 오류발생해서 일단 주석처리
        // 사용자가 글을 오래 클릭한 경우 : 1. 글쓴이와 동일 인물인지를 확인 2. 동일 인물일 시 요청 리스트와 DB에서 해당 글을 삭제
        freeboard_ListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final DBManager dbManager = new DBManager(getApplicationContext(), "app_data.db", null, 1);
                final HashMap<String, String> dataMap = dbManager.getMemberInfo();
                String userName = dataMap.get("name");
                FreeNoticeBoard_ListData fData = (FreeNoticeBoard_ListData) listAdapter.getItem(position);
                if (!fData.fUser.equals(userName)) {    // 해당 글 작성자가 아닌 경우 실패 메세지를 띄우고 삭제 취소
                    Log.d("user", fData.fUser);
                    Log.d("userInfo", userName);
                    Toast.makeText(getApplicationContext(), "글 작성자가 아닙니다. 확인 후 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                    return true;
                }
                AlertDialog.Builder alert = new AlertDialog.Builder(FreeNoticeBoard_Main.this);
                alert.setTitle("확인창")
                        .setMessage("정말로 삭제하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("d","번호 : " + position);
                                listAdapter.clear();

                                backgroundRemoveThread = new RemoveAsyncThread();
                                backgroundRemoveThread.execute(position+"");   // DB에서 삭제
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                return true;
            }
        }); */

    }//onCreate()끝


    /*
        어플리케이션 종료시 쓰레드의 종료를 요청하는 메소드
    */
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            //if (backgroundInitThread.getStatus() == AsyncTask.Status.RUNNING) {//-------------------------------------주석처리(차후에 수정)
                //backgroundInitThread.cancel(true);
            //}
            //if (backgroundRemoveThread.getStatus() == AsyncTask.Status.RUNNING) {
                //backgroundRemoveThread.cancel(true);
            //}
        } catch (Exception e) {}
    }


    //글쓰기 버튼 클릭 리스너 - 글쓰기 창으로 인텐트
    TextView.OnClickListener writtingClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), FreeNoticeBoard_Write.class);
            intent.putExtra("no",listAdapter.getCount());//현재리스트 숫자로 만드는 uniqueNum
            Log.d("리스트뷰 몇 개? >>>>",listAdapter.getCount()+"");
            startActivity(intent);
            finish();
        }
    };

    //자유게시판에서 뒤로가기 버튼 클릭시 인텐트 종료
    TextView.OnClickListener backBtnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            finish();
        }
    };


    /*
       ViewHolder란, 이름 그대로 뷰들을 홀더에 꼽아놓듯이 보관하는 객체를 말한다.
       각각의 Row를 그려낼 때 그 안의 위젯들의 속성을 변경하기 위해 findViewById를 호출하는데,
       이것의 비용이 큰것을 줄이기 위해 사용한다. 여기서 게시판의 정보들을 ViewHolder를 이용해 삽입한다.
    */
    private class ViewHolder {
        public TextView fContent;
        public TextView fUser;
        public TextView fDate;
    }
    // <리스트 뷰> 요청 프래그먼트의 리스트뷰를 관리하는 메소드
    private class ListViewAdapter extends BaseAdapter {

        private Context mContext = null;
        private ArrayList<FreeNoticeBoard_ListData> free_ListData = new ArrayList<>();

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return free_ListData.size();
        }

        @Override
        public Object getItem(int position) {
            return free_ListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        // 사용자가 선택한 아이템 데이터를 freeboard_item 형태에 맞춰 반환한다.
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.freeboard_item, null);

                holder.fUser = (TextView) convertView.findViewById(R.id.list_user);
                holder.fContent = (TextView) convertView.findViewById(R.id.list_content);
                holder.fDate = (TextView) convertView.findViewById(R.id.list_date);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            FreeNoticeBoard_ListData fData = free_ListData.get(position); // FreeNoticeBoard_ListData로부터 해당 아이템의 데이터를 받아온다.

            holder.fUser.setText(fData.fUser);
            holder.fContent.setText(fData.fContent);
            holder.fDate.setText(fData.fDate);
            Log.d("-----------리스트 뷰 유저 : ",fData.fUser);
            Log.d("-----------리스트 뷰 내용 : ",fData.fContent);
            Log.d("-----------리스트 뷰 내용 : ",fData.fDate);
            return convertView;
        }//getView끝

        //리스트에 아이템을 추가하는 메소드
        public void addItem(String fUser, String fContent, String fDate) { //일단 user랑 content만 리스트에 추가
            FreeNoticeBoard_ListData addInfo = null;
            addInfo = new FreeNoticeBoard_ListData();

            addInfo.fUser = fUser;
            addInfo.fContent = fContent;
            addInfo.fDate = fDate;
            Log.d("리스트 뷰 유저",fUser);
            Log.d("리스트 뷰 내용",fContent);
            Log.d("리스트 뷰 현재시간",fDate);
            free_ListData.add(addInfo);
            Log.d("에드아이템","-----------------------------------------");
        }//addItem끝


        // 리스트를 새로고침 하는 메소드
        public void clear(){
            free_ListData.clear();
        }

        // 데이터가 바뀌었음을 DB에 알려주는 메소드
        public void dataChange() {
            listAdapter.notifyDataSetChanged();
        }
    }//ListViewAdapter끝----------------------------------------------------------------------------







    /*-----------------------------------------------------------------------------------------
        사용자가 리스트를 아래로 끌어당겨 새로고침을 하는 경우, 데이터를 새로고침 하는 메소드
    */
    SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            //listAdapter.clear();
            //backgroundInitThread = new InitAsyncThread(); // DB로부터 데이터를 새로 받아온다.//-------------------------------------주석처리(차후에 수정)
            //backgroundInitThread.execute();
        }
    };

    // [게시판 초기화]------------------------------------------------------------------------------
    public class InitAsyncThread extends AsyncTask<Void, String, String> {
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

            //urlStr = "http://"+getString(R.string.ip_address)+":8080/SkhuGlocalitWebProject/bookdream/initRequestInfo"; //원래거
            //urlStr = "http://172.30.21.64:8080/BookDreamServerProject/test_addFreeNoticeBoardInitInfo"; //테스트용
            try {
                url = new URL(urlStr);
                Log.d("test", urlStr); //여기까지 찍힘 연결안됨
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

                    for (int i = 0; i < dataMap.size(); i++) {
                        HashMap<String, String> stringDataMap = dataMap.get(i + "");
                        Message msg = handler.obtainMessage();
                        Bundle b = new Bundle();
                        b.putString("status", "init");
                        b.putString("no", stringDataMap.get("no"));
                        b.putString("user", stringDataMap.get("user"));
                        b.putString("content", stringDataMap.get("content"));
                        msg.setData(b);
                        handler.sendMessage(msg);
                    }
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
            mSwipeRefreshLayout.setRefreshing(false); //  새로고침이 완료 되었음을 표시
        }

        // AsyncTask.cancel(true) 호출시 실행되어 thread를 취소 합니다.
        protected void onCancelled() {
            super.onCancelled();
        }
    }

// [글 삭제]----------------------------------------------------------------------------------------
public class RemoveAsyncThread extends AsyncTask<String, String, String> {
    // Thread를 시작하기 전에 호출되는 함수
    protected void onPreExecute() {
        super.onPreExecute();
    }

    // Thread의 주요 작업을 처리 하는 함수
    // Thread를 실행하기 위해 excute(~)에서 전달한 값을 인자로 받습니다.
    protected String doInBackground(String...args) {
        try {
            URL url = null;
            HttpURLConnection conn = null;
            String urlStr = "";

            //urlStr = "http://"+getString(R.string.ip_address)+":8080/SkhuGlocalitWebProject/bookdream/removeRequestInfo"; //원래거
            urlStr = "http://172.30.21.64:8080/BookDreamServerProject/test_removeFreeNoticeBoardInfo"; //테스트용
            url = new URL(urlStr);
            Log.d("test", urlStr);

            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            HashMap<String, String> params = new HashMap<>();
            params.put("no", args[0]);
            ObjectOutputStream oos = new ObjectOutputStream(conn.getOutputStream());
            oos.writeObject(params);
            oos.flush();
            oos.close();
            Log.d("test", "remove_write");
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { // 서버가 받았다면
                Log.d("글 삭제 - coded", "들어옴");
                ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                HashMap<String, HashMap<String, String>> dataMap = (HashMap<String, HashMap<String, String>>)ois.readObject();
                ois.close();

                for(int i=0; i<dataMap.size(); i++) {
                    HashMap<String, String> stringDataMap = dataMap.get(i+"");
                    Message msg = handler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("status","init");
                    b.putString("no", stringDataMap.get("no"));
                    b.putString("user", stringDataMap.get("user"));
                    b.putString("content", stringDataMap.get("content"));
                    msg.setData(b);
                    handler.sendMessage(msg);
                }
            }
            conn.disconnect();
            return "OK";
        }  catch (Exception e) {
            //Toast.makeText(getActivity(), "Error 발생", Toast.LENGTH_SHORT).show();
            Log.e("ERR", "RemoveAsyncThread ERR : " + e.getMessage());
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

    // AsyncTask.cancel(true) 호출시 실행되어 thread를 취소한다.
    protected void onCancelled() {
        super.onCancelled();
    }
}


//--------------------------------------------------------------------------------------------------
    /*
        뒤로가기 버튼을 2초내로 2번 누를 시 Application 종료
    */
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
