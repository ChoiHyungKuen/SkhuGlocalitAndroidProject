package com.example.user_16.skhuglocalitandroidproject.FreeNoticeBoard;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user_16.skhuglocalitandroidproject.DBManager;
import com.example.user_16.skhuglocalitandroidproject.R;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;


public class FreeNoticeBoard_Write extends AppCompatActivity {

    int indexNum;

    TextView gallaryBtn, photoBtn;                      //갤러리,사진찍기
        private static final int PICK_FROM_CAMERA = 0;
        private static final int PICK_FROM_ALBUM = 1;
        private static final int CROP_FROM_iMAGE = 2;
        private Uri mImageCaptureUri;
        private int xValue, yValue;
        private Drawable uploadingImage = null;
        private ImageView image_input;

    TextView freeboard_writeClearBtn;   //자유게시판 글쓰기 완료 버튼
    TextView backBtn;                   //뒤로가기 버튼
    EditText freeboard_edit_content;    //글쓰기 - 내용

    private WriteAsyncThread backgroundWriteThread;


    /*
        이미지를 업로드 하기위해 권한 허용
    */
    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(25)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }


    /*
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
                else{

                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.freeboard_write); //자유게시판 글쓰기

        Intent intent = getIntent();                    //FreeNoticeBoard_Main에서 FreeNoticeBoard_Write로 인텐트를 할 떄 넘겨준
                                                        //현재 리스트 뷰의 개수를 받아옴.
        indexNum = intent.getExtras().getInt("no");    //받아온 리스트 뷰의 key값인 "no"을 정수형 변수 n에 저장
        Log.d("여기서 현재의 키값>>>>>>>>>>>>.",indexNum+"");

        freeboard_writeClearBtn = (TextView) findViewById(R.id.freeboard_writeClearBtn);    //글쓰기완료버튼
        freeboard_writeClearBtn.setOnClickListener(writtingClearListener);                  //글쓰기완료버튼 리스너

        image_input = (ImageView)findViewById(R.id.image_input);    //이미지 들어갈 자리
        gallaryBtn = (TextView) findViewById(R.id.gallaryBtn);
        gallaryBtn.setOnClickListener(gallaryBtnListener);          //갤러리버튼 리스너
        photoBtn = (TextView) findViewById(R.id.photoBtn);
        photoBtn.setOnClickListener(photoBtnListener);              //사진찍기버튼 리스너

        backBtn = (TextView)findViewById(R.id.backBtn);
        backBtn.setOnClickListener(backBtnClickListener);

        //이미지 업로드 시 해상도
        try {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            xValue = yValue = displayMetrics.widthPixels;
        } catch (Exception e) {
            Log.e("ERR", "Scale  ERR : " + e.getMessage());
        }

        //이미지 업로드 권한 허용
        if (shouldAskPermissions()) {
            askPermissions();
        }

    }//onCreate끝

    /*
        어플리케이션 종료시 쓰레드의 종료를 요청하는 메소드
    */
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            //if (backgroundWriteThread.getStatus() == AsyncTask.Status.RUNNING) {//------------------------------------->>주석처리(차후에 수정)
                //backgroundWriteThread.cancel(true);
            //}
        } catch (Exception e) {}
    }

    /*
        사진 업로드를 사용자 핸드폰의 갤러리로부터 하는 경우,
         갤러리로 이동시켜 사진을 선택할 수 있게 하는 리스너
    */
    TextView.OnClickListener gallaryBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
            startActivityForResult(intent, PICK_FROM_ALBUM);
        }
    };

    /*
        사용자가 직접 사진을 찍는 경우, 사진 앱으로 연결해주는 리스너
    */
    TextView.OnClickListener photoBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // 임시로 사용할 파일의 경로를 생성
            String url = "ex_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
            mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));

            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            startActivityForResult(intent, PICK_FROM_CAMERA);
        }
    };

    /*
        사용자가 직접 사진을 찍거나 갤러리에서 특정 사진을 선택 한 후 이미지를 편집하는 메소드
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            // 갤러리로 부터 사진을 선택한 경우, 이미지를 가져온다. 이후의 처리가 직접 사진을 찍은 경우와 같으므로 일단  break없이 진행한다.
            case PICK_FROM_ALBUM :
                mImageCaptureUri = data.getData();
                Log.d("갤러리에서 사진을 눌렀을 때 >>>", mImageCaptureUri.getPath().toString());
                // 직접 사진을 찍은 경우, 사진을 찍은 후 크기를 알맞게 크롭한다.
            case PICK_FROM_CAMERA:
                // 이미지를 가져온 이후의 리사이즈할 이미지 크기를 결정합니다.
                // 이후에 이미지 크롭 어플리케이션을 호출하게 됩니다.
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mImageCaptureUri, "image/*");

                // CROP할 이미지를 저장
                intent.putExtra("outputX", xValue); // CROP한 이미지의 x축 크기
                intent.putExtra("outputY", yValue); // CROP한 이미지의 y축 크기
                intent.putExtra("aspectX", 1); // CROP 박스의 X축 비율
                intent.putExtra("aspectY", 1); // CROP 박스의 Y축 비율
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, CROP_FROM_iMAGE); // CROP_FROM_CAMERA case문 이동
                break;
            // 크롭된 이후의 이미지 설정, 이미지뷰에 이미지를 보여준다거나 부가적인 작업 이후에 임시 파일을 삭제한다.
            case CROP_FROM_iMAGE:
                if (resultCode != RESULT_OK) {
                    return;
                }
                final Bundle extras = data.getExtras();
                // CROP된 이미지를 저장하기 위한 FILE 경로
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FreeBoard/" + System.currentTimeMillis() + ".jpg";
                Log.d(filePath, " <<< 크롭된 이미지가 저장된 경로");
                if (extras != null) {
                    Bitmap photo = extras.getParcelable("data"); // CROP된 BITMAP
                    storeCropImage(photo, filePath); // CROP된 이미지를 외부저장소, 앨범에 저장한다.
                    uploadingImage = new BitmapDrawable(getResources(), photo);
                    Log.d("카메라로 찍은 사진이 저장 됨 >>> ", "완료");
                    image_input.setImageDrawable(uploadingImage); // 레이아웃의 이미지칸에 CROP된 BITMAP을 보여줌
                    break;
                }

                // 임시 파일 삭제
                File f = new File(mImageCaptureUri.getPath());
                if (f.exists()) {
                    f.delete();
                }
        }
    }

    /*
        크롭할 이미지를 저장하는 메소드
    */
    private void storeCropImage(Bitmap bitmap, String filePath) {
        // SmartWheel 폴더를 생성하여 이미지를 저장하는 방식이다.
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FreeBoard/";
        Log.d("경로가 뭐니",dirPath);
        File directory_FreeBoard = new File(dirPath);

        if (!directory_FreeBoard.exists()) { // FreeBoard 디렉터리에 폴더가 없다면 (새로 이미지를 저장할 경우에 속한다.)
            directory_FreeBoard.mkdir();
            Log.d("디렉터리에 폴더없으면 생성","");
        }

        File copyFile = new File(filePath);
        BufferedOutputStream out = null;

        try {
            copyFile.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(copyFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            // sendBroadcast를 통해 Crop된 사진을 앨범에 보이도록 갱신한다.
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(copyFile))); //getContext지움

            out.flush();
            out.close();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error 발생", Toast.LENGTH_SHORT).show();
            Log.e("ERR", "Image Setting  ERR : " + e.getMessage());
            Log.d("이미지 셋팅 에러","ㅜㅜ");
        }
    }


    /*
        뒤로가기 버튼 클릭시 인텐트 종료
    */
    TextView.OnClickListener backBtnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            finish();
        }
    };

    /*
        글쓰기 완료버튼 리스너 부분
    */
    TextView.OnClickListener writtingClearListener = new View.OnClickListener() {

        String content = null;
        boolean flag = true;

        @Override
        public void onClick(View v) {
            Log.d("완료버튼","누름");

            freeboard_edit_content = (EditText)findViewById(R.id.freeboard_edit_content); //글쓰기 content

            // 설명을 쓰지 않는 경우 요청 불가 설정
                try {
                    content = freeboard_edit_content.getText().toString();
                    Log.d("내용",content);
                    if (content.length() == 0) {
                        flag = false;
                        Log.d("글안씀","-----------------------------------------------" + flag);
                        Toast.makeText(getApplicationContext(), "내용을 입력해주세요.", Toast.LENGTH_LONG).show();
                    } else {
                        flag = true;
                        Log.d("글씀","-----------------------------------------------" + flag);
                        //Toast.makeText(getApplicationContext(), "내용을 입력했습니다.", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    flag = false;
                    Log.d("에러에러","-----------------------------------------------" + flag);
                    Toast.makeText(getApplicationContext(), "Demand Content ERR : " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            // 모든 요청 데이터가 채워진 경우, 요청 완료(DB 업로드, List 업로드) 후 다이얼로그 종료
            if (flag) {
                final DBManager dbManager = new DBManager(getApplicationContext(), "app_data.db", null, 1);
                final HashMap<String, String> dataMap = dbManager.getMemberInfo();
                String userName = dataMap.get("name");
                Calendar cal = Calendar.getInstance();
                String date = (cal.get(Calendar.YEAR) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.DATE));
                dbManager.write(indexNum, userName, content, date);
                //dbManager.tablecreate();

                //------------------------------------->>주석처리(차후에 수정)
                //insertDatabase(indexNum, userName, content, date); //DB에 새로운 데이터를 추가

                FreeNoticeBoard_Main.a = true;
                Intent intent = new Intent(getApplicationContext(), FreeNoticeBoard_Main.class);
                startActivity(intent);
                finish();
            }
        }//onClick끝끝
    }; //TextView리스너 끝


    /*
            DB에 새로운 데이터를 추가하는 메소드
    */
    private void insertDatabase(int uniqueNum, String userName, String content, String date) {
        //backgroundWriteThread = new WriteAsyncThread();//------------------------------------------>>주석처리(차후에 수정)
        //backgroundWriteThread.execute(uniqueNum+"", userName, content, date);
    }

    public class WriteAsyncThread extends AsyncTask<String, String, String> {
        // Thread를 시작하기 전에 호출되는 함수
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Thread의 주요 작업을 처리 하는 함수
        // Thread를 실행하기 위해 excute(~)에서 전달한 값을 인자로 받는다.
        protected String doInBackground(String... args) {
            try {
                URL url = null;
                HttpURLConnection conn = null;
                String urlStr = "";

                //urlStr = "http://"+getString(R.string.ip_address)+":8080/BookDreamServerProject/test_addFreeNoticeBoardInfo"; 원래거
                //urlStr = "http://172.30.21.64:8080/BookDreamServerProject/test_addFreeNoticeBoardInfo"; //테스트용
                url = new URL(urlStr);
                Log.d("test", urlStr);
                OutputStream os = null;
                InputStream is = null;
                ByteArrayOutputStream baos = null;
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                ObjectOutputStream oos = new ObjectOutputStream(conn.getOutputStream());
                HashMap<String, String> requestDataMap = new HashMap<>();
                requestDataMap.put("no", args[0]);
                requestDataMap.put("user", args[1]);
                requestDataMap.put("content", args[2]);
                requestDataMap.put("date", args[3]);
                oos.writeObject(requestDataMap);
                oos.flush();
                oos.close();
                Log.d("test-------------------", ">> write");

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { // 서버가 받았다면
                    Log.d("get","서버데이터 받음");
                    ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                    HashMap<String,String> responseDataMap = (HashMap<String,String>)ois.readObject();
                    ois.close();

                    Log.d("get","서버데이터 추가함");
                    Bundle b = new Bundle();
                    if(responseDataMap.size() ==0 ) {
                        b.putString("status", "write_fail");
                    }
                    else {
                        b.putString("status", "write");
                        b.putString("no", responseDataMap.get("no"));
                        b.putString("user", responseDataMap.get("user"));
                        b.putString("content", responseDataMap.get("content"));
                        b.putString("date", responseDataMap.get("date"));
                        Message msg = handler.obtainMessage();
                        msg.setData(b);
                        handler.sendMessage(msg);
                    }
                }
                conn.disconnect();
                return "OK";
            } catch (Exception e) {
                //Toast.makeText(getActivity(), "Error 발생", Toast.LENGTH_SHORT).show();
                Log.e("ERR", "WriteAsyncThread ERR : " + e);
            }
            return "";
        }//doInBackground끝
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

    }//WriteAsyncThread끝

}
