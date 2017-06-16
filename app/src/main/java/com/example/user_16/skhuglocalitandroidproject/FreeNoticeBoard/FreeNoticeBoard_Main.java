package com.example.user_16.skhuglocalitandroidproject.FreeNoticeBoard;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user_16.skhuglocalitandroidproject.DBManager;
import com.example.user_16.skhuglocalitandroidproject.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static com.example.user_16.skhuglocalitandroidproject.R.id.swipe_layout;

public class FreeNoticeBoard_Main extends AppCompatActivity {

    private final long	FINSH_INTERVAL_TIME = 2000;         //2초안에 Back 버튼을 2번 누르면 앱 종료 -> 2초
    private long backPressedTime = 0;

    private ListView freeboard_ListView = null;             //자유게시판의 리스트 뷰
    static private ListViewAdapter listAdapter = null;      //ListViewAdapter클래스

    private SwipeRefreshLayout mSwipeRefreshLayout = null;  //새로고침

    private InitAsyncThread backgroundInitThread;           //초기화 스레드
    private RemoveAsyncThread backgroundRemoveThread;       //삭제 스레드

    TextView freeboard_writeBtn;                            //글쓰기 버튼
    TextView backBtn;                                       //뒤로가기 버튼
    ImageView freeboard_list_image;                         //리스트뷰의 이미지

    static boolean a = false;

    // View창으로 인텐트 할 때
    Drawable intentImage;
    String intentTitle, intentUser, intentContent, intentDate;

    /*----------------------------------------------------------------------------------------------
        자유 게시판에 글을 올린 경우, DB와 상호작용 하는 핸들러
    ----------------------------------------------------------------------------------------------*/
    final Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            Bundle b = msg.getData();

