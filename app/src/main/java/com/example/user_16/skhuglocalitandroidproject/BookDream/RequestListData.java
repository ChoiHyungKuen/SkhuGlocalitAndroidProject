package com.example.user_16.skhuglocalitandroidproject.BookDream;

import java.text.Collator;
import java.util.Comparator;

/**
 * 요청 프래그먼트에서 사용되는 데이터를 관리한다.
 **/
public class RequestListData {
    public int mNo;          // 유니크넘버
    public String mTitle;       // 제목
    public String mDate;        // 날짜
    public String mUser;        // 사용자
    public String mSemester;    // 필요기간
    public String mContent;     // 작성 내용
    public static final Comparator<RequestListData> ALPHA_COMPARATOR = new Comparator<RequestListData>() {
        private final Collator sCollator = Collator.getInstance();

        @Override
        public int compare(RequestListData mListData1, RequestListData mListData2) {
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
