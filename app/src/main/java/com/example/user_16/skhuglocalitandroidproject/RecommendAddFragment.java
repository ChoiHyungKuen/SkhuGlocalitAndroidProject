package com.example.user_16.skhuglocalitandroidproject;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by user-16 on 2017-04-14.
 */

public class RecommendAddFragment extends Fragment {

    private Spinner category_sp;
    private TextInputLayout name_textlayout, call_textlayout;
    private EditText name_edittext, call_edittext;
    private TextView addLocation, addRecommend;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.recommend_add_fragment,container,false);

        category_sp = (Spinner) rootView.findViewById(R.id.category_sp);
        name_textlayout = (TextInputLayout)rootView.findViewById(R.id.name_textlayout);
        call_textlayout = (TextInputLayout)rootView.findViewById(R.id.call_textlayout);
        name_edittext = (EditText)rootView.findViewById(R.id.name_edittext);
        call_edittext = (EditText)rootView.findViewById(R.id.call_edittext);
        addLocation = (TextView)rootView.findViewById(R.id.addLocation_btn);
        addLocation.setOnClickListener(addListener);
        addRecommend = (TextView)rootView.findViewById(R.id.addRecommend_btn);
        addRecommend.setOnClickListener(addListener);
        String[] subject = getResources().getStringArray(R.array.category_sp_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, subject);
        category_sp.setAdapter(adapter);
        return rootView;
    }

    private final View.OnClickListener addListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.addLocation_btn:
                    MapViewer.MakePoint = true;
                    ((RecommendFragmentActivity)getActivity()).changeMapFragment();
                    break;
                case R.id.addRecommend_btn:
                    Toast.makeText(getActivity(),"추천 등록",Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    };
}
