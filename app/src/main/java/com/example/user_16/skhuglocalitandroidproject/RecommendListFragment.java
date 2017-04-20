package com.example.user_16.skhuglocalitandroidproject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class RecommendListFragment extends Fragment {

    private ListView recommendListView = null;
    private RecommendListFragment.ListViewAdapter listViewAdapter = null;
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.recommend_list_fragment, container, false);
        Log.d("리스트뷰 프래그먼트","들어옴");

        recommendListView = (ListView) rootView.findViewById(R.id.recommend_listview);
        recommendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                RecommendListData data = (RecommendListData) parent.getItemAtPosition(position);
                switch (data.mTitle) {
                    case "지도 보기":
                        ((RecommendFragmentActivity)getActivity()).changeMapFragment();
                        break;
                    case "추천 등록":
                        ((RecommendFragmentActivity)getActivity()).changeAddFragment();
                        break;
                    case "추천 음식점 검색":
                        ((RecommendFragmentActivity)getActivity()).changeSearchFragment();
                        break;
                }
            }
        });
        listViewAdapter = new RecommendListFragment.ListViewAdapter(getContext());
        recommendListView.setAdapter(listViewAdapter);
        listViewAdapter.addItem(ContextCompat.getDrawable(getContext(), R.drawable.icon), getString(R.string.map_show));
        listViewAdapter.addItem(ContextCompat.getDrawable(getContext(), R.drawable.icon), getString(R.string.add_recommend));
        listViewAdapter.addItem(ContextCompat.getDrawable(getContext(), R.drawable.icon), getString(R.string.recommend_search));
        listViewAdapter.dataChange();
        Log.d("리스트뷰 프래그먼트","완료");
        return rootView;

    }


    /*
       ViewHolder란, 이름 그대로 뷰들을 홀더에 꼽아놓듯이 보관하는 객체를 말한다.
       각각의 Row를 그려낼 때 그 안의 위젯들의 속성을 변경하기 위해 findViewById를 호출하는데,
       이것의 비용이 큰것을 줄이기 위해 사용한다. 여기서 게시판의 정보들을 ViewHolder를 이용해 삽입한다.
     */
    private class ViewHolder {
        public ImageView mIcon;
        public TextView mTitle;
    }

    /*
        요청 프래그먼트의 리스트뷰를 관리하는 메소드
     */
    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<RecommendListData> mListData = new ArrayList<>();

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        // 총 몇개의 리스트가 있는지 반환
        @Override
        public int getCount() {
            return mListData.size();
        }

        // 사용자가 선택한 아이템을 반환
        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        // ID(몇 번째 아이템인지) 반환
        @Override
        public long getItemId(int position) {
            return position;
        }

        // 사용자가 선택한 아이템 데이터를 bookdream_request_item 형태에 맞춰 반환한다.
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RecommendListFragment.ViewHolder holder;
            if (convertView == null) {
                holder = new RecommendListFragment.ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.recommend_list_item, null);

                holder.mIcon = (ImageView) convertView.findViewById(R.id.recommend_list_icon);
                holder.mTitle = (TextView) convertView.findViewById(R.id.recommend_list_title);
                convertView.setTag(holder);
            } else {
                holder = (RecommendListFragment.ViewHolder) convertView.getTag();
            }

            RecommendListData mData = mListData.get(position); // DListData로부터 해당 아이템의 데이터를 받아온다.

            if (mData.mIcon != null) {
                holder.mIcon.setVisibility(View.VISIBLE);
                holder.mIcon.setImageDrawable(mData.mIcon);
            } else {
                holder.mIcon.setVisibility(View.GONE);
            }
            holder.mTitle.setText(mData.mTitle);
            return convertView;
        }

        /*
            리스트에 아이템을 추가하는 메소드
        */
        public void addItem(Drawable mIcon, String mTitle) {
            RecommendListData addInfo = null;
            addInfo = new RecommendListData();
            addInfo.mIcon = mIcon;
            addInfo.mTitle = mTitle;
            mListData.add(addInfo);
        }

        // 리스트를 새로고침 하는 메소드
        public void clear() {
            mListData.clear();
        }

        // 데이터가 바뀌었음을 DB에 알려주는 메소드
        public void dataChange() {
            listViewAdapter.notifyDataSetChanged();
        }
    }
}