            if(b.getString("status") !=null ) {
                if (b.getString("status").equals("write_faile")) {
                    Toast.makeText(getApplicationContext(), "파일을 추가하지 못 했습니다. 조금 이따 다시 시도해주세요!",Toast.LENGTH_SHORT).show();
                }
                else{
                    byte[] bytes = (byte[])msg.obj;
                    ByteArrayInputStream is = new ByteArrayInputStream(bytes);
                    Drawable icon = Drawable.createFromStream(is, "articleImage");
                    listAdapter.addItem(Integer.parseInt(b.getString("no")), icon, b.getString("title"), b.getString("user"),
                            b.getString("content"), b.getString("date"));
                    Log.d("핸들러", "자유게시판 아이템 추가했니?");
                    //listAdapter.sort();
                    listAdapter.dataChange();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.freeboard_main);

        freeboard_writeBtn = (TextView) findViewById(R.id.freeboard_writeBtn);  //글쓰기 버튼
        freeboard_writeBtn.setOnClickListener(writtingClickListener);           //글쓰기 버튼 리스너
        backBtn = (TextView)findViewById(R.id.backBtn);                         //뒤로가기 버튼
        backBtn.setOnClickListener(backBtnClickListener);

        freeboard_list_image= (ImageView)findViewById(R.id.freeboard_list_image);  //리스트 뷰의 이미지

        //리스트 뷰 새로고침
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(swipe_layout);  //새로고침
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listAdapter.clear(); //리스트뷰 갱신
                Log.d("정보게시판 리스트 뷰 새로고침","ok");
                backgroundInitThread = new InitAsyncThread(); // DB로부터 데이터를 새로 받아온다.
                backgroundInitThread.execute();
            }
        });

        // 자유게시판의 리스트 뷰
        freeboard_ListView = (ListView)findViewById(R.id.freeboard_listView);

        // 리스트 뷰 클릭시 게시된 글 view 인텐트로 넘기기
        freeboard_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final FreeNoticeBoard_ListData freeData = (FreeNoticeBoard_ListData) listAdapter.getItem(position);

                Intent intent = new Intent(getApplicationContext(), FreeNoticeBoard_View.class);

                int p = freeData.fNo;           // Main에서 View로 인텐트할 때 position값을 넘겨주기 위해
                intent.putExtra("position",p);  // 현재리스트 숫자로 만드는 uniqueNum
                Log.d("현재 위치 인덱스 값---->>", p+"");

                intentImage = freeData.fIcon; //drawable형 선택한 해당 위치 이미지

                // 인텐트할 때 이미지 데이터형을 drawable로 못넘겨주기때문에 byte[]으로 바꿔서 보내준다.
                Bitmap bitmap = ((BitmapDrawable)intentImage).getBitmap();  //drawable->bitmap
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] myBitmapData = stream.toByteArray();
                intent.putExtra("intentImage", myBitmapData);           Log.d("현재 위치 이미지 값---->>", myBitmapData+"");

                intentTitle = freeData.fTitle;
                intent.putExtra("intentTitle",intentTitle);             Log.d("현재 위치 제목 값---->>", intentTitle);
                intentUser = freeData.fUser;
                intent.putExtra("intentUser",intentUser);               Log.d("현재 위치 유저 값---->>", intentUser);
                intentContent = freeData.fContent;
                intent.putExtra("intentContent",intentContent);         Log.d("현재 위치 내용 값---->>", intentContent);
                intentDate = freeData.fDate;
                intent.putExtra("intentDate",intentDate);               Log.d("현재 위치 날짜 값---->>", intentDate);
                startActivity(intent);
            }
        });

        //리스트 뷰 갱신
        listAdapter = new ListViewAdapter(getApplicationContext());
        freeboard_ListView.setAdapter(listAdapter);
        backgroundInitThread = new InitAsyncThread(); // DB로부터 데이터를 새로 받아온다.
        backgroundInitThread.execute();


        // 리스트뷰의 글을 오래 클릭한 경우 글 삭제하기
        freeboard_ListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final DBManager dbManager = new DBManager(getApplicationContext(), "app_data.db", null, 1);
                final HashMap<String, String> dataMap = dbManager.getMemberInfo();
                String userName = dataMap.get("name");

                final FreeNoticeBoard_ListData fData = (FreeNoticeBoard_ListData) listAdapter.getItem(position);
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
                                Log.d("d","해당 글 번호 : " + fData.fNo);
                                listAdapter.clear();

                                backgroundRemoveThread = new RemoveAsyncThread();
                                backgroundRemoveThread.execute(fData.fNo+"");        // DB에서 삭제

                                backgroundInitThread = new InitAsyncThread();       // 삭제 후 새로고침
                                backgroundInitThread.execute();

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
        });

    }


    /*----------------------------------------------------------------------------------------------
        어플리케이션 종료시 쓰레드의 종료를 요청하는 메소드
    ----------------------------------------------------------------------------------------------*/
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (backgroundInitThread.getStatus() == AsyncTask.Status.RUNNING) {
                backgroundInitThread.cancel(true);
            }
            if (backgroundRemoveThread.getStatus() == AsyncTask.Status.RUNNING) {
                backgroundRemoveThread.cancel(true);
            }

        } catch (Exception e) {}
    }

    //글쓰기 버튼 클릭 리스너 - 글쓰기 창으로 인텐트
    TextView.OnClickListener writtingClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), FreeNoticeBoard_Write.class);
            intent.putExtra("no",listAdapter.getCount());//현재리스트 숫자로 만드는 uniqueNum -- 원래거
            Log.d("자유게시판 리스트뷰 몇 개? >>>>",listAdapter.getCount()+"");
            startActivity(intent);
            FreeNoticeBoard_Main.this.finish();//
        }
    };

    //자유게시판에서 뒤로가기 버튼 클릭시 인텐트 종료
    TextView.OnClickListener backBtnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            FreeNoticeBoard_Main.this.finish();
        }
    };


    private class ViewHolder {//-------------------------------------------------------------------
        public ImageView fIcon;
        public TextView fTitle;
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

                holder.fIcon = (ImageView) convertView.findViewById(R.id.freeboard_list_image);
                holder.fTitle = (TextView) convertView.findViewById(R.id.list_title);
                holder.fUser = (TextView) convertView.findViewById(R.id.list_user);
                holder.fContent = (TextView) convertView.findViewById(R.id.list_content);
                holder.fDate = (TextView) convertView.findViewById(R.id.list_date);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // FreeNoticeBoard_ListData로부터 해당 아이템의 데이터를 받아온다.
            FreeNoticeBoard_ListData fData = free_ListData.get(position);
            Log.d("position값 : ", position+"");

            if (fData.fIcon != null) {
                holder.fIcon.setVisibility(View.VISIBLE);
                holder.fIcon.setImageDrawable(fData.fIcon);
            }else{
                holder.fIcon.setVisibility(View.GONE);
            }

            holder.fTitle.setText(fData.fTitle);
            holder.fUser.setText(fData.fUser);
            holder.fContent.setText(fData.fContent);
            holder.fDate.setText(fData.fDate);
            Log.d("-----자유게시판 리스트 뷰 제목 : ",fData.fTitle);
            Log.d("-----자유게시판 리스트 뷰 유저 : ",fData.fUser);
            Log.d("-----자유게시판 리스트 뷰 내용 : ",fData.fContent);
            Log.d("-----자유게시판 리스트 뷰 내용 : ",fData.fDate);
            return convertView;
        }

        //리스트에 아이템을 추가하는 메소드
        public void addItem(int fNo, Drawable icon, String fTitle, String fUser, String fContent, String fDate) {
            FreeNoticeBoard_ListData addInfo = null;
            addInfo = new FreeNoticeBoard_ListData();

            //디비에서 받아온 데이터 저장
            addInfo.fNo = fNo;
            addInfo.fIcon = icon;                //이미지 추가
            addInfo.fTitle = fTitle;
            addInfo.fUser = fUser;
            addInfo.fContent = fContent;
            addInfo.fDate = fDate;
            Log.d("자유게시판 리스트 뷰 글번호",fNo+"");
            Log.d("자유게시판 리스트 뷰 제목",fTitle);
            Log.d("자유게시판 리스트 뷰 유저",fUser);
            Log.d("자유게시판 리스트 뷰 내용",fContent);
            Log.d("자유게시판 리스트 뷰 현재시간",fDate);
            free_ListData.add(addInfo);
            Log.d("자유게시판 에드아이템","-----------------------------------------");
        }

       /* // 리스트 뷰에 글이 역순으로 추가되도록 하는 메소드
        public void sort() {
            Collections.sort(free_ListData, FreeNoticeBoard_ListData.ALPHA_COMPARATOR);
            Collections.reverse(free_ListData);
        }*/

        // 리스트를 새로고침 하는 메소드
        public void clear() {
            free_ListData.clear();
        }

        // 데이터가 바뀌었음을 DB에 알려주는 메소드
        public void dataChange() {
            listAdapter.notifyDataSetChanged();
        }
    }

    /*----------------------------------------------------------------------------------------------
        리스트 뷰 새로고침 스레드
    ----------------------------------------------------------------------------------------------*/
    private class InitAsyncThread extends AsyncTask<Void, String, String> {
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
            //urlStr = "http://192.168.25.55:8080/BookDreamServerProject/FreeNoticeBoard_Init"; //테스트용

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
                    HashMap<String, byte[]> imgByteMap = (HashMap<String, byte[]>)ois.readObject();
                    ois.close();

                    for(int i=dataMap.size()-1; i>=0; i--) {
                        HashMap<String, String> map = dataMap.get(i+"");
                        Message msg = handler.obtainMessage();
                        Bundle b = new Bundle();
                        b.putString("status", "init");//체크
                        b.putString("no", map.get("no"));
                        b.putString("title", map.get("title"));
                        b.putString("user", map.get("user"));
                        b.putString("content", map.get("content"));
                        b.putString("date", map.get("date"));
                        msg.obj = imgByteMap.get(map.get("img_des"));   //이미지
                        msg.setData(b);
                        handler.sendMessage(msg);
                    }

                    /*for(int i=0; i<dataMap.size(); i++) {
                        HashMap<String, String> map = dataMap.get(i+"");
                        Message msg = handler.obtainMessage();
                        Bundle b = new Bundle();
                        b.putString("status", "init");//체크
                        b.putString("no", map.get("no"));
                        b.putString("title", map.get("title"));
                        b.putString("user", map.get("user"));
                        b.putString("content", map.get("content"));
                        b.putString("date", map.get("date"));
                        msg.obj = imgByteMap.get(map.get("img_des"));   //이미지
                        msg.setData(b);
                        handler.sendMessage(msg);
                    }*/
                }
                conn.disconnect();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error 발생", Toast.LENGTH_SHORT).show();
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
            mSwipeRefreshLayout.setRefreshing(false); //  새로고침이 완료 되었음을 표시
        }

        // AsyncTask.cancel(true) 호출시 실행되어 thread를 취소 합니다.
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    /*----------------------------------------------------------------------------------------------
        글 삭제 스레드
    ----------------------------------------------------------------------------------------------*/
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

                urlStr = "http://"+getString(R.string.ip_address)+":8080/SkhuGlocalitWebProject/FreeNoticeBoard_Remove";
                //urlStr = "http://192.168.25.55:8080/BookDreamServerProject/FreeNoticeBoard_Remove"; //테스트용

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
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) { // 서버가 받았다면
                    Log.d("coded", "자유게시판 데이터 들어옴");
                    ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                    HashMap<String, HashMap<String,String>> dataMap = (HashMap<String, HashMap<String,String>>)ois.readObject();
                    HashMap<String, byte[]> imgByteMap = (HashMap<String, byte[]>)ois.readObject();
                    ois.close();

                    for(int i=dataMap.size()-1; i>=0; i--) {
                        HashMap<String, String> map = dataMap.get(i+"");
                        Message msg = handler.obtainMessage();
                        Bundle b = new Bundle();

                        b.putString("no", map.get("no"));
                        b.putString("title", map.get("title"));
                        b.putString("user", map.get("user"));
                        b.putString("content", map.get("content"));
                        b.putString("date", map.get("date"));
                        msg.obj = imgByteMap.get(map.get("img_des"));
                        msg.setData(b);
                        handler.sendMessage(msg);
                    }
                    /*for(int i=0; i<dataMap.size(); i++) {
                        HashMap<String, String> map = dataMap.get(i+"");
                        Message msg = handler.obtainMessage();
                        Bundle b = new Bundle();

                        b.putString("no", map.get("no"));
                        b.putString("title", map.get("title"));
                        b.putString("user", map.get("user"));
                        b.putString("content", map.get("content"));
                        b.putString("date", map.get("date"));
                        msg.obj = imgByteMap.get(map.get("img_des"));
                        msg.setData(b);
                        handler.sendMessage(msg);
                    }*/
                }
                conn.disconnect();

            } catch (Exception e) {
                Log.e("ERR", "RemoveAsyncThread ERR : " + e.getMessage());
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
