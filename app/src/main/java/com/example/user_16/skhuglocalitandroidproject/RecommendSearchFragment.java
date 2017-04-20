package com.example.user_16.skhuglocalitandroidproject;

import android.os.Bundle;
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
import android.widget.Toast;

/**
 * Created by user-16 on 2017-04-15.
 */

public class RecommendSearchFragment extends Fragment {

    private Spinner category_sp;
    private TextView add_condition1, add_condition2, condition_reset, search;
    private LinearLayout condition_name, condition_delivery, condition_category;
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
                    Toast.makeText(getActivity(),"검색",Toast.LENGTH_SHORT).show();
            }
        }
    };

}
