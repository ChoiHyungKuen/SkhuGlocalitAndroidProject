
package com.example.user_16.skhuglocalitandroidproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;


public class TabFragment01 extends Fragment {

    private TextView btn_logout;
    private SharedPreferences login_pref, map_pref;
    SharedPreferences.Editor editor;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.tab_fragment01, container, false);
        final DBManager dbManager = new DBManager(getContext(), "app_data.db", null, 1);

        //로그아웃 기능
        btn_logout = (TextView) rootView.findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //DB헬퍼에 있는 정보와 login_Info 삭제
                login_pref = getContext().getSharedPreferences("login_Info", MODE_PRIVATE);
                Log.d("회원테이블",dbManager.getMemberInfo().toString());   //지우면 에러
                dbManager.deleteAll();
                //dbManager.delete(login_pref.getString("id",""));
                Log.d("test", login_pref.getString("id",""));
                editor = login_pref.edit();
                editor.clear();
                editor.commit();
                map_pref = getContext().getSharedPreferences("map_center",MODE_PRIVATE);
                editor = map_pref.edit();
                editor.clear();
                editor.commit();

                Toast.makeText(getActivity(), "로그아웃 되었습니다.\n 다시 로그인 해주세요.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getContext(), Activity_Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });
        return rootView;


    }
}
