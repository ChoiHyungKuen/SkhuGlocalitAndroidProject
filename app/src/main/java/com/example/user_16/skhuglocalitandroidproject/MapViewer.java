package com.example.user_16.skhuglocalitandroidproject;

import android.content.Context;
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

/**
 * Created by user-16 on 2017-04-12.
 */

public class MapViewer extends NMapActivity {

    static boolean MakePoint = false;
    private NMapView mMapView = null;                   //네이버 맵 객체
    private NMapController mMapController = null;       //맵 컨트롤러
    private NMapViewerResourceProvider mMapViewerResourceProvider = null;
    private NMapOverlayManager mMapOverlayManager;
    private MapContainerView mMapContainerView;

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
            Toast.makeText(MapViewer.this, item.getTitle(), Toast.LENGTH_SHORT).show();
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
                mMapController.setMapCenter(new NGeoPoint(126.82575, 37.487444), 11);
            } else { //실패
                Log.e("NMAP", "onMapInitHandler: 에러=" + nMapError.toString());
            }
        }

        /**
         * 지도 중심 변경 시 호출되며 변경된 중심 좌표가 파라미터로 전달된다.
         */
        @Override
        public void onMapCenterChange(NMapView nMapView, NGeoPoint nGeoPoint) {

        }

        /**
         * 지도 레벨 변경 시 호출되며 변경된 지도 레벨이 파라미터로 전달된다.
         */
        @Override
        public void onZoomLevelChange(NMapView nMapView, int i) {

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

    private final NMapView.OnMapViewTouchEventListener onMapViewTouchEventListener = new NMapView.OnMapViewTouchEventListener() {

        @Override
        public void onLongPress(NMapView mapView, MotionEvent ev) {
            // TODO Auto-generated method stub
            if (MakePoint) {
                int touch_x = (int) ev.getX();
                Log.d("터치X좌표", touch_x + "");
                int touch_y = (int) ev.getY();
                Log.d("터치Y좌표", touch_y + "");
                NMapProjection mMapProjection = mapView.getMapProjection();
                NGeoPoint selectPoint = mMapProjection.fromPixels(touch_x, touch_y);
                Log.d("지오포인트", selectPoint.getLatitude() + ":" + selectPoint.getLongitude());
                int markerID = NMapPOIflagType.PIN;
                NMapPOIdata poiData = new NMapPOIdata(0, mMapViewerResourceProvider);
                poiData.beginPOIdata(1);
                poiData.addPOIitem(selectPoint, "내가 선택한거", markerID, 0);

                poiData.endPOIdata();
                NMapPOIdataOverlay poiDataOverlay = mMapOverlayManager.createPOIdataOverlay(poiData, null);

                poiDataOverlay.showAllPOIdata(0);
                poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);

                MakePoint = false;
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
}
