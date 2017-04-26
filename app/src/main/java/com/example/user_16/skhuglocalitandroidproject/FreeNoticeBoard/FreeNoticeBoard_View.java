package com.example.user_16.skhuglocalitandroidproject.FreeNoticeBoard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.user_16.skhuglocalitandroidproject.DBManager;
import com.example.user_16.skhuglocalitandroidproject.R;

import java.util.HashMap;


// 자유게시판 -> 게시된 리스트 뷰를 눌렀을 때
public class FreeNoticeBoard_View extends AppCompatActivity {

    int n;
    TextView view_user, view_content;
    TextView backBtn;   //뒤로가기 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.freeboard_view);

        //private ListViewAdapter viewAdapter = null; //ListViewAdapter클래스

        view_user = (TextView)findViewById(R.id.view_user);
        view_content = (TextView)findViewById(R.id.view_content);

        backBtn = (TextView)findViewById(R.id.backBtn);
        backBtn.setOnClickListener(backBtnClickListener);

        //게시된 글을 눌렀을 때 게시글 띄우기

        // intent = getIntent();            //FreeNoticeBoard_Main에서 FreeNoticeBoard_Write로 인텐트를 할 떄 넘겨준
                                            //현재 리스트 뷰의 개수를 받아옴.
        //n = intent.getExtras().getInt("no");    //받아온 리스트 뷰의 key값인 "no"을 정수형 변수 n에 저장

        final DBManager dbManager = new DBManager(getApplicationContext(), "app_data.db", null, 1); //SQLiteOpenHelper를 사용한 DBManager호출
        final HashMap<String, HashMap<String, String>> viewInfo = dbManager.getBoard();    //dbManager.getBoard()메소드로 게시된 글의 내용을 얻어와 해시맵 boardinfo에 저장

        HashMap<String, String> viewText = viewInfo.get(0 + "");
        //String index = viewText.get("index");
        //Log.d("인덱스-----", index);
        String a = viewText.get("name");
        String b = viewText.get("context");

        view_user.setText(a);
        view_content.setText(b);
        //Log.d("가져온 게시판 글", viewText.toString());

        /*// 해당 글의 정보를 다이얼로그에 띄움
        try {
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error 발생", Toast.LENGTH_SHORT).show();
            Log.e("ERR", "requestingBtn.setOnClickListener ERR : " + e.getMessage());
        }*/

    }//onCreate()끝

    //뒤로가기 버튼 클릭시 인텐트 종료
    TextView.OnClickListener backBtnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            finish();
        }
    };

}
