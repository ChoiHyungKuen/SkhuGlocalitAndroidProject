package com.example.user_16.skhuglocalitandroidproject.BookDream;

import android.graphics.drawable.Drawable;

import java.text.Collator;
import java.util.Comparator;

public class GiveListData {
    public Drawable mIcon; // 아이콘
    public String mTitle;  // 제목
    public String mDate;   // 날짜
    public String mUnique; // 유니크 숫자
    public String mType;   // 타입
    public String mGrade;  // 등급
    public String mUser;  // 작성자 정보
    public static final Comparator<GiveListData> ALPHA_COMPARATOR = new Comparator<GiveListData>() {
        private final Collator sCollator = Collator.getInstance();

        @Override
        public int compare(GiveListData mListData1, GiveListData mListData2) {
            if(mListData1.mDate==null) {
                return (mListData2 == null) ? -1 : 0;
            } else if(mListData2.mDate==null) {
                return 1;
            } else {
                return sCollator.compare(mListData1.mDate, mListData2.mDate);
            }
        }
    };
}
