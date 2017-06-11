package com.example.user_16.skhuglocalitandroidproject;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by user-16 on 2017-04-12.
 */

public class RecommendFragmentActivity extends FragmentActivity {

    private final static int ADD_SUCCESS = 100;
    private final static int SELECT_VIEW = 101;
    private final static int EDIT_SUCCESS = 102;
    private final static int DELETE_SUCCESS = 103;
    private final static int SEARCH_SUCCESS = 104;

    private final long FINSH_INTERVAL_TIME = 2000; // 2초안에 Back 버튼을 2번 누르면 앱 종료 -> 2초
    private long backPressedTime = 0;
    private SharedPreferences login_pref, map_pref, add_pref;
    private SharedPreferences.Editor editor;

    private LinearLayout content_layout, edit_layout, review_layout;
    private TextView recommend_title, recommend_branch, recommend_search, recommend_id, recommend_name, recommend_category,
            recommend_callNumber, recommend_review, recommend_edit, recommend_save, recommend_delete;
    private ImageView recommend_searchImg;
    private Spinner recommend_category_sp;
    private int category_position;
    private CheckBox recommend_delivery, recommend_favorites;
    private EditText recommend_call_edit, recommend_edit_review;
    private Button recommend_up, recommend_down;
    private String recommend_longitude, recommend_latitude;
    private AlertDialog selectDialog, deleteDialog, searchDialog;
    private int up, down;
    private boolean up_flag, down_flag, recommend_flag, favorite_flag;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private Fragment list_fragment, map_fragment, add_fragment, search_fragment;

    private ListView recommendSearchListView = null;
    private ListViewAdapter listViewAdapter = null;
    private TextView search_condition;
    private String condition;

    private RecommendAddAsyncThread recommendAddAsyncThread;
    private SelectRecommendAsyncThread selectRecommendAsyncThread;
    private UpdateRecommendAsyncThread updateRecommendAsyncThread;
    private UpdownUpdateAsyncThread updownUpdateAsyncThread;
    private DeleteRecommendAsyncThread deleteRecommendAsyncThread;
    private SearchRecommendAsyncThread searchRecommendAsyncThread;

