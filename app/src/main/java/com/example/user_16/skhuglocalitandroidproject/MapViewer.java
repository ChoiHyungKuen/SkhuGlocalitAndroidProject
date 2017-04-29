package com.example.user_16.skhuglocalitandroidproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;

import java.util.HashMap;

/**
 * Created by user-16 on 2017-04-12.
 */

public class MapViewer extends NMapActivity {

    private final long	FINSH_INTERVAL_TIME = 2000; // 2초안에 Back 버튼을 2번 누르면 앱 종료 -> 2초
    private long backPressedTime = 0;
    private SharedPreferences login_pref, map_pref;
    private SharedPreferences.Editor editor;

    private NMapView mMapView = null;                   //네이버 맵 객체
    private NMapController mMapController = null;       //맵 컨트롤러
    private NMapViewerResourceProvider mMapViewerResourceProvider = null;
    private NMapOverlayManager mMapOverlayManager;
    private MapContainerView mMapContainerView;
    private int markerCount;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        mMapView = new NMapView(this);

        mMapContainerView = new MapContainerView(this);
        mMapContainerView.addView(mMapView);

        // set the activity content to the parent view
        setContentView(mMapContainerView);

        mMapView.setClientId(getString(R.string.CLIENT_ID));
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


    }

    private final NMapPOIdataOverlay.OnStateChangeListener onPOIdataStateChangeListener = new NMapPOIdataOverlay.OnStateChangeListener() {
        public void onCalloutClick(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {
            RecommendFragmentActivity.setMakable(false);
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
                map_pref = getSharedPreferences("map_center",MODE_PRIVATE);
                if(map_pref.getInt("longitude",0)!=0 && map_pref.getInt("latitude",0)!=0 && map_pref.getInt("zoomlevel",0)!=0){
                    mMapController.setMapCenter(new NGeoPoint(map_pref.getInt("longitude",0), map_pref.getInt("latitude",0)), map_pref.getInt("zoomlevel",0));
                } else {
                    mMapController.setMapCenter(new NGeoPoint(126.82575, 37.487444), 11);
                }

            } else { //실패
                Log.e("NMAP", "onMapInitHandler: 에러=" + nMapError.toString());
            }
        }

        /**
         * 지도 중심 변경 시 호출되며 변경된 중심 좌표가 파라미터로 전달된다.
         */
        @Override
        public void onMapCenterChange(NMapView nMapView, NGeoPoint nGeoPoint) {
            map_pref = getSharedPreferences("map_center",MODE_PRIVATE);
            editor = map_pref.edit();
            editor.putInt("longitude",nGeoPoint.getLongitudeE6());
            Log.d("longitude",map_pref.getInt("longitude",0)+"");
            editor.putInt("latitude",nGeoPoint.getLatitudeE6());
            Log.d("latitude",map_pref.getInt("latitude",0)+"");
            editor.commit();
        }

        /**
         * 지도 레벨 변경 시 호출되며 변경된 지도 레벨이 파라미터로 전달된다.
         */
        @Override
        public void onZoomLevelChange(NMapView nMapView, int i) {
            map_pref = getSharedPreferences("map_center",MODE_PRIVATE);
            editor = map_pref.edit();
            editor.putInt("zoomlevel",i);
            Log.d("줌레벨",map_pref.getInt("zoomlevel",0)+"");
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

    void addPoint(NMapView mapView, int x, int y){
        markerCount = 0;
        NMapProjection mMapProjection = mapView.getMapProjection();
        NGeoPoint selectPoint = mMapProjection.fromPixels(x, y);
        Log.d("지오포인트", selectPoint+"");
        int markerID = NMapPOIflagType.PIN;
        Log.d("마커아이디",markerID+"");
        NMapPOIdata poiData = new NMapPOIdata(++markerCount, mMapViewerResourceProvider);
        poiData.beginPOIdata(1);
        poiData.addPOIitem(selectPoint, "<< 위치 설정 완료 >>", markerID, markerCount);
        poiData.endPOIdata();
        NMapPOIdataOverlay poiDataOverlay = mMapOverlayManager.createPOIdataOverlay(poiData, null);
        poiDataOverlay.showAllPOIdata(0);
        poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);

    }


    private final NMapView.OnMapViewTouchEventListener onMapViewTouchEventListener = new NMapView.OnMapViewTouchEventListener() {

        @Override
        public void onLongPress(NMapView mapView, MotionEvent ev) {
            // TODO Auto-generated method stub
            if (RecommendFragmentActivity.getMakable()) {
                int touch_x = (int) ev.getX();
                int touch_y = (int) ev.getY();
                addPoint(mapView,touch_x,touch_y);
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

        if ( 0 <= intervalTime && FINSH_INTERVAL_TIME >= intervalTime ) {
            super.onBackPressed();
            final DBManager dbManager = new DBManager(getApplicationContext(), "app_data.db", null, 1);
            HashMap<String, String> data = dbManager.getMemberInfo();
            login_pref = getSharedPreferences("login_Info",MODE_PRIVATE);
            if(login_pref.getString("id","").equals("") && login_pref.getString("pw","").equals("") && data.size()!=0)
                dbManager.deleteAll();
            map_pref = getSharedPreferences("map_center",MODE_PRIVATE);
            editor = map_pref.edit();
            editor.clear();
            editor.commit();

        } else {
            backPressedTime = tempTime;
            Toast.makeText(MapViewer.this, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
