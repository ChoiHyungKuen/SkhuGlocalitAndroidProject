package com.example.user_16.skhuglocalitandroidproject.InfoNoticeBoard;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
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

import static com.example.user_16.skhuglocalitandroidproject.R.id.infoboard_view_image;

public class InfoNoticeBoard_View extends AppCompatActivity {

    // 이미지 크롭 관련
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_iMAGE = 2;
    private Uri mImageCaptureUri;
    private int xValue, yValue;
    private Drawable uploadingImage = null;
    private Drawable getImage = null;

    int position;

    private ImageView Infoboard_view_image;
    TextView Infoboard_view_title, Infoboard_view_user, Infoboard_view_content, Infoboard_view_date;
    TextView Infoboard_backBtn, infoboard_view_editBtn;

    // 인텐트 할 때 받아온 데이터 저장
    byte[] intentImage;
    String intentTitle, intentUser, intentContent, intentDate;

    // 글 수정 창(다이얼로그)
    TextView info_update_user, info_update_date, info_update_photoBtn, info_update_gallaryBtn, info_update_backBtn;
    EditText info_update_title, info_update_content;
    ImageView info_update_image;

    private UpdateAsyncThread backgroundUpdateThread;           // 글 수정 스레드

    /*--------------------------------------
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

    /*---------------------------------------------------------
       정보 게시판에 글을 올린 경우, DB와 상호작용 하는 핸들러
    ---------------------------------------------------------*/
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
        setContentView(R.layout.infoboard_view);

        Infoboard_view_title = (TextView)findViewById(R.id.infoboard_view_title);
        Infoboard_view_user = (TextView)findViewById(R.id.infoboard_view_user);
        Infoboard_view_content = (TextView)findViewById(R.id.infoboard_view_content);
        Infoboard_view_date = (TextView)findViewById(R.id.infoboard_view_date);
        Infoboard_view_image = (ImageView)findViewById(infoboard_view_image);

        Infoboard_backBtn = (TextView)findViewById(R.id.infoboard_view_backBtn);        // 뒤로가기 버튼
        Infoboard_backBtn.setOnClickListener(backBtnClickListener);
        infoboard_view_editBtn = (TextView)findViewById(R.id.infoboard_view_editBtn);   // 글 수정 버튼

        // Main에서 인텐트 할 때 넘겨준 값 받아오기
        Intent intent = getIntent();
            position = intent.getExtras().getInt("position");     //받아온 리스트 뷰의 position값을 정수형 변수 position에 저장
            Log.d("View에서 현재 position값", position+"");
            intentImage = (byte[]) intent.getExtras().get("intentImage");
            Bitmap bitmap = BitmapFactory.decodeByteArray(intentImage, 0, intentImage.length);      // 인텐트로 받은 데이터형은 byte[]형이고 이걸 다시 비트맵으로
            intentTitle = intent.getExtras().getString("intentTitle");                              //인텐트 할 때 넘겨준 데이터 값을 저장
            intentUser = intent.getExtras().getString("intentUser");
            intentContent = intent.getExtras().getString("intentContent");
            intentDate = intent.getExtras().getString("intentDate");

        // view에 출력
        Infoboard_view_image.setImageBitmap(bitmap);
        Infoboard_view_title.setText(intentTitle);
        Infoboard_view_user.setText(intentUser);
        Infoboard_view_content.setText(intentContent);
        Infoboard_view_date.setText(intentDate);

        // 글 수정 전 내용 불러오기
        final Bitmap b = bitmap;                                                                  // 수정 전 이미지
        final String getTitle = Infoboard_view_title.getText().toString();                        Log.d("수정 전 제목", getTitle);
        final String getUser = Infoboard_view_user.getText().toString();                          Log.d("수정 전 유저", getUser);
        final String getDate = Infoboard_view_date.getText().toString();                          Log.d("수정 전 날짜", getDate);
        final String getContent = Infoboard_view_content.getText().toString();                    Log.d("수정 전 내용", getContent);

        // 글 수정버튼 클릭 시 다이얼로그 창 띄우기
        infoboard_view_editBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                final DBManager dbManager = new DBManager(getApplicationContext(), "app_data.db", null, 1);
                final HashMap<String, String> dataMap = dbManager.getMemberInfo();
                String userName = dataMap.get("name");
                if(!intentUser.equals(userName)) {          //글작성자 아니면 수정 못함
                    Toast.makeText(getApplicationContext(), "글 작성자가 아닙니다.", Toast.LENGTH_LONG).show();
                    return ;
                }

