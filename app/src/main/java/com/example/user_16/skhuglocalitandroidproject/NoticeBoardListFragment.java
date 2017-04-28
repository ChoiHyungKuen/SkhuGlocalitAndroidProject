
package com.example.user_16.skhuglocalitandroidproject;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import com.example.user_16.skhuglocalitandroidproject.BookDream.MainActivity;
import com.example.user_16.skhuglocalitandroidproject.FreeNoticeBoard.FreeNoticeBoard_Main;

import java.util.ArrayList;

/*
    게시판을 한 곳에 묶어 사용하려 합니다.
 */
public class NoticeBoardListFragment extends Fragment {

    private ListView noticeBoardListView = null;
    private ListViewAdapter listViewAdapter = null;
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.noticeboard_list_fragment, container, false);
        noticeBoardListView = (ListView) rootView.findViewById(R.id.noticeboard_listview);
        noticeBoardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                NoticeBoardListData data = (NoticeBoardListData)parent.getItemAtPosition(position);
                if(data.mTitle.equals("Book:Dream")) { //북드림을 눌렀을 경우
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    Log.d("인텐트!!",intent.toString());
                    getActivity().startActivity(intent);
                }
                //내가추가
                if(data.mTitle.equals("자유게시판")) { //자유게시판을 눌렀을 경우
                    Intent intent1 = new Intent(getContext(), FreeNoticeBoard_Main.class);
                    getActivity().startActivity(intent1);
                    Toast.makeText(getContext(), data.mTitle + "을 선택했습니다.", Toast.LENGTH_SHORT).show();
                }//내가추가

                else {
                    Toast.makeText(getContext(), data.mTitle + "을 선택했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        listViewAdapter = new ListViewAdapter(getContext());
        noticeBoardListView.setAdapter(listViewAdapter);
        listViewAdapter.addItem(ContextCompat.getDrawable(getContext(), R.mipmap.ic_launcher) ,"Book:Dream", "선,후배 간에 책을 주고 받을 수 있습니다.");
        listViewAdapter.addItem(ContextCompat.getDrawable(getContext(), R.mipmap.ic_launcher), "자유게시판", "자유롭게 소통하세요!");
        listViewAdapter.addItem(ContextCompat.getDrawable(getContext(), R.mipmap.ic_launcher), "정보게시판", "정보를 주고 받을 수 있습니다.");
        listViewAdapter.dataChange();
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
        public TextView mDescription;
    }

    /*
        요청 프래그먼트의 리스트뷰를 관리하는 메소드
     */
    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<NoticeBoardListData> mListData = new ArrayList<>();

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
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.noticeboard_list_item, null);

                holder.mIcon = (ImageView) convertView.findViewById(R.id.list_icon);
                holder.mTitle = (TextView) convertView.findViewById(R.id.list_title);
                holder.mDescription = (TextView) convertView.findViewById(R.id.list_description);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            NoticeBoardListData mData = mListData.get(position); // DListData로부터 해당 아이템의 데이터를 받아온다.

            if (mData.mIcon != null) {
                holder.mIcon.setVisibility(View.VISIBLE);
                holder.mIcon.setImageDrawable(mData.mIcon);
            } else {
                holder.mIcon.setVisibility(View.GONE);
            }
            holder.mTitle.setText(mData.mTitle);
            holder.mDescription.setText(mData.mDescription);
            return convertView;
        }

        /*
            리스트에 아이템을 추가하는 메소드
        */
        public void addItem(Drawable mIcon, String mTitle, String mDescription) {
            NoticeBoardListData addInfo = null;
            addInfo = new NoticeBoardListData();
            addInfo.mIcon = mIcon;
            addInfo.mTitle = mTitle;
            addInfo.mDescription = mDescription;
            mListData.add(addInfo);
        }

        // 리스트를 새로고침 하는 메소드
        public void clear(){
            mListData.clear();
        }

        // 데이터가 바뀌었음을 DB에 알려주는 메소드
        public void dataChange() {
            listViewAdapter.notifyDataSetChanged();
        }
    }

}
