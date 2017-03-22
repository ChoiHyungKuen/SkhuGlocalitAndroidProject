
package com.example.user_16.skhuglocalitandroidproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class Activity_Login extends AppCompatActivity {

    Button btn_login;
    TextView text_join;

    TextInputLayout TextInputLayout01, TextInputLayout02, TextInputLayout03, TextInputLayout04, TextInputLayout05;
    EditText EditText_id, EditText_pw, EditText_pwTest, EditText_name, EditText_mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_login = (Button) findViewById(R.id.btn_login);
        text_join = (TextView) findViewById(R.id.text_join);

    }

    public void onClick_Login(View view) {
        Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent1);
        finish();
    }

    public void onClick_Join(View view) {
        LayoutInflater inflater = Activity_Login.this.getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.activity_join, null);

        TextInputLayout01 = (TextInputLayout) alertLayout.findViewById(R.id.TextInputLayout01);
        TextInputLayout02 = (TextInputLayout) alertLayout.findViewById(R.id.TextInputLayout02);
        TextInputLayout03 = (TextInputLayout) alertLayout.findViewById(R.id.TextInputLayout03);
        TextInputLayout04 = (TextInputLayout) alertLayout.findViewById(R.id.TextInputLayout04);
        TextInputLayout05 = (TextInputLayout) alertLayout.findViewById(R.id.TextInputLayout05);

        EditText_id = (EditText) alertLayout.findViewById(R.id.EditText_id);
        EditText_pw = (EditText) alertLayout.findViewById(R.id.EditText_pw);
        EditText_pwTest = (EditText) alertLayout.findViewById(R.id.EditText_pwTest);
        EditText_name = (EditText) alertLayout.findViewById(R.id.EditText_name);
        EditText_mail = (EditText) alertLayout.findViewById(R.id.EditText_mail);

        AlertDialog.Builder buider = new AlertDialog.Builder(Activity_Login.this); //AlertDialog.Builder 객체 생성
        buider.setCancelable(false);
        buider.setView(alertLayout); //위에서 inflater가 만든 dialogView 객체 세팅 (Customize)

        buider.setPositiveButton("확인", null); // 회원 가입에 대한 리스너는 아래서 붙여준다.
        buider.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = buider.create();

        //Dialog의 바깥쪽을 터치했을 때 Dialog를 없앨지 설정
        dialog.setCanceledOnTouchOutside(false);//없어지지 않도록 설정

        //Dialog 보이기
        dialog.show();

        /*Intent intent2 = new Intent(getApplicationContext(), Activity_Join.class);
        startActivity(intent2);*/
    }
}
