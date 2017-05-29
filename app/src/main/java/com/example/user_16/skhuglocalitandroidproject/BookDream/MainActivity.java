package com.example.user_16.skhuglocalitandroidproject.BookDream;


import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.user_16.skhuglocalitandroidproject.R;

public class MainActivity extends AppCompatActivity {

        private final long	FINSH_INTERVAL_TIME = 2000; // 2초안에 Back 버튼을 2번 누르면 앱 종료 -> 2초
        private long backPressedTime = 0;

        private int[] imageResId = {
                R.drawable.request,
                R.drawable.give,
                R.drawable.info,
                R.drawable.settings
        };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_bookdream_main);

            // 각 4개의 탭 구성 - 각 프래그먼트를 구별하는 용도
            TabLayout tabLayout = (TabLayout) findViewById(R.id.bookdream_tab_layout);
            tabLayout.addTab(tabLayout.newTab().setText("요청").setIcon(imageResId[0]));
            tabLayout.addTab(tabLayout.newTab().setText("드림").setIcon(imageResId[1]));
            tabLayout.addTab(tabLayout.newTab().setText("정보").setIcon(imageResId[2]));
            tabLayout.addTab(tabLayout.newTab().setText("설정").setIcon(imageResId[3]));/*
            tabLayout.getBackground().setColorFilter(Color.parseColor("#3247B2"), PorterDuff.Mode.SRC);*/
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

            // 5개의 탭에 맞게 프래그먼트 구성
            final ViewPager viewPager = (ViewPager) findViewById(R.id.bookdream_pager);
            final TabPagerAdapter adapter = new TabPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
            viewPager.setAdapter(adapter);
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager.setCurrentItem(tab.getPosition());
                }
                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}
                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
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
            } else {
                backPressedTime = tempTime;
                Toast.makeText(getApplicationContext(), "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
            }
        }

}
