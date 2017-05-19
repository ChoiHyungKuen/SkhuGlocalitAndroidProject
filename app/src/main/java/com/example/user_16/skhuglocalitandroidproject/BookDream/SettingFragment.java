package com.example.user_16.skhuglocalitandroidproject.BookDream;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.user_16.skhuglocalitandroidproject.DBManager;
import com.example.user_16.skhuglocalitandroidproject.R;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class SettingFragment extends Fragment {
    private SharedPreferences ring_pref;
    private SharedPreferences.Editor editor;
    private ListView settingListview;
    private String[] settingList;

    private static final int REQUEST_CODE = 999;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.bookdream_setting_fragment, container, false);
        settingListview = (ListView)rootView.findViewById(R.id.setting_listView);
        settingList = getResources().getStringArray(R.array.setting_list);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, settingList);
        settingListview.setAdapter(adapter);

        // 사용자가 어떤 리스트 아이템을 선택하는지 확인하는 리스너
        settingListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (position == 0) {
                        setRingtone();
                    } else if(position==1){ // 두 번째 아이템를 선택한 경우, 만든이 정보를 띄워준다.
                        getInformation();
                    } else { // 세세번째 아이템를 선택한 경우, 문의 메일 보내기를 띄워준다.
                        getEmail();

                    }
                } catch(Exception e) {
                    Toast.makeText(getActivity(), "Error 발생", Toast.LENGTH_SHORT).show();
                    Log.e("ERR", "Setting  ERR : " + e.getMessage());
                }
            }
        });

        return rootView;
    }
    private  void setRingtone(){
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,"Select reingtone");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
        startActivityForResult(intent, REQUEST_CODE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        Uri pickedUri =
                intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

        if(pickedUri != null) {

            ring_pref = getContext().getSharedPreferences("init_Info",MODE_PRIVATE);
            editor = ring_pref.edit();
            editor.putString("uri", pickedUri.toString());
            editor.commit();
            Toast.makeText(getContext(), "path: " + pickedUri.toString(), Toast.LENGTH_LONG).show();
        }

    }
    /*
        사용자가 만든이 정보를 선택한 경우, 만든이 정보 다이얼로그를 띄워준다.
     */
    private void getInformation() {
        // Dialog에서 보여줄 입력화면 View 객체 생성 작업
        // Layout xml 리소스 파일을 View 객체로 부불려 주는(inflate) LayoutInflater 객체 생성
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // res폴더>>layout폴더>>layout_writing_custom_dialog.xml 레이아웃 리소스 파일로 View 객체 생성
        // Dialog의 listener에서 사용하기 위해 final로 참조변수 선언
        final View dialogView = inflater.inflate(R.layout.activity_made_by_dialog, null);

        AlertDialog.Builder buider = new AlertDialog.Builder(getContext()); //AlertDialog.Builder 객체 생성
        buider.setView(dialogView); //위에서 inflater가 만든 dialogView 객체 세팅 (Customize)
        buider.setNegativeButton("확인", new DialogInterface.OnClickListener() {
            //Dialog에 "확인"라는 타이틀의 버튼을 설정
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //설정한 값으로 AlertDialog 객체 생성
        AlertDialog dialog = buider.create();
        //Dialog의 바깥쪽을 터치했을 때 Dialog를 없앨지 설정
        dialog.setCanceledOnTouchOutside(false);//없어지지 않도록 설정
        //Dialog 보이기
        dialog.show();
    }

    /*
        사용자가 문의메일 보내기를 선택한 경우, 문의 내용을 적는 다이얼로그를 띄운다.
     */
    private void getEmail() {
        // Dialog에서 보여줄 입력화면 View 객체 생성 작업
        // Layout xml 리소스 파일을 View 객체로 부불려 주는(inflate) LayoutInflater 객체 생성
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // res폴더>>layout폴더>>layout_writing_custom_dialog.xml 레이아웃 리소스 파일로 View 객체 생성
        // Dialog의 listener에서 사용하기 위해 final로 참조변수 선언
        final View dialogView = inflater.inflate(R.layout.bookdream_sending_email_dialog, null);

        AlertDialog.Builder buider = new AlertDialog.Builder(getContext()); //AlertDialog.Builder 객체 생성
        buider.setView(dialogView); //위에서 inflater가 만든 dialogView 객체 세팅 (Customize)
        buider.setPositiveButton("보내기", new DialogInterface.OnClickListener() {
            //Dialog에 "확인"라는 타이틀의 버튼을 설정
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String contents = null;
                try {
                    EditText content = (EditText)dialogView.findViewById(R.id.mail_content);
                    contents = content.getText().toString();
                } catch(Exception e) {
                    Toast.makeText(getActivity(), "Error 발생", Toast.LENGTH_SHORT).show();
                    Log.e("ERR", "Sending Button  ERR : " + e.getMessage());
                }

                if (contents.length() > 0) { // 사용자가 원하는 문의 내용을 적은 경우,
                    try {
                        sendingEmail(contents); // 사용자가 원하는 앱을 선택하는 메소드로 연결한 후
                        dialog.dismiss();       // 이 다이얼로그를 종료시킨다.
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "Error 발생", Toast.LENGTH_SHORT).show();
                        Log.e("ERR", "Sending  ERR : " + e.getMessage());
                    }
                } else { // 사용자가 아무 내용을 적지 않는 경우 문의 내용을 다시 입력해달라는 메세지를 띄운다.
                    Toast.makeText(getActivity(), "문의 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buider.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //설정한 값으로 AlertDialog 객체 생성
        AlertDialog dialog = buider.create();
        //Dialog의 바깥쪽을 터치했을 때 Dialog를 없앨지 설정
        dialog.setCanceledOnTouchOutside(false);//없어지지 않도록 설정
        //Dialog 보이기
        dialog.show();
    }

    /*
        실제로 이메일을 보내는 메소드
        사용자가 보내기 버튼을 누른다면, 사용자가 지정된 이메일 앱에 연동되어 "받는이, 보내는이, 제목, 내용"이 자동 완성된다.
        (사용자의 이메일 아이디를 이용해 문의메일을 보낸다)
     */
    public void sendingEmail(String contents) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"gudrms1592@naver.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Book:DREAM 문의메일");
        intent.putExtra(Intent.EXTRA_TEXT, contents);
        intent.setType("message/rfc822");
        startActivity(Intent.createChooser(intent, "Select an Email Client:"));
    }
}
