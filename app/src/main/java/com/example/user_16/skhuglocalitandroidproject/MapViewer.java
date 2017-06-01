package com.example.user_16.skhuglocalitandroidproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.user_16.skhuglocalitandroidproject.NaverMap.NMapPOIflagType;
import com.example.user_16.skhuglocalitandroidproject.NaverMap.NMapViewerResourceProvider;
import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapProjection;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.nmapmodel.NMapPlacemark;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;

import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by user-16 on 2017-04-12.
 */

public class MapViewer extends NMapActivity {

    private final static int ADD_POINT = 200;

    private final long FINSH_INTERVAL_TIME = 2000; // 2초안에 Back 버튼을 2번 누르면 앱 종료 -> 2초
    private long backPressedTime = 0;
    private SharedPreferences login_pref, map_pref, add_pref;
    private SharedPreferences.Editor editor;

    private NMapView mMapView = null;                   //네이버 맵 객체
    private NMapController mMapController = null;       //맵 컨트롤러
    private NMapViewerResourceProvider mMapViewerResourceProvider = null;
    private NMapOverlayManager mMapOverlayManager;
    private MapContainerView mMapContainerView;
    private int markerCount;
    private InitRecommendAsyncThread initRecommendAsyncThread;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        mMapView = new NMapView(this);

        mMapContainerView = new MapContainerView(this);
        mMapContainerView.addView(mMapView);

        // set the activity content to the parent view
        setContentView(mMapContainerView);
        String clientID = getString(R.string.CLIENT_ID);
        mMapView.setClientId(clientID);
        mMapView.setClickable(true);
        mMapView.setEnabled(true);
        mMapView.setFocusable(true);
        mMapView.setFocusableInTouchMode(true);
        mMapView.requestFocus();

        mMapController = mMapView.getMapController();
        mMapView.setOnMapStateChangeListener(onMapViewStateChangeListener);
        mMapView.setOnMapViewTouchEventListener(onMapViewTouchEventListener);
        mMapViewerResourceProvider = new NMapViewerResourceProvider(this);
        mMapOverlayManager = new NMapOverlayManager(this, mMapView, mMapViewerResourceProvider);
        super.setMapDataProviderListener(onDataProviderListener);

        initRecommend();
    }

    public class InitRecommendAsyncThread extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            URL url;
            HttpURLConnection conn;
            String urlStr;
            urlStr = "http://"+getString(R.string.ip_address)+":8080/SkhuGlocalitWebProject/recommend/InitRecommendList";
