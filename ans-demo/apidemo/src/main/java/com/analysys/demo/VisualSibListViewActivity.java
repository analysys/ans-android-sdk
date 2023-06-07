package com.analysys.demo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.analysys.apidemo.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisualSibListViewActivity extends BaseActivity {
    private List<Map<String, Object>> mData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mData = getData();
        setContentView(R.layout.activity_visual_sib_list_view);
        ListView listView = findViewById(R.id.list_view);
        VisualSibListViewActivity.MyAdapter adapter = new VisualSibListViewActivity.MyAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
    }

    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("btn", "按钮 " + i);
            map.put("title", "title " + i);
            map.put("info", "detail " + i);
            map.put("img", R.drawable.beauty);
            list.add(map);
        }

        return list;
    }

    public final class ViewHolder {
        public ImageView img;
        public TextView title;
        public TextView info;
        public Button viewBtn;
    }

    public class MyAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public MyAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mData.size();
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
        public int getItemViewType(int position) {
            return position % 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            VisualSibListViewActivity.ViewHolder holder;
            if (convertView == null) {
                holder = new VisualSibListViewActivity.ViewHolder();
                convertView = mInflater.inflate(R.layout.list_row_visual, null);
                holder.img = convertView.findViewById(R.id.img);
                holder.title = convertView.findViewById(R.id.title);
                holder.info = convertView.findViewById(R.id.info);
                holder.viewBtn = convertView.findViewById(R.id.view_btn);
                convertView.setTag(holder);
            } else {
                holder = (VisualSibListViewActivity.ViewHolder) convertView.getTag();
            }

            holder.viewBtn.setText((String) mData.get(position).get("btn"));
            holder.img.setBackgroundResource((Integer) mData.get(position).get("img"));
            holder.title.setText((String) mData.get(position).get("title"));
            holder.info.setText((String) mData.get(position).get("info"));

            return convertView;
        }

    }
}
