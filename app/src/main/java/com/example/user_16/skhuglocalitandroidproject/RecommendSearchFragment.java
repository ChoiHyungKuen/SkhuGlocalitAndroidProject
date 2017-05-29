package com.example.user_16.skhuglocalitandroidproject;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by user-16 on 2017-04-15.
 */

public class RecommendSearchFragment extends Fragment {

    private Spinner category_sp;
    private TextView add_condition1, add_condition2, condition_reset, search;
    private LinearLayout condition_name, condition_delivery, condition_category;
    private TextInputEditText name_edittext;
    private CheckBox delivery;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.recommend_search_fragment,container,false);

        category_sp = (Spinner) rootView.findViewById(R.id.category_sp);
        add_condition1 = (TextView) rootView.findViewById(R.id.add_condition_btn1);
        add_condition1.setOnClickListener(searchListener);
        add_condition2 = (TextView) rootView.findViewById(R.id.add_condition_btn2);
        add_condition2.setOnClickListener(searchListener);
        condition_reset = (TextView) rootView.findViewById(R.id.conditionreset_btn);
        condition_reset.setOnClickListener(searchListener);
        search = (TextView) rootView.findViewById(R.id.search_btn);
        search.setOnClickListener(searchListener);
        condition_category = (LinearLayout) rootView.findViewById(R.id.condition_category);
        condition_name = (LinearLayout) rootView.findViewById(R.id.condition_name);
        condition_delivery = (LinearLayout) rootView.findViewById(R.id.condition_delivery);
        delivery = (CheckBox) rootView.findViewById(R.id.delivery_check);
        name_edittext = (TextInputEditText) rootView.findViewById(R.id.name_edittext);

        String[] subject = getResources().getStringArray(R.array.category_sp_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, subject);
        category_sp.setAdapter(adapter);

        return rootView;
    }

    private final View.OnClickListener searchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Animation animation = new AlphaAnimation(0, 1);
            animation.setDuration(100);

            switch (v.getId()){
                case R.id.add_condition_btn1:
                    condition_name.setVisibility(View.VISIBLE);
                    condition_name.setAnimation(animation);
                    add_condition1.setVisibility(View.GONE);
                    condition_category.setWeightSum(4);
                    break;
                case R.id.add_condition_btn2:
                    condition_delivery.setVisibility(View.VISIBLE);
                    condition_delivery.setAnimation(animation);
                    add_condition2.setVisibility(View.GONE);
                    condition_name.setWeightSum(4);
                    break;
                case R.id.conditionreset_btn:
                    category_sp.setSelection(0);
                    name_edittext.setText("");
                    delivery.setChecked(false);
                    condition_category.setWeightSum(6);
                    condition_name.setWeightSum(6);
                    add_condition1.setVisibility(View.VISIBLE);
                    add_condition1.setAnimation(animation);
                    add_condition2.setVisibility(View.VISIBLE);
                    add_condition2.setAnimation(animation);
                    condition_name.setVisibility(View.GONE);
                    condition_delivery.setVisibility(View.GONE);
                    break;
                case R.id.search_btn:
                    String[] condition = new String[3];
                    condition[0] = category_sp.getSelectedItem().toString();
                    condition[1] = null;
                    if (condition_name.getVisibility()==View.VISIBLE){
                        if(name_edittext.getText().length()>0){
                            condition[1] = name_edittext.getText().toString();
                        }else {
                            condition[1] = null;
                        }
                    }
                    condition[2] = null;
                    if(condition_delivery.getVisibility()==View.VISIBLE){
                        if(delivery.isChecked()){
                            condition[2] = "true";
                        }else {
                            condition[2] = "false";
                        }
                    }
                    ((RecommendFragmentActivity)getActivity()).searchRecommend(condition);
                    category_sp.setSelection(0);
                    name_edittext.setText("");
                    delivery.setChecked(false);
            }
        }
    };

}
