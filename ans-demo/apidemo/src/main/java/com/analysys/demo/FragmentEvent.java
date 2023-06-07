package com.analysys.demo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.analysys.apidemo.R;

import java.util.List;

/**
 * @Copyright © 2020 Analysys Inc. All rights reserved.
 * @Description:
 * @Create: 2020/7/21 5:22 PM
 * @author: huchangqing
 */
public class FragmentEvent extends Fragment {

    private String mEventName;

    public FragmentEvent(String eventName) {
        mEventName = eventName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView listView = view.findViewById(R.id.list_view);
        List<TrackEventObserver.EventData> list = TrackEventObserver.getInstance().getData(mEventName);
        MyAdapter adapter = new MyAdapter(getContext(), list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSelectedPosition(position);
            }
        });
    }

    public final class ViewHolder {
        public TextView name;
        public TextView time;
        public TextView detail;
    }

    public class MyAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private List<TrackEventObserver.EventData> mListData;
        private int mSelectedPosition;

        public MyAdapter(Context context, List<TrackEventObserver.EventData> list) {
            this.mInflater = LayoutInflater.from(context);
            mListData = list;
        }

        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.list_row_event, null);
                holder.name = convertView.findViewById(R.id.tv_name);
                holder.time = convertView.findViewById(R.id.tv_time);
                holder.detail = convertView.findViewById(R.id.tv_detail);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            TrackEventObserver.EventData data = mListData.get(position);
            holder.name.setText(data.eventName);
            holder.time.setText(data.time);
            if (position == mSelectedPosition) {
                holder.detail.setText(data.getDetail());
                holder.detail.setVisibility(View.VISIBLE);
            } else {
                holder.detail.setText("");
                holder.detail.setVisibility(View.GONE);
            }

            return convertView;
        }

        public void setSelectedPosition(int position) {
            if (mSelectedPosition == position) {
                mSelectedPosition = -1;
            } else {
                mSelectedPosition = position;
            }
            notifyDataSetChanged();
        }
    }
}
