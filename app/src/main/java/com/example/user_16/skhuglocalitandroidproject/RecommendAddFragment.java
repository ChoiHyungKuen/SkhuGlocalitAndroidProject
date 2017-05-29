package com.example.user_16.skhuglocalitandroidproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by user-16 on 2017-04-14.
 */

public class RecommendAddFragment extends Fragment {

    private SharedPreferences add_pref;
    private SharedPreferences.Editor editor;

    private Spinner category_sp;
    private TextInputLayout name_textlayout, call_textlayout;
    private EditText name_edittext, call_edittext, review_edittext;
    private CheckBox delivery_check;
    private TextView addLocation, addRecommend;
    public static String title;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.recommend_add_fragment, container, false);

        category_sp = (Spinner) rootView.findViewById(R.id.category_sp);
        name_textlayout = (TextInputLayout) rootView.findViewById(R.id.name_textlayout);
        call_textlayout = (TextInputLayout) rootView.findViewById(R.id.call_textlayout);
        name_edittext = (EditText) rootView.findViewById(R.id.name_edittext);
        call_edittext = (EditText) rootView.findViewById(R.id.call_edittext);
        review_edittext = (EditText) rootView.findViewById(R.id.review_edittext);
        delivery_check = (CheckBox) rootView.findViewById(R.id.delivery_check);
        addLocation = (TextView) rootView.findViewById(R.id.addLocation_btn);
        addLocation.setOnClickListener(addListener);
        addRecommend = (TextView) rootView.findViewById(R.id.addRecommend_btn);
        addRecommend.setOnClickListener(addListener);
        String[] subject = getResources().getStringArray(R.array.category_sp_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, subject);
        category_sp.setAdapter(adapter);
        return rootView;
    }

    private final View.OnClickListener addListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.addLocation_btn:
                    ((RecommendFragmentActivity)getActivity()).setMakable(true);
                    title = " 위치 설정 완료";
                    if (!name_edittext.getText().equals("")) {
                        title = name_edittext.getText().toString() + title;
                    }
                    ((RecommendFragmentActivity) getActivity()).changeMapFragment();
                    Toast.makeText(getActivity(), "길게 눌러 위치를 추가하세요\n추가한 후에 위치를 클릭해주세요", Toast.LENGTH_LONG).show();
                    break;
                case R.id.addRecommend_btn:
                    String category, name = "", callNumber, delivery, review;
                    boolean test = true;
                    if (delivery_check.isChecked()) {
                        delivery = "true";
                    } else {
                        delivery = "false";
                    }
                    category = category_sp.getSelectedItem().toString();
                    if (test) {
                        name = name_edittext.getText().toString();
                        if (!name.isEmpty() && name.length() > 0) {
                            name_textlayout.setErrorEnabled(false);
                        } else {
                            Toast.makeText(getActivity(), "이름을 입력해주세요", Toast.LENGTH_SHORT).show();
                            name_textlayout.setError("이름을 입력해주세요");
                            requestFocus(name_edittext);
                            test = false;
                        }
                    }
                    if (test) {
                        if (!((RecommendFragmentActivity)getActivity()).getMaking()) {
//                            Log.d("마킹 - 테스트",RecommendFragmentActivity.getMaking() + " - "+test);
                            test = false;
//                            Log.d("마킹 - 테스트",RecommendFragmentActivity.getMaking() + " - "+test);
                            Toast.makeText(getActivity(), "위치를 설정해주세요", Toast.LENGTH_SHORT).show();
                        }
                    }
                    callNumber = call_edittext.getText().toString();
                    if (callNumber.isEmpty() || callNumber.length() == 0) {
                        callNumber = "미지정";
                    }
                    review = review_edittext.getText().toString();
                    if (review.isEmpty() || review.length() == 0) {
                        review = "내용 없음";
                    }


                    if (test) {
                        add_pref = getActivity().getSharedPreferences("recommend_Info", Context.MODE_PRIVATE);
                        editor = add_pref.edit();

                        editor.putString("category", category);
                        editor.putString("title", name);
                        editor.putString("callNumber", callNumber);
                        editor.putString("delivery", delivery);
                        editor.putString("review", review);
                        editor.commit();
                        ((RecommendFragmentActivity) getActivity()).addRecommend();
                        ((RecommendFragmentActivity) getActivity()).changeMapFragment();
                        category_sp.setSelection(0);
                        name_edittext.setText("");
                        call_edittext.setText("");
                        delivery_check.setChecked(false);
                        review_edittext.setText("");
                        ((RecommendFragmentActivity)getActivity()).setMaking(false);
                    }
                    break;
            }
        }
    };

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }
}