    private boolean markable = false;
    private boolean marking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recommend_fragments);

        list_fragment = new RecommendListFragment();
        map_fragment = new RecommendMapFragment();
        add_fragment = new RecommendAddFragment();
        search_fragment = new RecommendSearchFragment();

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_map, map_fragment);
        fragmentTransaction.replace(R.id.fragment_list, list_fragment);
        fragmentTransaction.commit();

    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case ADD_SUCCESS:
                    Toast.makeText(RecommendFragmentActivity.this, "추천 등록 완료", Toast.LENGTH_SHORT).show();
                    changeMapFragment();
                    break;
                case SELECT_VIEW:
                    HashMap<String, String> selectMap = (HashMap<String, String>) msg.obj;
                    selectRecommendView(selectMap);
                    break;
                case EDIT_SUCCESS:
                    HashMap<String, String> resultDataMap = (HashMap<String, String>) msg.obj;
                    if (resultDataMap.get("result").equals("success")) {
                        Toast.makeText(getParent(), "수정 완료", Toast.LENGTH_SHORT).show();
                        recommend_category.setText(resultDataMap.get("category"));
                        recommend_callNumber.setText(resultDataMap.get("callNumber"));
                        recommend_review.setText(resultDataMap.get("review"));
                        content_layout.setWeightSum(6);
                        recommend_category.setVisibility(View.VISIBLE);
                        recommend_callNumber.setVisibility(View.VISIBLE);
                        review_layout.setVisibility(View.VISIBLE);
                        recommend_edit.setVisibility(View.VISIBLE);
                        recommend_category_sp.setVisibility(View.GONE);
                        recommend_call_edit.setVisibility(View.GONE);
                        recommend_delivery.setEnabled(false);
                        recommend_edit_review.setVisibility(View.GONE);
                        recommend_save.setVisibility(View.GONE);
                    } else if (resultDataMap.get("result").equals("fail")) {
                        Toast.makeText(getParent(), "수정 실패", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case DELETE_SUCCESS:
                    if (msg.obj.equals("success")) {
                        changeAddFragment();
                        changeMapFragment();
                        if (selectDialog.isShowing()) {
                            selectDialog.dismiss();
                        }
                        Toast.makeText(getParent(), "삭제가 완료되었습니다", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getParent(), "삭제 실패", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case SEARCH_SUCCESS:
                    HashMap<Integer, HashMap<String, String>> searchMap = (HashMap<Integer, HashMap<String, String>>) msg.obj;
                    searchListView(searchMap);
                    break;
            }
        }
    };

    /**
     * 위치 정보 보기 기능 (다이얼로그)
     * @param selectMap
     */
    public void selectRecommendView(HashMap<String, String> selectMap) {
        LayoutInflater inflater = getParent().getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.recommend_select_view, null);

        SelectOnClickListener selectOnClickListener = new SelectOnClickListener();
        final DBManager dbManager = new DBManager(getParent(), "app_data.db", null, 1);
        HashMap<String, String> memberInfo = dbManager.getMemberInfo();
        final int favorites_count = dbManager.getCount();
        //뷰 아이디 매칭
        content_layout = (LinearLayout) alertLayout.findViewById(R.id.content_layout);
        edit_layout = (LinearLayout) alertLayout.findViewById(R.id.edit_layout);
        review_layout = (LinearLayout) alertLayout.findViewById(R.id.review_layout);
        recommend_searchImg = (ImageView) alertLayout.findViewById(R.id.recommend_searchImage);
        recommend_search = (TextView) alertLayout.findViewById(R.id.recommend_searchText);
        recommend_title = (TextView) alertLayout.findViewById(R.id.recommend_title);
        recommend_branch = (TextView) alertLayout.findViewById(R.id.recommend_branch);
        recommend_id = (TextView) alertLayout.findViewById(R.id.recommend_id);
        recommend_name = (TextView) alertLayout.findViewById(R.id.recommend_name);
        recommend_category = (TextView) alertLayout.findViewById(R.id.recommend_category);
        recommend_callNumber = (TextView) alertLayout.findViewById(R.id.recommend_callNumber);
        recommend_review = (TextView) alertLayout.findViewById(R.id.recommend_review);
        recommend_category_sp = (Spinner) alertLayout.findViewById(R.id.recommend_category_sp);
        recommend_call_edit = (EditText) alertLayout.findViewById(R.id.recommend_call_edit);
        recommend_delivery = (CheckBox) alertLayout.findViewById(R.id.recommend_delivery);
        recommend_edit_review = (EditText) alertLayout.findViewById(R.id.recommend_edit_review);
        recommend_favorites = (CheckBox) alertLayout.findViewById(R.id.recommend_favorites);
        recommend_up = (Button) alertLayout.findViewById(R.id.recommend_up);
        recommend_down = (Button) alertLayout.findViewById(R.id.recommend_down);
        recommend_edit = (TextView) alertLayout.findViewById(R.id.recommend_edit);
        recommend_save = (TextView) alertLayout.findViewById(R.id.recommend_save);
        recommend_delete = (TextView) alertLayout.findViewById(R.id.recommend_delete);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE)) {
                    String[] permission = {"android.permission.CALL_PHONE",};
                    getParent().requestPermissions(permission, 1000);
                }
            }
        }

        //내용 초기화
        String state = selectMap.get("state");
        if (state.equals("not")) {
            recommend_flag = true;
            up_flag = true;
            down_flag = true;
        } else if (state.equals("up")) {
            recommend_flag = false;
            up_flag = false;
            down_flag = true;
        } else if (state.equals("down")) {
            recommend_flag = false;
            up_flag = true;
            down_flag = false;
        }

        up = Integer.parseInt(selectMap.get("up"));
        down = Integer.parseInt(selectMap.get("down"));
        recommend_title.setText(selectMap.get("title"));
        recommend_branch.setText(selectMap.get("branch"));
        recommend_id.setText(selectMap.get("id"));
        recommend_name.setText(selectMap.get("name"));
        recommend_category.setText(selectMap.get("category"));
        recommend_callNumber.setText(selectMap.get("callNumber"));
        recommend_review.setText(selectMap.get("review"));
        recommend_up.setText(selectMap.get("up"));
        recommend_down.setText(selectMap.get("down"));
        setSelectedPoint(selectMap.get("longitude"), selectMap.get("latitude"));

        String[] subject = getResources().getStringArray(R.array.category_sp_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getParent(), android.R.layout.simple_spinner_dropdown_item, subject);
        recommend_category_sp.setAdapter(adapter);
        category_position = adapter.getPosition(recommend_category.getText().toString());
        if (selectMap.get("delivery").equals("true")) {
            recommend_delivery.setChecked(true);
        }
        recommend_edit_review.setText(selectMap.get("review"));
        if (memberInfo.get("id").equals(selectMap.get("id"))) {
            content_layout.setWeightSum(6);
            edit_layout.setVisibility(View.VISIBLE);
        }
        favorite_flag = dbManager.isFavorite(recommend_title.getText().toString(), recommend_longitude, recommend_latitude);
        recommend_favorites.setChecked(favorite_flag);

        recommend_searchImg.setOnClickListener(selectOnClickListener);
        recommend_search.setOnClickListener(selectOnClickListener);
        recommend_title.setOnClickListener(selectOnClickListener);
        recommend_callNumber.setOnClickListener(selectOnClickListener);
        recommend_up.setOnClickListener(selectOnClickListener);
        recommend_down.setOnClickListener(selectOnClickListener);
        recommend_edit.setOnClickListener(selectOnClickListener);
        recommend_save.setOnClickListener(selectOnClickListener);
        recommend_delete.setOnClickListener(selectOnClickListener);

        AlertDialog.Builder buider = new AlertDialog.Builder(RecommendFragmentActivity.this); //AlertDialog.Builder 객체 생성
        buider.setCancelable(false);
        buider.setView(alertLayout);

        buider.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final DBManager dbManager = new DBManager(getApplicationContext(), "app_data.db", null, 1);
                HashMap<String, String> data = dbManager.getMemberInfo();

                String id = data.get("id");
                String title = recommend_title.getText().toString();
                String branch = recommend_branch.getText().toString();
                String update_up = recommend_up.getText().toString();
                String update_down = recommend_down.getText().toString();
                String state = "not";
                if (!up_flag) {
                    state = "up";
                } else if (!down_flag) {
                    state = "down";
                }
                updownUpdate(title, recommend_longitude, recommend_latitude, id, state, update_up, update_down);
                //이전에 등록했다
                if (favorite_flag) {
                    int no = dbManager.getFavoriteNo(title, recommend_longitude, recommend_latitude);
                    //여전히 등록한다 - 업데이트
                    if (recommend_favorites.isChecked()){
                        dbManager.updateFavorite(no, update_up, update_down);
                    }
                    //추가를 취소한다 - 삭제
                    else {
                        dbManager.deleteFavorite(no);
                    }
                }
                //등록하지 않았었다
                else {
                    //이번에 새로 등록한다 - 추가
                    if (recommend_favorites.isChecked()){
                        dbManager.add_favorites(favorites_count, title, branch, recommend_longitude, recommend_latitude, update_up, update_down);
                    }
                }
                dialog.dismiss();
            }
        });
        buider.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        selectDialog = buider.create();
        selectDialog.setCanceledOnTouchOutside(false);//없어지지 않도록 설정
        if (!selectDialog.isShowing()) {
            selectDialog.show();
        }

    }

    public void setSelectedPoint(String longitude, String latitude) {
        this.recommend_longitude = longitude;
        this.recommend_latitude = latitude;
    }

    private class SelectOnClickListener implements View.OnClickListener {
        String category, title, callNumber, delivery, review;

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.recommend_title:
                    if (searchDialog != null) {
                        map_pref = getSharedPreferences("map_center", MODE_PRIVATE);
                        editor = map_pref.edit();
                        editor.putInt("longitude", Integer.parseInt(recommend_longitude));
                        editor.putInt("latitude", Integer.parseInt(recommend_latitude));
                        editor.putInt("zoomlevel", 14);
                        editor.commit();
                        selectDialog.dismiss();
                        searchDialog.dismiss();
                        changeMapFragment();
                    }
                    break;
                case R.id.recommend_callNumber:
                    if (!recommend_callNumber.getText().equals("미지정")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE)) {
                                String[] permission = {"android.permission.CALL_PHONE"};
                                getParent().requestPermissions(permission, 1000);
                            } else {
                                String callNumber = recommend_callNumber.getText().toString();
                                final String tel = "tel:" + callNumber;
                                AlertDialog.Builder builder = new AlertDialog.Builder(RecommendFragmentActivity.this);
                                builder.setTitle("전화 걸기")        // 제목 설정
                                        .setMessage(callNumber + " 에 전화연결 하시겠습니까?")
                                        .setCancelable(false)        // 뒤로 버튼 클릭시 취소 가능 설정
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            // 확인 버튼 클릭시 설정
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(tel));
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_DENIED){
                                                        startActivity(intent);
                                                    }
                                                }
                                            }
                                        })
                                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                            // 취소 버튼 클릭시 설정
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                dialog.dismiss();
                                            }
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.setCanceledOnTouchOutside(false);
                                dialog.show();
                            }
                        }
                    }
                    break;
                case R.id.recommend_searchText:
                case R.id.recommend_searchImage:
                    Intent intent = new Intent(RecommendFragmentActivity.this, SearchNaverActivity.class);
                    intent.putExtra("title", recommend_title.getText().toString());
                    intent.putExtra("branch", recommend_branch.getText().toString());
                    startActivity(intent);
                    break;
                case R.id.recommend_up:
                    if (recommend_flag) {
                        if (up_flag) {
                            up += 1;
                            recommend_up.setText(Integer.toString(up));
                            up_flag = false;
                            recommend_flag = false;
                        }
                    } else {
                        if (!up_flag) {
                            up -= 1;
                            recommend_up.setText(Integer.toString(up));
                            up_flag = true;
                            recommend_flag = true;
                        } else {
                            Toast.makeText(getParent(), "이미 비추천을 하셨습니다", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case R.id.recommend_down:
                    if (recommend_flag) {
                        if (down_flag) {
                            down += 1;
                            recommend_down.setText(Integer.toString(down));
                            down_flag = false;
                            recommend_flag = false;
                        }
                    } else {
                        if (!down_flag) {
                            down -= 1;
                            recommend_down.setText(Integer.toString(down));
                            down_flag = true;
                            recommend_flag = true;
                        } else {
                            Toast.makeText(getParent(), "이미 추천을 하셨습니다", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case R.id.recommend_edit:
                    content_layout.setWeightSum(5);
                    recommend_category.setVisibility(View.GONE);
                    recommend_callNumber.setVisibility(View.GONE);
                    review_layout.setVisibility(View.GONE);
                    recommend_edit.setVisibility(View.GONE);
                    recommend_category_sp.setVisibility(View.VISIBLE);
                    recommend_category_sp.setSelection(category_position);
                    recommend_call_edit.setVisibility(View.VISIBLE);
                    callNumber = recommend_callNumber.getText().toString();
                    if (!callNumber.equals("미지정")) {
                        recommend_call_edit.setText(callNumber);
                    }
                    recommend_delivery.setEnabled(true);
                    recommend_edit_review.setVisibility(View.VISIBLE);
                    recommend_save.setVisibility(View.VISIBLE);
                    break;

                case R.id.recommend_save:
                    category = recommend_category_sp.getSelectedItem().toString();
                    category_position = recommend_category_sp.getSelectedItemPosition();
                    title = recommend_title.getText().toString();
                    callNumber = recommend_call_edit.getText().toString();
                    if (callNumber.isEmpty() || callNumber.length() == 0) {
                        callNumber = "미지정";
                    }
                    if (recommend_delivery.isChecked()) {
                        delivery = "true";
                    } else {
                        delivery = "false";
                    }
                    review = recommend_edit_review.getText().toString();
                    if (review.isEmpty() || review.length() == 0) {
                        callNumber = "내용 없음";
                    }

                    updateRecommend(category, title, callNumber, delivery, review, recommend_longitude, recommend_latitude);
                    break;
                case R.id.recommend_delete:
                    title = recommend_title.getText().toString();

                    AlertDialog.Builder builder = new AlertDialog.Builder(RecommendFragmentActivity.this);
                    builder.setTitle("삭제 확인")        // 제목 설정
                            .setMessage(title + " 정보를 삭제 하시겠습니까?")
                            .setCancelable(false)        // 뒤로 버튼 클릭시 취소 가능 설정
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                // 확인 버튼 클릭시 설정
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    deleteRecommend(title, recommend_longitude, recommend_latitude);
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                // 취소 버튼 클릭시 설정
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.cancel();
                                }
                            });

                    deleteDialog = builder.create();    // 알림창 객체 생성
                    deleteDialog.show();    // 알림창 띄우기
                    break;

            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) { // 요청한 권한을 사용자가 "허용" 했다면...
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String callNumber = recommend_callNumber.getText().toString();
                final String tel = "tel:" + callNumber;
                AlertDialog.Builder builder = new AlertDialog.Builder(RecommendFragmentActivity.this);
                builder.setTitle("전화 걸기")        // 제목 설정
                        .setMessage(callNumber + " 에 전화연결 하시겠습니까?")
                        .setCancelable(false)        // 뒤로 버튼 클릭시 취소 가능 설정
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            // 확인 버튼 클릭시 설정
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(tel));
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_DENIED){
                                        startActivity(intent);
                                    }
                                }
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            // 취소 버튼 클릭시 설정
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            } else {
                Toast.makeText(RecommendFragmentActivity.this, "권한요청을 거부했습니다.", Toast.LENGTH_SHORT).show();
            }
        }

    }
    //-----------------------------------------------------------------------------------------------------------------------

    /**
     * 위치 검색 기능 (다이얼로그)
     * @param searchMap
     */
    public void searchListView(HashMap<Integer, HashMap<String, String>> searchMap) {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.recommend_search_view, null);

        search_condition = (TextView) alertLayout.findViewById(R.id.search_condition);
        search_condition.setText(condition);
        recommendSearchListView = (ListView) alertLayout.findViewById(R.id.search_list);

        listViewAdapter = new ListViewAdapter(RecommendFragmentActivity.this);
        recommendSearchListView.setAdapter(listViewAdapter);

        String title, branch, longitude, latitude, up, down;
        for (int i = 0; i < searchMap.size(); i++) {
            HashMap<String, String> searchOne = searchMap.get(i);
            title = searchOne.get("title");
            branch = searchOne.get("branch");
            longitude = searchOne.get("longitude");
            latitude = searchOne.get("latitude");
            up = searchOne.get("up");
            down = searchOne.get("down");
            listViewAdapter.addItem(title, branch, longitude, latitude, up, down);
        }

        recommendSearchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RecommendSearchListData searchList = (RecommendSearchListData) listViewAdapter.getItem(position);
                final DBManager dbManager = new DBManager(getApplicationContext(), "app_data.db", null, 1);
                HashMap<String, String> data = dbManager.getMemberInfo();
                String userId = data.get("id");
                String title = searchList.sTitle;
                String longitude = searchList.sLongitude;
                String latitude = searchList.sLatitude;
                selectRecommend(title, longitude, latitude, userId);
                dbManager.close();
            }
        });
        AlertDialog.Builder buider = new AlertDialog.Builder(RecommendFragmentActivity.this); //AlertDialog.Builder 객체 생성
        buider.setCancelable(false);
        buider.setView(alertLayout);
        buider.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });
        buider.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        searchDialog = buider.create();
        searchDialog.setCanceledOnTouchOutside(false);//없어지지 않도록 설정
        searchDialog.show();

    }
    //----------------------------------------------------------------------------------------------------------------------

    /**
     * 위치 추가 기능
     */
    public void addRecommend() {
        final DBManager dbManager = new DBManager(getApplicationContext(), "app_data.db", null, 1);
        HashMap<String, String> data = dbManager.getMemberInfo();
        String id = data.get("id");
        add_pref = getSharedPreferences("recommend_Info", MODE_PRIVATE);
        String category = add_pref.getString("category", "");
        String title = add_pref.getString("title", "");
        String branch = add_pref.getString("branch", "");
        String callNumber = add_pref.getString("callNumber", "");
        String delivery = add_pref.getString("delivery", "");
        String review = add_pref.getString("review", "");
        String longitude = add_pref.getString("longitude", "");
        String latitude = add_pref.getString("latitude", "");
        String up = "0";
        String down = "0";
        editor = add_pref.edit();
        editor.clear();
        editor.commit();
        recommendAddAsyncThread = new RecommendAddAsyncThread();
        recommendAddAsyncThread.execute(id, category, title, branch, callNumber, delivery, review, longitude, latitude, up, down);
    }
    public void setMakable(boolean m) { markable = m; }

    public boolean getMakable() {
        return markable;
    }

    public void setMaking(boolean m) {
        marking = m;
    }

    public boolean getMaking() {
        return marking;
    }
    //-----------------------------------------------------------------------------------------------------------------------


    /**
     * 백그라운드 스레드 실행
     */
    public void selectRecommend(String title, String longitude, String latitude, String id) {
        selectRecommendAsyncThread = new SelectRecommendAsyncThread();
        selectRecommendAsyncThread.execute(title, longitude, latitude, id);
    }

    public void deleteRecommend(String title, String longitude, String latitude) {
        deleteRecommendAsyncThread = new DeleteRecommendAsyncThread();
        deleteRecommendAsyncThread.execute(title, longitude, latitude);
    }

    public void updateRecommend(String category, String title, String callNumber, String delivery, String review, String longitude, String latitude) {
        updateRecommendAsyncThread = new UpdateRecommendAsyncThread();
        updateRecommendAsyncThread.execute(category, title, callNumber, delivery, review, longitude, latitude);
    }

    public void updownUpdate(String title, String longitude, String latitude, String id, String state, String up, String down) {
        updownUpdateAsyncThread = new UpdownUpdateAsyncThread();
        updownUpdateAsyncThread.execute(title, longitude, latitude, id, state, up, down);
    }

    public void searchRecommend(String[] conditionArray) {
        for (int i = 0; i < conditionArray.length; i++) {
            if (conditionArray[i] != null) {
                if (i == 0) {
                    condition = conditionArray[i];
                } else if (i == 1) {
                    condition += " / ";
                    condition += conditionArray[i];
                } else if (i == 2) {
                    condition += " / ";
                    if (conditionArray[2].equals("true")) {
                        condition += "배달 가능";
                    } else {
                        condition += "배달 불가능";
                    }
                }
            }
        }
        searchRecommendAsyncThread = new SearchRecommendAsyncThread();
        searchRecommendAsyncThread.execute(conditionArray[0], conditionArray[1], conditionArray[2]);
    }
    //-----------------------------------------------------------------------------------------------------------------------

     /**
     * 프래그먼트 이동 메소드
     */
    public void changeMapFragment() {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_map, map_fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.commit();
    }

    public void changeAddFragment() {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_map, add_fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.commit();
    }

    public void changeSearchFragment() {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_map, search_fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.commit();
    }
    //-------------------------------------------------------------------------------------------------------------------

    /**
     * 리스트 뷰 관련
     */
    private class ViewHolder {
        public TextView sTitle;
        public TextView sBranch;
        public TextView sUp;
        public TextView sDown;
    }

    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<RecommendSearchListData> searchListData = new ArrayList<>();

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        // 총 몇개의 리스트가 있는지 반환
        @Override
        public int getCount() {
            return searchListData.size();
        }

        // 사용자가 선택한 아이템을 반환
        @Override
        public Object getItem(int position) {
            return searchListData.get(position);
        }

        // ID(몇 번째 아이템인지) 반환
        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.recommend_search_item, null);

                holder.sTitle = (TextView) convertView.findViewById(R.id.search_title);
                holder.sBranch = (TextView) convertView.findViewById(R.id.search_branch);
                holder.sUp = (TextView) convertView.findViewById(R.id.search_up);
                holder.sDown = (TextView) convertView.findViewById(R.id.search_down);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            RecommendSearchListData searchData = searchListData.get(position); // DListData로부터 해당 아이템의 데이터를 받아온다.

            holder.sTitle.setText(searchData.sTitle);
            holder.sBranch.setText(searchData.sBranch);
            holder.sUp.setText(searchData.sUp);
            holder.sDown.setText(searchData.sDown);
            return convertView;
        }

        /*
            리스트에 아이템을 추가하는 메소드
        */
        public void addItem(String title, String branch, String longitude, String latitude, String up, String down) {
            RecommendSearchListData addInfo = new RecommendSearchListData();
            addInfo.sTitle = title;
            addInfo.sBranch = branch;
            addInfo.sLongitude = longitude;
            addInfo.sLatitude = latitude;
            addInfo.sUp = up;
            addInfo.sDown = down;
            searchListData.add(addInfo);
        }

        // 리스트를 새로고침 하는 메소드
        public void clear() {
            searchListData.clear();
        }

        // 데이터가 바뀌었음을 DB에 알려주는 메소드
        public void dataChange() {
            listViewAdapter.notifyDataSetChanged();
        }
    }

    public class RecommendAddAsyncThread extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            URL url;
            HttpURLConnection conn = null;
            String urlStr = "";

