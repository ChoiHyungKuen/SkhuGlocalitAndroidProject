package com.example.user_16.skhuglocalitandroidproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by user-16 on 2017-05-28.
 */

public class SearchNaverActivity extends AppCompatActivity implements View.OnClickListener {

    private final static int INIT_BLOG = 300;
    private final static int INIT_LOCAL = 301;
    private final static int BLOG_ERROR = 302;
    private final static int LOCAL_ERROR = 303;

    private final static int DISPLAY = 10;
    private final static int START = 1;

    private TextView searchText, titleText, descriptionText, telephoneText, addressText, roadText;
    private Button changeAddress;
    private ListView searchNaverListView;
    private ListViewAdapter listViewAdapter = null;
    private String title, branch, searchWord, localLink;
    private int count;
    private boolean lockListView, searchBlogFlag, searchWordFlag;

    private SearchBlogAsyncThread searchBlogAsyncThread;
    private SearchLocalAsyncThread searchLocalAsyncThread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_naver);

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        branch = intent.getStringExtra("branch");
        searchWord = title + " " + branch;
        searchWordFlag = true;
        searchBlogFlag = true;
        count = 0;
        searchText = (TextView) findViewById(R.id.naverText);
        titleText = (TextView) findViewById(R.id.localTitle);
        descriptionText = (TextView) findViewById(R.id.localDescription);
        telephoneText = (TextView) findViewById(R.id.localTelephone);
        addressText = (TextView) findViewById(R.id.localAddress);
        roadText = (TextView) findViewById(R.id.localRoadAddress);
        changeAddress = (Button) findViewById(R.id.changeAddress);
        titleText.setOnClickListener(this);
        telephoneText.setOnClickListener(this);
        changeAddress.setOnClickListener(this);

        searchNaverListView = (ListView) findViewById(R.id.naverList);
        listViewAdapter = new ListViewAdapter(SearchNaverActivity.this);
        searchNaverListView.setAdapter(listViewAdapter);
        searchNaverListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SearchNaverListData searchNaverListData = (SearchNaverListData) listViewAdapter.getItem(position);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(searchNaverListData.nLink));
                startActivity(intent);
            }
        });
        searchNaverListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            boolean flag;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && flag) {
                    searchBlog(title, ++count);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                flag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount) && (lockListView == false);
            }
        });

        searchLocal(searchWord);
        searchBlog(title, count);
        searchText.setText(searchWord);
    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case INIT_BLOG:
                    ArrayList<HashMap<String, String>> searchBlogList = (ArrayList<HashMap<String, String>>) msg.obj;
                    lockListView = true;
                    if (searchBlogList.size() < 10) {
                        searchBlogFlag = false;
                    }
                    for (int i = 0; i < searchBlogList.size(); i++) {
                        HashMap<String, String> itemInfo = searchBlogList.get(i);
                        String title = itemInfo.get("title");
                        String link = itemInfo.get("link");
                        String context = itemInfo.get("context");
                        String blogname = itemInfo.get("blogname");
                        String date = itemInfo.get("date");
//                        Log.d("뭐가 문제",title +"\n"+link +"\n"+context +"\n"+blogname +"\n"+date);
                        listViewAdapter.addItem(title, link, context, blogname, date);
                    }
                    listViewAdapter.dataChange();
                    lockListView = false;
                    break;
                case INIT_LOCAL:
                    HashMap<String, String> itemInfo = (HashMap<String, String>) msg.obj;
                    titleText.setText(itemInfo.get("title"));
                    descriptionText.setText(itemInfo.get("description"));
                    telephoneText.setText(itemInfo.get("telephone"));
                    addressText.setText(itemInfo.get("address"));
                    roadText.setText(itemInfo.get("roadAddress"));
                    localLink = itemInfo.get("link");
                    break;
                case BLOG_ERROR:
                    Toast.makeText(SearchNaverActivity.this, "검색결과가 없습니다", Toast.LENGTH_SHORT).show();
                    break;
                case LOCAL_ERROR:
                    if (searchWordFlag){
                        searchText.setText(title);
                        searchLocal(title);
                        searchWordFlag = false;
                    } else {
                        Toast.makeText(SearchNaverActivity.this, "검색결과가 없습니다", Toast.LENGTH_SHORT).show();
                        titleText.setText(R.string.search_error);
                        descriptionText.setText(R.string.search_error);
                        telephoneText.setText(R.string.search_error);
                        addressText.setText(R.string.search_error);
                        roadText.setText(R.string.search_error);
                        titleText.setClickable(false);
                        telephoneText.setClickable(false);
                        changeAddress.setClickable(false);
                    }
                    break;
            }
        }
    };

    void searchBlog(String title, int count) {
        if (searchBlogFlag) {
            searchBlogAsyncThread = new SearchBlogAsyncThread();
            searchBlogAsyncThread.execute(title, Integer.toString(count));
        } else {
            Toast.makeText(SearchNaverActivity.this, getString(R.string.search_error), Toast.LENGTH_SHORT).show();
        }

    }

    void searchLocal(String searchWord) {
        searchLocalAsyncThread = new SearchLocalAsyncThread();
        searchLocalAsyncThread.execute(searchWord);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.localTitle:
                if (localLink != null){
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(localLink)));
                }
                break;
            case R.id.localTelephone:
                if (telephoneText.getText().length() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(android.Manifest.permission.CALL_PHONE)) {
                            String[] permission = {"android.permission.CALL_PHONE"};
                            requestPermissions(permission, 1000);
                        } else {
                            final String tel = "tel:" + telephoneText.getText().toString().replace("-", "");
                            AlertDialog.Builder builder = new AlertDialog.Builder(SearchNaverActivity.this);
                            builder.setTitle("전화 걸기")
                                    .setMessage(telephoneText.getText() + "에 전화연결 하시겠습니까?")
                                    .setCancelable(false)
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(tel));
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                if (checkSelfPermission(android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_DENIED){
                                                    startActivity(intent);
                                                }
                                            }
                                        }
                                    })
                                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            AlertDialog dialog = builder.create();
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.show();
                        }
                    }
                }
                break;
            case R.id.changeAddress:
                if (changeAddress.getText().toString().contains("도로명")){
                    changeAddress.setText("번지주소\n보기");
                    addressText.setVisibility(View.GONE);
                    roadText.setVisibility(View.VISIBLE);
                } else if (changeAddress.getText().toString().contains("번지")){
                    changeAddress.setText("도로명주소\n보기");
                    addressText.setVisibility(View.VISIBLE);
                    roadText.setVisibility(View.GONE);
                }
                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) { // 요청한 권한을 사용자가 "허용" 했다면...
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                final String tel = "tel:" + telephoneText.getText().toString().replace("-", "");
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchNaverActivity.this);
                builder.setTitle("전화 걸기")
                        .setMessage(telephoneText.getText() + "에 전화연결 하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(tel));
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (checkSelfPermission(android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_DENIED){
                                        startActivity(intent);
                                    }
                                }
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            } else {
                Toast.makeText(SearchNaverActivity.this, "권한요청을 거부했습니다.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private class ViewHolder {
        public TextView nTitle;
        public TextView nContext;
        public TextView nBlogname;
        public TextView nDate;
    }

    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<SearchNaverListData> searchNaverListData = new ArrayList<>();

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        // 총 몇개의 리스트가 있는지 반환
        @Override
        public int getCount() {
            return searchNaverListData.size();
        }

        // 사용자가 선택한 아이템을 반환
        @Override
        public Object getItem(int position) {
            return searchNaverListData.get(position);
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
                convertView = inflater.inflate(R.layout.search_naver_item, null);

                holder.nTitle = (TextView) convertView.findViewById(R.id.naver_list_title);
                holder.nContext = (TextView) convertView.findViewById(R.id.naver_list_context);
                holder.nBlogname = (TextView) convertView.findViewById(R.id.naver_list_name);
                holder.nDate = (TextView) convertView.findViewById(R.id.naver_list_date);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            SearchNaverListData searchData = searchNaverListData.get(position); // DListData로부터 해당 아이템의 데이터를 받아온다.

            holder.nTitle.setText(searchData.nTitle);
            holder.nContext.setText(searchData.nContext);
            holder.nBlogname.setText(searchData.nBlogname);
            holder.nDate.setText(searchData.nDate);
            return convertView;
        }

        /*
            리스트에 아이템을 추가하는 메소드
        */
        public void addItem(String title, String link, String context, String blogName, String date) {
            SearchNaverListData addInfo = new SearchNaverListData();
            addInfo.nTitle = title;
            addInfo.nLink = link;
            addInfo.nContext = context;
            addInfo.nBlogname = blogName;
            addInfo.nDate = date;
            searchNaverListData.add(addInfo);
        }

        // 리스트를 새로고침 하는 메소드
        public void clear() {
            searchNaverListData.clear();
        }

        // 데이터가 바뀌었음을 DB에 알려주는 메소드
        public void dataChange() {
            listViewAdapter.notifyDataSetChanged();
        }
    }
    public class SearchLocalAsyncThread extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            String clientId = getString(R.string.CLIENT_ID);
            String clientSecret = getString(R.string.CLIENTSECRET);
            XmlPullParserFactory factory;
            XmlPullParser parser;
            try {
                String text = URLEncoder.encode(args[0],"UTF-8");
                String apiURL = "https://openapi.naver.com/v1/search/local.xml?query=" + text + "&display=1"; // xml 결과
                Log.d("URL",apiURL);
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                factory = XmlPullParserFactory.newInstance();
                parser = factory.newPullParser();
                parser.setInput(con.getInputStream(),null);
                parser.next();
                int eventType= parser.getEventType();
                String tag;
                HashMap<String, String> itemInfo = null;
                while( eventType != XmlPullParser.END_DOCUMENT ){
                    switch(eventType) {
                        case XmlPullParser.END_DOCUMENT://문서의 끝
                            break;
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG: {
                            tag = parser.getName();
                            Log.d("tag",tag);
                            switch (tag) {
                                case "item":
                                    itemInfo = new HashMap<>();
                                    break;
                                case "title":
                                    parser.next();
                                    if (parser.getText().contains("Naver Open API")){
                                        Log.d("타이틀","검색타이틀");
                                    }else {
                                        itemInfo.put("title",parser.getText().replace("<b>","").replace("</b>","").replace("&amp;","&").replace("&gt;",">").replace("&lt;","<").replace("&quot;","\""));
                                        Log.d("아이템타이틀",itemInfo.get("title"));
                                    }
                                    break;
                                case "link":
                                    parser.next();
                                    if (parser.getText() != null){
                                        if (parser.getText().contains("http://search.naver.com")){
                                            Log.d("링크","검색링크");
                                        }else {
                                            itemInfo.put("link",parser.getText());
                                            Log.d("아이템링크",itemInfo.get("link"));
                                        }
                                    }
                                    break;
                                case "description":
                                    parser.next();
                                    if (parser.getText() != null){
                                        if (parser.getText().contains("Naver Search Result")){
                                            Log.d("내용","검색내용");
                                        }else {
                                            itemInfo.put("description",parser.getText().replace("<b>","").replace("</b>","").replace("&amp;","&").replace("&gt;",">").replace("&lt;","<").replace("&quot;","\""));
                                            Log.d("아이템내용",itemInfo.get("description"));
                                        }
                                    }
                                    break;
                                case "telephone":
                                    parser.next();
                                    if (parser.getText() != null){
                                        itemInfo.put("telephone",parser.getText());
                                        Log.d("아이템내용",itemInfo.get("telephone"));
                                    }
                                    break;
                                case "address":
                                    parser.next();
                                    if (parser.getText() != null){
                                        itemInfo.put("address",parser.getText());
                                        Log.d("아이템내용",itemInfo.get("address"));
                                    }
                                    break;
                                case "roadAddress":
                                    parser.next();
                                    if (parser.getText() != null){
                                        itemInfo.put("roadAddress",parser.getText());
                                        Log.d("아이템내용",itemInfo.get("roadAddress"));
                                    }
                                    break;
                            }
                            break;
                        }
                    }
                    eventType = parser.next();
                }
                Message msg = handler.obtainMessage();
                if (itemInfo.get("title").length()==0){
                    handler.sendEmptyMessage(LOCAL_ERROR);
                }else {
                    msg.what = INIT_LOCAL;
                    msg.obj = itemInfo;
                    handler.sendMessage(msg);
                }
            } catch (Exception e) {
                handler.sendEmptyMessage(LOCAL_ERROR);
                System.out.println(e);
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

    public class SearchBlogAsyncThread extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            String clientId = getString(R.string.CLIENT_ID);
            String clientSecret = getString(R.string.CLIENTSECRET);

            XmlPullParserFactory factory;
            XmlPullParser parser;
            try {
                String text = URLEncoder.encode(args[0],"UTF-8");
                int count = Integer.parseInt(args[1]);
                String apiURL = "https://openapi.naver.com/v1/search/blog.xml?query=" + text + "&display=" + DISPLAY + "&start=" + (START+10*count); // xml 결과
                Log.d("URL",apiURL);
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                factory = XmlPullParserFactory.newInstance();
                parser = factory.newPullParser();
                parser.setInput(con.getInputStream(),null);
                parser.next();
                int eventType= parser.getEventType();
                String tag;
                ArrayList<HashMap<String, String>> searchList = new ArrayList<>();
                HashMap<String, String> itemInfo = null;
                while( eventType != XmlPullParser.END_DOCUMENT ){
                    switch(eventType) {
                        case XmlPullParser.END_DOCUMENT://문서의 끝
                            break;
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG: {
                            tag = parser.getName();
                            Log.d("tag",tag);
                            switch (tag) {
                                case "item":
                                    itemInfo = new HashMap<>();
                                    break;
                                case "title":
                                    parser.next();
                                    if (parser.getText().contains("Naver Open API")){
                                        Log.d("타이틀","검색타이틀");
                                        break;
                                    }else {
                                        itemInfo.put("title",parser.getText().replace("<b>","").replace("</b>","").replace("&amp;","&").replace("&gt;",">").replace("&lt;","<").replace("&quot;","\""));
//                                        Log.d("아이템타이틀",searchNaverListData.nTitle);
                                        break;
                                    }
                                case "link":
                                    parser.next();
                                    if (parser.getText().contains("http://search.naver.com")){
                                        Log.d("링크","검색링크");
                                        break;
                                    }else {
                                        itemInfo.put("link",parser.getText());
//                                        Log.d("아이템링크",searchNaverListData.nLink);
                                        break;
                                    }
                                case "description":
                                    parser.next();
                                    if (parser.getText().contains("Naver Search Result")){
                                        Log.d("내용","검색내용");
                                        break;
                                    }else {
                                        itemInfo.put("context",parser.getText().replace("<b>","").replace("</b>","").replace("&amp;","&").replace("&gt;",">").replace("&lt;","<").replace("&quot;","\""));
//                                        Log.d("아이템내용",searchNaverListData.nContext);
                                        break;
                                    }
                                case "bloggername":
                                    parser.next();
                                    itemInfo.put("blogname",parser.getText());
//                                    Log.d("아이템블로그이름",searchNaverListData.nBlogname);
                                    break;
                                case "postdate":
                                    parser.next();
                                    itemInfo.put("date",parser.getText());
//                                    Log.d("아이템게시일",searchNaverListData.nDate);
                                    searchList.add(itemInfo);
                                    break;
                            }
                            break;
                        }
                    }
                    eventType = parser.next();
                }
                Message msg = handler.obtainMessage();
                if (searchList.isEmpty()){
                    handler.sendEmptyMessage(BLOG_ERROR);
                }else {
                    msg.what = INIT_BLOG;
                    msg.obj = searchList;
                    handler.sendMessage(msg);
                }

            } catch (Exception e) {
                handler.sendEmptyMessage(BLOG_ERROR);
                System.out.println(e);
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
