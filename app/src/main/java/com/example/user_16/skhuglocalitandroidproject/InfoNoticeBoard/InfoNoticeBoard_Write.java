package com.example.user_16.skhuglocalitandroidproject.InfoNoticeBoard;

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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class InfoNoticeBoard_Write extends AppCompatActivity {

    static int indexNum_info;

    TextView Infoboard_gallaryBtn, Infoboard_photoBtn;      // 갤러리,사진찍기
        private static final int PICK_FROM_CAMERA = 0;
        private static final int PICK_FROM_ALBUM = 1;
        private static final int CROP_FROM_iMAGE = 2;
        private Uri mImageCaptureUri;
        private int xValue, yValue;
        private Drawable uploadingImage = null;
        private Drawable getImage = null;

    private ImageView Infoboard_image_input;                //정보게시판 이미지 넣기
    TextView Infoboard_writeClearBtn;                       //정보게시판 글쓰기 완료 버튼
    TextView Infoboard_backBtn;                             //정보게시판 뒤로가기 버튼
    EditText Infoboard_edit_title, Infoboard_edit_content;  //정보게시판 글 제목, 글 내용

    static public WriteAsyncThread backgroundWriteThread;   //글쓰기 스레드

    /*-------------------------------------
       이미지를 업로드 하기위해 권한 허용
   ---------------------------------------*/
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

    /*-----------------------------------------------------------
       정보 게시판에 글을 올린 경우, DB와 상호작용 하는 핸들러
    ------------------------------------------------------------*/
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
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.infoboard_write); // 정보 게시판 글쓰기

        Intent intent = getIntent();                        //InfoNoticeBoard_Main에서 InfoNoticeBoard_Write로 인텐트를 할 떄 넘겨준
                                                            //현재 리스트 뷰의 개수를 받아옴.
        indexNum_info = intent.getExtras().getInt("no");    //받아온 리스트 뷰의 key값인 "no"을 정수형 변수 n에 저장
        Log.d("여기서 현재의 키 값>>>>>>>>>>.",indexNum_info+"");

        Infoboard_writeClearBtn = (TextView) findViewById(R.id.infoboard_writeClearBtn);    //글쓰기완료버튼
        Infoboard_writeClearBtn.setOnClickListener(writtingClearListener2);                 //글쓰기완료버튼 리스너

        Infoboard_gallaryBtn = (TextView) findViewById(R.id.infoboard_gallaryBtn);
        Infoboard_gallaryBtn.setOnClickListener(gallaryBtnListener);                        //갤러리버튼 리스너
        Infoboard_photoBtn = (TextView) findViewById(R.id.infoboard_photoBtn);
        Infoboard_photoBtn.setOnClickListener(photoBtnListener);                            //사진찍기버튼 리스너

        Infoboard_backBtn = (TextView)findViewById(R.id.infoboard_backBtn);
        Infoboard_backBtn.setOnClickListener(backBtnClickListener);                         //뒤로가기 버튼 리스너

        Infoboard_image_input = (ImageView)findViewById(R.id.infoboard_image_input);         //이미지 들어갈 자리

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

    }

    /*----------------------------------------------------------------------------------------------
        어플리케이션 종료시 쓰레드의 종료를 요청하는 메소드
    ----------------------------------------------------------------------------------------------*/
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (backgroundWriteThread.getStatus() == AsyncTask.Status.RUNNING) {
                backgroundWriteThread.cancel(true);
             }
        } catch (Exception e) {}
    }


    /*----------------------------------------------------------------------------------------------
        정보 게시판 글쓰기 완료버튼 리스너
    ----------------------------------------------------------------------------------------------*/
    TextView.OnClickListener writtingClearListener2 = new View.OnClickListener() {
        String title = null;    //글 제목
        String content = null;  //글 내용
        boolean flag;

        @Override
        public void onClick(View v) {
            Log.d("정보게시판 글쓰기 완료 버튼","누름");

            flag = true;
            Infoboard_edit_title = (EditText)findViewById(R.id.infoboard_edit_title);       //글쓰기 title
            Infoboard_edit_content = (EditText)findViewById(R.id.infoboard_edit_content);   //글쓰기 content
            Infoboard_image_input = (ImageView)findViewById(R.id.infoboard_image_input);    //이미지 들어갈 자리

            // 제목을 쓰지 않는 경우 요청 불가 설정
            if (flag) {
                try {
                    title = Infoboard_edit_title.getText().toString();
                    Log.d("제목", title);
                    if (title.length() == 0) {
                        flag = false;
                        Log.d("글 제목 안씀", "-----------------------------------------------" + flag);
                        Toast.makeText(getApplicationContext(), "제목을 입력해주세요.", Toast.LENGTH_LONG).show();
                    } else {
                        flag = true;
                        Log.d("글 제목 씀", "-----------------------------------------------" + flag);
                    }
                } catch (Exception e) {
                    flag = false;
                    Log.d("오류 발생...", "-----------------------------------------------" + flag);
                    Toast.makeText(getApplicationContext(), "Demand Content ERR : " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            // 설명을 쓰지 않는 경우 요청 불가 설정
            if (flag) {
                try {
                    content = Infoboard_edit_content.getText().toString();
                    Log.d("내용", content);
                    if (content.length() == 0) {
                        flag = false;
                        Log.d("글안씀", "-----------------------------------------------" + flag);
                        Toast.makeText(getApplicationContext(), "내용을 입력해주세요.", Toast.LENGTH_LONG).show();
                    } else {
                        flag = true;
                        Log.d("글씀", "-----------------------------------------------" + flag);
                    }
                } catch (Exception e) {
                    flag = false;
                    Log.d("오류 발생...", "-----------------------------------------------" + flag);
                    Toast.makeText(getApplicationContext(), "Demand Content ERR : " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            // 사용자가 이미지를 제대로 업로드 했는지를 체크
            if (flag) {
                try {
                    if (uploadingImage != null) {
                        getImage = uploadingImage;
                        flag = true;
                    } else {
                        // 이미지를 업로드 안했을 시 빈 이미지 삽입
                        getImage = getResources().getDrawable(R.drawable.empty_image);
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error 발생", Toast.LENGTH_SHORT).show();
                    Log.e("ERR", "Image  ERR : " + e.getMessage());
                    flag = false;
                }
            }

            // 모든 데이터가 채워진 경우, 글쓰기 완료(DB 업로드, List 업로드) 후 메인화면으로 인텐트
            if (flag) {
                final DBManager dbManager = new DBManager(getApplicationContext(), "app_data.db", null, 1);
                final HashMap<String, String> dataMap = dbManager.getMemberInfo();
                String userName = dataMap.get("name");
                Calendar cal = Calendar.getInstance();
                String date = (cal.get(Calendar.YEAR) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.DATE));

                insertDatabase(indexNum_info, title, userName, content, date, getImage); //DB에 새로운 데이터를 추가

                InfoNoticeBoard_Main.a = true;
                Intent intent = new Intent(getApplicationContext(), InfoNoticeBoard_Main.class);
                startActivity(intent);
                InfoNoticeBoard_Write.this.finish();

                Toast.makeText(getApplicationContext(), "글쓰기가 완료되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    };


     /*---------------------------------------------------------------------------------------------
        DB에 새로운 데이터를 추가하는 메소드
     ---------------------------------------------------------------------------------------------*/
    public void insertDatabase(int indexNum_info, String title, String userName, String content, String date, Drawable getImage) {
        Bundle b = new Bundle();
        b.putString("no", indexNum_info+"");
        b.putString("title", title);
        b.putString("user", userName);
        b.putString("content", content);
        b.putString("date", date);
        b.putString("file_name", indexNum_info + "_" + title + "_" + date + ".png");

        Message msg= new Message();
        msg.setData(b);
        msg.obj = getImage;
        backgroundWriteThread = new WriteAsyncThread();
        backgroundWriteThread.execute(msg);
    }

    public class WriteAsyncThread extends AsyncTask<Message, String, String> {
        // Thread를 시작하기 전에 호출되는 함수
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // Thread의 주요 작업을 처리 하는 함수
        // Thread를 실행하기 위해 excute(~)에서 전달한 값을 인자로 받습니다.
        protected String doInBackground(Message... args) {
            Message dataMsg = args[0];
            Bundle stringData = dataMsg.getData();
            URL url = null;
            HttpURLConnection conn = null;
            String urlStr = "";

            urlStr = "http://"+getString(R.string.ip_address)+":8080/SkhuGlocalitWebProject/InfoNoticeBoard_Write";
            //urlStr = "http://192.168.25.55:8080/BookDreamServerProject/InfoNoticeBoard_Write"; //테스트용

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

                HashMap<String, String> stringDataMap = new HashMap<String, String>();
                stringDataMap.put("no", stringData .getString("no"));
                stringDataMap.put("title",stringData .getString("title"));
                stringDataMap.put("user",stringData .getString("user"));
                stringDataMap.put("content",stringData .getString("content"));
                stringDataMap.put("date",stringData .getString("date"));
                stringDataMap.put("file_name",stringData .getString("file_name"));

                Bitmap bitmap = ((BitmapDrawable) dataMsg.obj).getBitmap();
                ArrayList<byte[]> list = new ArrayList<byte[]>();
                ByteArrayOutputStream stream = new ByteArrayOutputStream() ;
                bitmap.compress( Bitmap.CompressFormat.PNG, 100, stream) ;
                list.add(stream.toByteArray());

                ObjectOutputStream oos =new ObjectOutputStream(conn.getOutputStream());
                oos.writeObject(stringDataMap);
                oos.flush();
                oos.writeObject(list);
                oos.flush();
                oos.close();
                Log.d("test-------------------", ">> 정보 게시판 write");

                int responseCode = conn.getResponseCode();
                Log.d("D", responseCode+"");
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("test", "gogo");

                    ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                    HashMap<String, String> dataMap = (HashMap<String,String>)ois.readObject();
                    ArrayList<byte[]> imgByteList= (ArrayList<byte[]>)ois.readObject();
                    ois.close();

                    Message msg = handler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("no", dataMap.get("no"));
                    b.putString("title", dataMap.get("title"));
                    b.putString("user", dataMap.get("user"));
                    b.putString("content", dataMap.get("content"));
                    b.putString("date", dataMap.get("date"));

                    msg.obj = imgByteList.get(0);
                    msg.setData(b);
                    handler.sendMessage(msg);
                }

                conn.disconnect();
            } catch (Exception e) {
                Log.e("ERR", "WriteAsyncThread ERR : " + e.getMessage());
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


    /*----------------------------------------------------------------------------------------------
        사진 업로드를 사용자 핸드폰의 갤러리로부터 하는 경우,
        갤러리로 이동시켜 사진을 선택할 수 있게 하는 리스너
    ----------------------------------------------------------------------------------------------*/
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
                    Infoboard_image_input.setImageDrawable(uploadingImage); // 레이아웃의 이미지칸에 CROP된 BITMAP을 보여줌
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
            InfoNoticeBoard_Write.this.finish();
        }
    };

}
