package com.example.user_16.skhuglocalitandroidproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class DepartmentNoticeboardContentActivity extends AppCompatActivity {
    private ContentDepartmentInfoAsyncThread backgroundContentDepartmentInfoThread;
    private DownloadDepartmentInfoAsyncThread backgroundDownloadDepartmentInfoThread;
    private TextView contentTx, dateTx, writerTx, titleTx, downloadTx;
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            HashMap<String, String> dataMap = (HashMap<String, String>) msg.obj;
            if (b.get("state") != null) {
                if (b.get("state").equals("fail"))
                    Toast.makeText(getApplicationContext(), "파일을 다운로드 하지 못 했습니다. \n잠시만 기다려주세요.", Toast.LENGTH_LONG).show();
                else {
                    Toast.makeText(getApplicationContext(), "파일을 다운로드 했습니다.", Toast.LENGTH_LONG).show();
                    String filePath = b.getString("filePath");
                    String fileName = b.getString("fileName");
                    viewFile(DepartmentNoticeboardContentActivity.this, filePath, fileName);

                }
            } else {
                if (dataMap != null && dataMap.get("downloadName").equals("")) {
                    downloadTx.setVisibility(View.INVISIBLE);
                    downloadTx.setEnabled(false);
                    contentTx.setText("\n" + dataMap.get("content"));
                } else {
                    downloadTx.setText("첨부파일 : " + dataMap.get("downloadName"));
                    downloadTx.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(DepartmentNoticeboardContentActivity.this);
                            alert.setTitle("파일 다운로드 ");
                            alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    StringTokenizer stz = new StringTokenizer(downloadTx.getText().toString(), " : ");
                                    stz.nextToken();
                                    String download = stz.nextToken();
                                    String title = titleTx.getText().toString();
                                    stz = new StringTokenizer(dateTx.getText().toString(), " : ");
                                    stz.nextToken();
                                    String date = stz.nextToken();
                                    Log.d("ddfde", title + date);
                                    backgroundDownloadDepartmentInfoThread = new DownloadDepartmentInfoAsyncThread();
                                    backgroundDownloadDepartmentInfoThread.execute(title, date, download);
                                }
                            });
                            alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            alert.setIcon(R.drawable.download);

                            alert.setMessage("이 파일을 다운로드 하시겠습니까?");
                            alert.show();
                        }
                    });
                }
                contentTx.setText("\n" + dataMap.get("content"));
            }

        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department_noticeboard_content);
        contentTx = (TextView) findViewById(R.id.department_content);
        dateTx = (TextView) findViewById(R.id.department_date);
        writerTx = (TextView) findViewById(R.id.department_writer);
        titleTx = (TextView) findViewById(R.id.department_title);
        downloadTx = (TextView) findViewById(R.id.department_download);

        Intent intent = getIntent();
        titleTx.setText(intent.getStringExtra("title"));
        writerTx.setText("작성자 : " + intent.getStringExtra("writer"));
        dateTx.setText("작성날짜 : " + intent.getStringExtra("date"));

        backgroundContentDepartmentInfoThread = new ContentDepartmentInfoAsyncThread();
        backgroundContentDepartmentInfoThread.execute(intent.getStringExtra("title"), intent.getStringExtra("date"));
    }

    /**
     *  확장자를 추출하는 함수
     */
    public static String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    /**
     *  각 파일의 확장자별로 지정해준 뒤 암시인텐트로 열수있는 어플리케이션을 연결해서
     *  그 파일을 볼 수 있게 해주는 함수
     */
    public static void viewFile(Context context, String filePath, String fileName) {
        Intent fileLinkIntent = new Intent(Intent.ACTION_VIEW);
        fileLinkIntent.addCategory(Intent.CATEGORY_DEFAULT);
        File file = new File(filePath, fileName);
        Uri uri = Uri.fromFile(file); //확장자 구하기
        String fileExtend = getExtension(file.getAbsolutePath()); // 파일 확장자 별로 mime type 지정해 준다.
        if (fileExtend.equalsIgnoreCase("mp3")) {
            fileLinkIntent.setDataAndType(Uri.fromFile(file), "audio/*");
        } else if (fileExtend.equalsIgnoreCase("mp4")) {
            fileLinkIntent.setDataAndType(Uri.fromFile(file), "vidio/*");
        } else if (fileExtend.equalsIgnoreCase("jpg") || fileExtend.equalsIgnoreCase("jpeg") || fileExtend.equalsIgnoreCase("gif") || fileExtend.equalsIgnoreCase("png") || fileExtend.equalsIgnoreCase("bmp")) {
            fileLinkIntent.setDataAndType(Uri.fromFile(file), "image/*");
        } else if (fileExtend.equalsIgnoreCase("txt")) {
            fileLinkIntent.setDataAndType(Uri.fromFile(file), "text/*");
        } else if (fileExtend.equalsIgnoreCase("doc") || fileExtend.equalsIgnoreCase("docx")) {
            fileLinkIntent.setDataAndType(Uri.fromFile(file), "application/msword");
        } else if (fileExtend.equalsIgnoreCase("xls") || fileExtend.equalsIgnoreCase("xlsx")) {
            fileLinkIntent.setDataAndType(Uri.fromFile(file), "application/vnd.ms-excel");
        } else if (fileExtend.equalsIgnoreCase("ppt") || fileExtend.equalsIgnoreCase("pptx")) {
            fileLinkIntent.setDataAndType(Uri.fromFile(file), "application/vnd.ms-powerpoint");
        } else if (fileExtend.equalsIgnoreCase("pdf")) {
            fileLinkIntent.setDataAndType(Uri.fromFile(file), "application/pdf");
        } else if (fileExtend.equalsIgnoreCase("hwp")) {
            fileLinkIntent.setDataAndType(Uri.fromFile(file), "application/haansofthwp");
        }
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(fileLinkIntent, PackageManager.GET_META_DATA);
        // 열수 있는 어플리케이션이 없으면
        if (list.size() == 0) {
            Toast.makeText(context, fileName + "을 확인할 수 있는 앱이 설치되지 않았습니다.", Toast.LENGTH_SHORT).show();
        } else {
            context.startActivity(fileLinkIntent);
        }
    }


    public class ContentDepartmentInfoAsyncThread extends AsyncTask<String, String, String> {

        ProgressDialog pd = new ProgressDialog(DepartmentNoticeboardContentActivity.this);

        // Thread를 시작하기 전에 호출되는 함수
        protected void onPreExecute() {
            super.onPreExecute();

            pd.setTitle("정보 받아오는 중...");
            pd.setMessage("잠시만 기다려주세요. \n 게시글을 가져오고 있습니다.");
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
            dataMap.put("title", args[0]);
            dataMap.put("date", args[1]);

            urlStr = "http://" + getString(R.string.ip_address) + ":8080/ForestWebProject/departmentNoticeboard/content";
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

                ObjectOutputStream oos = new ObjectOutputStream(conn.getOutputStream());
                oos.writeObject(dataMap);
                oos.flush();
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { // 서버가 받았다면
                    Message msg = handler.obtainMessage();
                    Bundle b = new Bundle();
                    ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                    HashMap<String, String> departmentContentInfo = (HashMap<String, String>) ois.readObject();

                    msg.obj = departmentContentInfo;
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
            if (pd.isShowing()) pd.dismiss();
        }

        // AsyncTask.cancel(true) 호출시 실행되어 thread를 취소 합니다.
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    public class DownloadDepartmentInfoAsyncThread extends AsyncTask<String, String, String> {

        ProgressDialog pd = new ProgressDialog(DepartmentNoticeboardContentActivity.this);

        // Thread를 시작하기 전에 호출되는 함수
        protected void onPreExecute() {
            super.onPreExecute();

            pd.setTitle("정보 받아오는 중...");
            pd.setMessage("잠시만 기다려주세요. \n 파일을 다운로드 하고 있습니다.");
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
            dataMap.put("title", args[0]);
            dataMap.put("date", args[1]);
            dataMap.put("download", args[2]);
            urlStr = "http://" + getString(R.string.ip_address) + ":8080/ForestWebProject/departmentNoticeboard/download";
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

                ObjectOutputStream oos = new ObjectOutputStream(conn.getOutputStream());
                oos.writeObject(dataMap);
                oos.flush();
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { // 서버가 받았다면
                    Log.d("test", "Dd");
                    Message msg = handler.obtainMessage();
                    Bundle b = new Bundle();
                    ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                    ArrayList<byte[]> dataList = (ArrayList<byte[]>) ois.readObject();
                    if (dataList.size() == 0) {
                        b.putString("state", "fail");
                    } else {
                        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/glocalit/";
                        File dir = new File(dirPath);
                        // 디렉토리들이 있는지 확인
                        if (!dir.exists()) {
                            dir.mkdir();
                        }

                        Log.d("ttt", "여기까지는 됨");

                        FileOutputStream fileOutputStream = new FileOutputStream(new File(dirPath + args[2]));
                        for (int i = 0; i < dataList.size(); i++) {
                            Log.d("ttt", dataList.size() + "");
                            fileOutputStream.write(dataList.get(i));
                        }
                        fileOutputStream.close();
                        b.putString("state", "success");
                        b.putString("filePath", dirPath);
                        b.putString("fileName", args[2]);
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
            if (pd.isShowing()) pd.dismiss();
        }

        // AsyncTask.cancel(true) 호출시 실행되어 thread를 취소 합니다.
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}