//            urlStr = "http://192.168.123.175:8080/ServerProject/Recommend/InitRecommendList";

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
                Log.d("응답메세지", "실행중5---" + conn.getResponseCode());
                if (conn.getResponseCode() == 200) { // 서버가 받았다면

                    ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                    HashMap<Integer, HashMap<String, String>> recommendListMap = new HashMap<>();
                    int index = 0;
                    boolean flag = ois.readBoolean();
                    while (flag) {
                        HashMap<String, String> locationData = (HashMap<String, String>) ois.readObject();
                        recommendListMap.put(index++, locationData);
                        flag = ois.readBoolean();
                    }
                    ois.close();
                    Message msg = handler.obtainMessage();
                    msg.what = ADD_POINT;
                    msg.obj = recommendListMap;
                    handler.sendMessage(msg);

                }

                conn.disconnect();
            } catch (Exception e) {
                Log.e("ERR", "LoadRecommendAsyncThread ERR : " + e);
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

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ADD_POINT:
                    HashMap<Integer, HashMap<String, String>> recommendListMap = (HashMap<Integer, HashMap<String, String>>) msg.obj;
                    String title, longitude, latitude;
                    for (int i = 0; i < recommendListMap.size(); i++) {
                        HashMap<String, String> locationData = getLocationData(recommendListMap, i);
                        title = locationData.get("title");
                        longitude = locationData.get("longitude");
                        latitude = locationData.get("latitude");
                        initRecommendPoint(title, Integer.parseInt(longitude), Integer.parseInt(latitude));
                    }
                    map_pref = getSharedPreferences("map_center", MODE_PRIVATE);
                    if (map_pref.getInt("longitude", 0) != 0 && map_pref.getInt("latitude", 0) != 0 && map_pref.getInt("zoomlevel", 0) != 0) {
                        mMapController.setMapCenter(new NGeoPoint(map_pref.getInt("longitude", 0), map_pref.getInt("latitude", 0)), map_pref.getInt("zoomlevel", 0));
                    } else {
                        mMapController.setMapCenter(new NGeoPoint(126.82575, 37.487444), 11);
                    }
                    break;
            }
        }
    };

    public void initRecommend(){
        initRecommendAsyncThread = new InitRecommendAsyncThread();
        initRecommendAsyncThread.execute();
    }
    public HashMap<String, String> getLocationData(HashMap<Integer, HashMap<String, String>> map, int index) {
        HashMap<String, String> locationData = map.get(index);
        return locationData;
    }


    private final NMapPOIdataOverlay.OnStateChangeListener onPOIdataStateChangeListener = new NMapPOIdataOverlay.OnStateChangeListener() {
        public void onCalloutClick(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {
            if(((RecommendFragmentActivity)getParent()).getMakable()){
                ((RecommendFragmentActivity)getParent()).setMakable(false);
                ((RecommendFragmentActivity)getParent()).setMaking(true);
                ((RecommendFragmentActivity)getParent()).changeAddFragment();
            }
            else {
                final DBManager dbManager = new DBManager(getApplicationContext(), "app_data.db", null, 1);
                HashMap<String, String> data = dbManager.getMemberInfo();
                String title = item.getTitle();
                int longitude = item.getPoint().getLongitudeE6();
                int latitude = item.getPoint().getLatitudeE6();
                ((RecommendFragmentActivity) getParent()).selectRecommend(title, Integer.toString(longitude), Integer.toString(latitude), data.get("id"));
            }
        }

        public void onFocusChanged(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {

            if (item != null) {
                Log.i("NMAP", "onFocusChanged: " + item.toString() + item.getTitle());
            } else {
                Log.i("NMAP", "onFocusChanged: ");
            }
        }
    };

    private final NMapView.OnMapStateChangeListener onMapViewStateChangeListener = new NMapView.OnMapStateChangeListener() {
        /**
         * 지도가 초기화된 후 호출된다.
         * 정상적으로 초기화되면 error 객체는 null이 전달되며,
         * 초기화 실패 시 error객체에 에러 원인이 전달된다
         */
        @Override
        public void onMapInitHandler(NMapView nMapView, NMapError nMapError) {

            if (nMapError == null) { //성공
                centerLoaction();
            } else { //실패
                Log.e("NMAP", "onMapInitHandler: 에러=" + nMapError.toString());
            }
        }

        void centerLoaction() {
            map_pref = getSharedPreferences("map_center", MODE_PRIVATE);
            if (map_pref.getInt("longitude", 0) != 0 && map_pref.getInt("latitude", 0) != 0 && map_pref.getInt("zoomlevel", 0) != 0) {
                mMapController.setMapCenter(new NGeoPoint(map_pref.getInt("longitude", 0), map_pref.getInt("latitude", 0)), map_pref.getInt("zoomlevel", 0));
                Log.d("로딩 되어야 할 위치", map_pref.getAll().toString());
            } else {
                mMapController.setMapCenter(new NGeoPoint(126.82575, 37.487444), 11);
            }
        }

        /**
         * 지도 중심 변경 시 호출되며 변경된 중심 좌표가 파라미터로 전달된다.
         */
        @Override
        public void onMapCenterChange(NMapView nMapView, NGeoPoint nGeoPoint) {
            map_pref = getSharedPreferences("map_center", MODE_PRIVATE);
            editor = map_pref.edit();
            editor.putInt("longitude", nGeoPoint.getLongitudeE6());
            Log.d("센터longitude", map_pref.getInt("longitude", 0) + "");
            editor.putInt("latitude", nGeoPoint.getLatitudeE6());
            Log.d("센터latitude", map_pref.getInt("latitude", 0) + "");
            editor.commit();
        }

        /**
         * 지도 레벨 변경 시 호출되며 변경된 지도 레벨이 파라미터로 전달된다.
         */
        @Override
        public void onZoomLevelChange(NMapView nMapView, int i) {
            map_pref = getSharedPreferences("map_center", MODE_PRIVATE);
            editor = map_pref.edit();
            editor.putInt("zoomlevel", i);
            Log.d("줌레벨", map_pref.getInt("zoomlevel", 0) + "");
            editor.commit();
        }

        /**
         * 지도 애니메이션 상태 변경 시 호출된다.
         * animType : ANIMATION_TYPE_PAN or ANIMATION_TYPE_ZOOM
         * animState : ANIMATION_STATE_STARTED or ANIMATION_STATE_FINISHED
         */
        @Override
        public void onAnimationStateChange(NMapView nMapView, int i, int i1) {
        }

        @Override
        public void onMapCenterChangeFine(NMapView nMapView) {

        }
    };
    
    void initRecommendPoint(String title, int x, int y) {
        NGeoPoint selectPoint = new NGeoPoint(x, y);
        int markerID = NMapPOIflagType.PIN;
        NMapPOIdata poiData = new NMapPOIdata(++markerCount, mMapViewerResourceProvider);
        poiData.beginPOIdata(1);
        poiData.addPOIitem(selectPoint, title, markerID, markerCount);
        poiData.endPOIdata();
        NMapPOIdataOverlay poiDataOverlay = mMapOverlayManager.createPOIdataOverlay(poiData, null);
        poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);

    }

    void addPoint(NMapView mapView, int x, int y) {
        NMapProjection mMapProjection = mapView.getMapProjection();
        NGeoPoint selectPoint = mMapProjection.fromPixels(x, y);
        int markerID = NMapPOIflagType.PIN;
        NMapPOIdata poiData = new NMapPOIdata(++markerCount, mMapViewerResourceProvider);
        markerCount = poiData.count();
        poiData.beginPOIdata(1);
        poiData.addPOIitem(selectPoint, RecommendAddFragment.title, markerID, markerCount);
        poiData.endPOIdata();
        findPlacemarkAtLocation(selectPoint.getLongitude(),selectPoint.getLatitude());
        add_pref = getSharedPreferences("recommend_Info", MODE_PRIVATE);
        editor = add_pref.edit();
        editor.putString("longitude", Integer.toString(selectPoint.getLongitudeE6()));
        editor.putString("latitude", Integer.toString(selectPoint.getLatitudeE6()));
        editor.commit();
        NMapPOIdataOverlay poiDataOverlay = mMapOverlayManager.createPOIdataOverlay(poiData, null);
        poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);

    }

    private NMapActivity.OnDataProviderListener onDataProviderListener = new OnDataProviderListener() {
        @Override
        public void onReverseGeocoderResponse(NMapPlacemark nMapPlacemark, NMapError nMapError) {
            if (nMapError != null){
                Toast.makeText(MapViewer.this,"주소 변환 에러",Toast.LENGTH_SHORT).show();
            } else {
                String doName = nMapPlacemark.doName.toString();
                String siName = nMapPlacemark.siName.toString();
                String dongName = nMapPlacemark.dongName.toString();
                String branch = null;
                if (!doName.contains("도")){
                    branch = doName.replace("광역시","").replace("특별시"," ");
                }
                if (branch == null){
                    branch = siName.replace("시","");
                }
                for(int i = 0 ; i < dongName.length(); i ++) {
                    if(48 <= dongName.charAt(i) && dongName.charAt(i) <= 57)
                        dongName = dongName.replace(dongName.charAt(i)+"","");
                }
                branch += dongName.replace("동","점");
                Toast.makeText(MapViewer.this,branch,Toast.LENGTH_SHORT).show();
                add_pref = getSharedPreferences("recommend_Info", MODE_PRIVATE);
                editor = add_pref.edit();
                editor.putString("branch",branch);
                editor.commit();
            }
        }
    };

    private final NMapView.OnMapViewTouchEventListener onMapViewTouchEventListener = new NMapView.OnMapViewTouchEventListener() {

        @Override
        public void onLongPress(NMapView mapView, MotionEvent ev) {
            // TODO Auto-generated method stub
            if (((RecommendFragmentActivity)getParent()).getMakable()) {
                int touch_x = (int) ev.getX();
                int touch_y = (int) ev.getY();
                addPoint(mapView, touch_x, touch_y);
            }
        }

        @Override
        public void onLongPressCanceled(NMapView mapView) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSingleTapUp(NMapView mapView, MotionEvent ev) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTouchDown(NMapView mapView, MotionEvent ev) {

        }

        @Override
        public void onScroll(NMapView mapView, MotionEvent e1, MotionEvent e2) {
        }

        @Override
        public void onTouchUp(NMapView mapView, MotionEvent ev) {
            // TODO Auto-generated method stub

        }

    };

    private class MapContainerView extends ViewGroup {

        public MapContainerView(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            final int width = getWidth();
            final int height = getHeight();
            final int count = getChildCount();
            for (int i = 0; i < count; i++) {
                final View view = getChildAt(i);
                final int childWidth = view.getMeasuredWidth();
                final int childHeight = view.getMeasuredHeight();
                final int childLeft = (width - childWidth) / 2;
                final int childTop = (height - childHeight) / 2;
                view.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
            }

            if (changed) {
                mMapOverlayManager.onSizeChanged(width, height);
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int w = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            int h = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
            int sizeSpecWidth = widthMeasureSpec;
            int sizeSpecHeight = heightMeasureSpec;

            final int count = getChildCount();
            for (int i = 0; i < count; i++) {
                final View view = getChildAt(i);

                if (view instanceof NMapView) {
                    if (mMapView.isAutoRotateEnabled()) {
                        int diag = (((int) (Math.sqrt(w * w + h * h)) + 1) / 2 * 2);
                        sizeSpecWidth = MeasureSpec.makeMeasureSpec(diag, MeasureSpec.EXACTLY);
                        sizeSpecHeight = sizeSpecWidth;
                    }
                }

                view.measure(sizeSpecWidth, sizeSpecHeight);
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
            Toast.makeText(MapViewer.this, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (initRecommendAsyncThread.getStatus() == AsyncTask.Status.RUNNING) {
                initRecommendAsyncThread.cancel(true);
            }
        } catch (Exception e) {

        }
    }
}