                // Dialog에서 보여줄 입력화면 View 객체 생성 작업
                LayoutInflater inflater = InfoNoticeBoard_View.this.getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.infoboard_update, null);

                info_update_image = (ImageView) alertLayout.findViewById(R.id.info_update_image);
                info_update_title = (EditText) alertLayout.findViewById(R.id.info_update_title);
                info_update_user = (TextView)alertLayout.findViewById(R.id.info_update_user);
                info_update_content = (EditText) alertLayout.findViewById(R.id.info_update_content);
                info_update_date = (TextView)alertLayout.findViewById(R.id.info_update_date);

                info_update_backBtn = (TextView)alertLayout.findViewById(R.id.info_update_backBtn);
                info_update_photoBtn = (TextView)alertLayout.findViewById(R.id.info_update_photoBtn);
                info_update_gallaryBtn = (TextView)alertLayout.findViewById(R.id.info_update_gallaryBtn);

                info_update_backBtn.setOnClickListener(backListener);
                info_update_photoBtn.setOnClickListener(takePhotoClickListener);                            // 수정 창의 카메라 버튼
                info_update_gallaryBtn.setOnClickListener(uploadClickListener);                             // 수정 창의 갤러리 버튼

                // 수정 전 내용 출력
                info_update_image.setImageBitmap(b);
                info_update_title.setText(getTitle);
                info_update_user.setText(getUser);
                info_update_date.setText(getDate);
                info_update_content.setText(getContent);

                AlertDialog.Builder buider = new AlertDialog.Builder(InfoNoticeBoard_View.this); // AlertDialog.Builder 객체 생성
                buider.setCancelable(false);
                buider.setView(alertLayout); // 위에서 inflater가 만든 dialogView 객체 세팅 (Customize)

                // Dialog에 "Cancel"이라는 타이틀의 버튼을 설정
                buider.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // 다이얼로그 종료
                    }
                });

                // 글 수정 서블릿 코드 작성
                buider.setPositiveButton("글 수정", new DialogInterface.OnClickListener() { // Dialog에 "확인"라는 타이틀의 버튼을 설정
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean flag;
                        Calendar cal = Calendar.getInstance();
                        String date = (cal.get(Calendar.YEAR) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.DATE));

                        String editTitle = info_update_title.getText().toString();                       Log.d("수정 후 제목", editTitle);
                        String editUser = info_update_user.getText().toString();
                        String editContent = info_update_content.getText().toString();                   Log.d("수정 후 내용", editContent);

                        // 이미지 업로드 여부
                        flag = true;
                        if (flag) {
                            try {
                                if (uploadingImage != null) {
                                    getImage = uploadingImage;

                                } else {                                // 이미지 업로드를 안했다면 수정 전 이미지 그대로 삽입
                                    getImage = new BitmapDrawable(b);   // 수정 전 이미지(비트맵)을 Drawable로 변환
                                }

                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "Error 발생", Toast.LENGTH_SHORT).show();
                                Log.e("ERR", "Image  ERR : " + e.getMessage());
                                flag = false;
                            }
                        }

                        // 수정된 데이터를 DB로 보냄
                        insertDatabase(position, editTitle, editUser, editContent, date, getImage);
                        Log.d("수정창 다이얼로그에서 position값", position+"");

                        // 수정된 내용 출력
                        Infoboard_view_image.setImageDrawable(getImage);
                        Infoboard_view_title.setText(editTitle);
                        Infoboard_view_user.setText(editUser);
                        Infoboard_view_content.setText(editContent);
                        Infoboard_view_date.setText(date);

                        Toast.makeText(getApplicationContext(), "글이 수정되었습니다. 새로고침해주세요.", Toast.LENGTH_SHORT).show();
                    }
                });

                //설정한 값으로 AlertDialog 객체 생성
                android.support.v7.app.AlertDialog dialog2 = buider.create();

                //Dialog의 바깥쪽을 터치했을 때 Dialog를 없앨지 설정
                dialog2.setCanceledOnTouchOutside(false);//없어지지 않도록 설정

                //Dialog 보이기
                dialog2.show();
            }
        });

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
            if (backgroundUpdateThread.getStatus() == AsyncTask.Status.RUNNING) {
                backgroundUpdateThread.cancel(true);
            }
        } catch (Exception e) {}
    }

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

        backgroundUpdateThread = new UpdateAsyncThread();
        backgroundUpdateThread.execute(msg);
    }

    public class UpdateAsyncThread extends AsyncTask<Message, String, String> {
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

            urlStr = "http://"+getString(R.string.ip_address)+":8080/SkhuGlocalitWebProject/InfoNoticeBoard_Update";
            //urlStr = "http://192.168.25.55:8080/BookDreamServerProject/InfoNoticeBoard_Update"; //테스트용

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
                Log.d("test-------------------", ">> 정보 게시판 Update");

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


    TextView.OnClickListener backListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            finish();
        }
    };

    /*----------------------------------------------------------------------------------------------
       사진 업로드를 사용자 핸드폰의 갤러리로부터 하는 경우,
       갤러리로 이동시켜 사진을 선택할 수 있게 하는 리스너
    ----------------------------------------------------------------------------------------------*/
    TextView.OnClickListener uploadClickListener = new View.OnClickListener() {

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
    TextView.OnClickListener takePhotoClickListener = new View.OnClickListener() {

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
            // 갤러리로 부터 사진을 선택한 경우, 이미지를 가져온다.
            // 이후의 처리가 직접 사진을 찍은 경우와 같으므로 일단  break없이 진행한다.
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
                intent.putExtra("aspectX", 1);      // CROP 박스의 X축 비율
                intent.putExtra("aspectY", 1);      // CROP 박스의 Y축 비율
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
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/NoticeBoard/" + System.currentTimeMillis() + ".jpg";
                Log.d(filePath, " <<< 크롭된 이미지가 저장된 경로");
                if (extras != null) {
                    Bitmap photo = extras.getParcelable("data"); // CROP된 BITMAP
                    storeCropImage(photo, filePath); // CROP된 이미지를 외부저장소, 앨범에 저장한다.
                    uploadingImage = new BitmapDrawable(getResources(), photo);
                    Log.d("카메라로 찍은 사진이 저장 됨 >>> ", "완료");
                    info_update_image.setImageDrawable(uploadingImage); // 레이아웃의 이미지칸에 CROP된 BITMAP을 보여줌
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
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/NoticeBoard/";
        File directory_FreeBoard = new File(dirPath);

        if (!directory_FreeBoard.exists()) { // InfoBoard 디렉터리에 폴더가 없다면 (새로 이미지를 저장할 경우에 속한다.)
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
            Log.d("이미지 셋팅 에러","실패");
        }
    }

    // 뒤로가기 버튼 클릭시 인텐트 종료
    TextView.OnClickListener backBtnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            InfoNoticeBoard_View.this.finish();
        }
    };
}
