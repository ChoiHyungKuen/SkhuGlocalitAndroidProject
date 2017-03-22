
package com.example.user_16.skhuglocalitandroidproject;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;


public class Activity_Join extends AppCompatActivity {

    TextInputLayout TextInputLayout01;
    TextInputLayout TextInputLayout02;
    AppCompatEditText EditText_id;
    AppCompatEditText EditText_pw;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        TextInputLayout01 = (TextInputLayout) findViewById(R.id.TextInputLayout01);
        TextInputLayout02 = (TextInputLayout) findViewById(R.id.TextInputLayout02);

        EditText_id = (AppCompatEditText) findViewById(R.id.EditText_id);
        EditText_pw = (AppCompatEditText) findViewById(R.id.EditText_pw);

        TextInputLayout01.setCounterEnabled(true);
        TextInputLayout02.setCounterEnabled(true);

    }
}