//            urlStr = "http://" + getString(R.string.ip_address) + ":8080/SkhuGlocalitWebProject/recommend/AddRecommend";
            urlStr = "http://192.168.123.175:8080/ServerProject/Recommend/AddRecommend";

            try {
                url = new URL(urlStr);
                Log.d("URL", "생성------" + urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                HashMap<String, String> addDataMap = new HashMap<String, String>();
                addDataMap.put("id", args[0]);
                addDataMap.put("category", args[1]);
                addDataMap.put("title", args[2]);
                addDataMap.put("branch", args[3]);
                addDataMap.put("callNumber", args[4]);
                addDataMap.put("delivery", args[5]);
                addDataMap.put("review", args[6]);
                addDataMap.put("longitude", args[7]);
                addDataMap.put("latitude", args[8]);
                addDataMap.put("up", args[9]);
                addDataMap.put("down", args[10]);
                ObjectOutputStream oos = new ObjectOutputStream(conn.getOutputStream());
                oos.writeObject(addDataMap);
                oos.flush();
                oos.close();
                Log.d("응답메세지", "실행중5---" + conn.getResponseCode());
                if (conn.getResponseCode() == 200) { // 서버가 받았다면
                    handler.sendEmptyMessage(ADD_SUCCESS);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("ERR", "AddMemberAsyncThread ERR : " + e);
            }
            return "";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    public class SelectRecommendAsyncThread extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            URL url;
            HttpURLConnection conn;
            String urlStr;

            urlStr = "http://" + getString(R.string.ip_address) + ":8080/SkhuGlocalitWebProject/recommend/SelectRecommend";
//            urlStr = "http://192.168.123.175:8080/ServerProject/Recommend/SelectRecommend";

            try {
                url = new URL(urlStr);
                Log.d("URL", "생성------" + urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                HashMap<String, String> selectDataMap = new HashMap<String, String>();
                selectDataMap.put("title", args[0]);
                selectDataMap.put("longitude", args[1]);
                selectDataMap.put("latitude", args[2]);
                selectDataMap.put("id", args[3]);
                ObjectOutputStream oos = new ObjectOutputStream(conn.getOutputStream());
                oos.writeObject(selectDataMap);
                oos.flush();
                oos.close();
                Log.d("응답메세지", "실행중5---" + conn.getResponseCode());
                if (conn.getResponseCode() == 200) { // 서버가 받았다면

                    ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                    HashMap<String, String> selectMap = (HashMap<String, String>) ois.readObject();
                    Message msg = handler.obtainMessage();
                    msg.obj = selectMap;
                    msg.what = SELECT_VIEW;
                    handler.sendMessage(msg);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("ERR", "SelectRecommendAsyncThread ERR : " + e);
            }
            return "";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    public class UpdateRecommendAsyncThread extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            URL url;
            HttpURLConnection conn;
            String urlStr;

            urlStr = "http://" + getString(R.string.ip_address) + ":8080/SkhuGlocalitWebProject/recommend/UpdateRecommend";
//            urlStr = "http://192.168.123.175:8080/ServerProject/Recommend/UpdateRecommend";

            try {
                url = new URL(urlStr);
                Log.d("URL", "생성------" + urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                HashMap<String, String> updateDataMap = new HashMap<>();
                updateDataMap.put("category", args[0]);
                updateDataMap.put("title", args[1]);
                updateDataMap.put("callNumber", args[2]);
                updateDataMap.put("delivery", args[3]);
                updateDataMap.put("review", args[4]);
                updateDataMap.put("longitude", args[5]);
                updateDataMap.put("latitude", args[6]);
                ObjectOutputStream oos = new ObjectOutputStream(conn.getOutputStream());
                oos.writeObject(updateDataMap);
                oos.flush();
                oos.close();
                Log.d("응답메세지", "실행중5---" + conn.getResponseCode());
                if (conn.getResponseCode() == 200) { // 서버가 받았다면
                    ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                    HashMap<String, String> resultData = (HashMap<String, String>) ois.readObject();
                    Message msg = handler.obtainMessage();
                    msg.obj = resultData;
                    msg.what = EDIT_SUCCESS;
                    handler.sendMessage(msg);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("ERR", "UpdateRecommendAsyncThread ERR : " + e);
            }
            return "";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    public class UpdownUpdateAsyncThread extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            URL url;
            HttpURLConnection conn;
            String urlStr;

            urlStr = "http://" + getString(R.string.ip_address) + ":8080/SkhuGlocalitWebProject/recommend/UpdownUpdate";
//            urlStr = "http://192.168.123.175:8080/ServerProject/Recommend/UpdownUpdate";

            try {
                url = new URL(urlStr);
                Log.d("URL", "생성------" + urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                HashMap<String, String> updateDataMap = new HashMap<>();
                updateDataMap.put("title", args[0]);
                updateDataMap.put("longitude", args[1]);
                updateDataMap.put("latitude", args[2]);
                updateDataMap.put("id", args[3]);
                updateDataMap.put("state", args[4]);
                updateDataMap.put("up", args[5]);
                updateDataMap.put("down", args[6]);

                ObjectOutputStream oos = new ObjectOutputStream(conn.getOutputStream());
                oos.writeObject(updateDataMap);
                oos.flush();
                oos.close();
                Log.d("응답메세지", "실행중5---" + conn.getResponseCode());
                conn.disconnect();
            } catch (Exception e) {
                Log.e("ERR", "UpdownRecommendAsyncThread ERR : " + e);
            }
            return "";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    public class DeleteRecommendAsyncThread extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            URL url;
            HttpURLConnection conn;
            String urlStr;

            urlStr = "http://" + getString(R.string.ip_address) + ":8080/SkhuGlocalitWebProject/recommend/DeleteRecommend";
//            urlStr = "http://192.168.123.175:8080/ServerProject/Recommend/DeleteRecommend";

            try {
                url = new URL(urlStr);
                Log.d("URL", "생성------" + urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                HashMap<String, String> updateDataMap = new HashMap<>();
                updateDataMap.put("title", args[0]);
                updateDataMap.put("longitude", args[1]);
                updateDataMap.put("latitude", args[2]);
                ObjectOutputStream oos = new ObjectOutputStream(conn.getOutputStream());
                oos.writeObject(updateDataMap);
                oos.flush();
                oos.close();
                Log.d("응답메세지", "실행중5---" + conn.getResponseCode());
                if (conn.getResponseCode() == 200) { // 서버가 받았다면
                    ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                    ArrayList<String> resultData = (ArrayList<String>) ois.readObject();
                    Message msg = handler.obtainMessage();
                    msg.obj = resultData.get(0);
                    msg.what = DELETE_SUCCESS;
                    handler.sendMessage(msg);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("ERR", "SelectRecommendAsyncThread ERR : " + e);
            }
            return "";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    public class SearchRecommendAsyncThread extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            URL url;
            HttpURLConnection conn;
            String urlStr;

            urlStr = "http://" + getString(R.string.ip_address) + ":8080/SkhuGlocalitWebProject/recommend/SearchRecommend";
//            urlStr = "http://192.168.123.175:8080/ServerProject/Recommend/SearchRecommend";

            try {
                url = new URL(urlStr);
                Log.d("URL", "생성------" + urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                HashMap<String, String> searchDataMap = new HashMap<String, String>();
                searchDataMap.put("category", args[0]);
                if (args[1] != null) {
                    searchDataMap.put("title", args[1]);
                }
                if (args[2] != null) {
                    searchDataMap.put("delivery", args[2]);
                }
                ObjectOutputStream oos = new ObjectOutputStream(conn.getOutputStream());
                oos.writeObject(searchDataMap);
                oos.flush();
                oos.close();
                Log.d("응답메세지", "실행중5---" + conn.getResponseCode());
                if (conn.getResponseCode() == 200) { // 서버가 받았다면
                    ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                    HashMap<Integer, HashMap<String, String>> searchMap = (HashMap<Integer, HashMap<String, String>>) ois.readObject();
                    Message msg = handler.obtainMessage();
                    msg.obj = searchMap;
                    msg.what = SEARCH_SUCCESS;
                    handler.sendMessage(msg);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("ERR", "SearchRecommendAsyncThread ERR : " + e);
            }
            return "";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (recommendAddAsyncThread.getStatus() == AsyncTask.Status.RUNNING) {
                recommendAddAsyncThread.cancel(true);
            }
            if (updateRecommendAsyncThread.getStatus() == AsyncTask.Status.RUNNING) {
                updateRecommendAsyncThread.cancel(true);
            }
            if (deleteRecommendAsyncThread.getStatus() == AsyncTask.Status.RUNNING) {
                deleteRecommendAsyncThread.cancel(true);
            }
            if (selectRecommendAsyncThread.getStatus() == AsyncTask.Status.RUNNING) {
                selectRecommendAsyncThread.cancel(true);
            }
            if (updownUpdateAsyncThread.getStatus() == AsyncTask.Status.RUNNING) {
                updownUpdateAsyncThread.cancel(true);
            }
            if (searchRecommendAsyncThread.getStatus() == AsyncTask.Status.RUNNING) {
                searchRecommendAsyncThread.cancel(true);
            }
        } catch (Exception e) {

        }
    }

    /*
     뒤로가기 버튼을 2초내로 2번 누를 시 Application 종료
  */
    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINSH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
            final DBManager dbManager = new DBManager(getApplicationContext(), "app_data.db", null, 1);
            HashMap<String, String> data = dbManager.getMemberInfo();
            login_pref = getSharedPreferences("login_Info", MODE_PRIVATE);
            if (login_pref.getString("id", "").equals("") && login_pref.getString("pw", "").equals("") && data.size() != 0)
                dbManager.deleteAll();
            map_pref = getSharedPreferences("map_center", MODE_PRIVATE);
            editor = map_pref.edit();
            editor.clear();
            editor.commit();

        } else {
            backPressedTime = tempTime;
            Toast.makeText(RecommendFragmentActivity.this, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
