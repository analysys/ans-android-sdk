package com.analysys.demo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.analysys.apidemo.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisualSibRecyclerViewActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private List<Map<String, Object>> mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_sib_recycler_view);
        mData = getData();
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.addItemDecoration(new LinearItemDecoration(VisualSibRecyclerViewActivity.this, LinearLayoutManager.VERTICAL));
        initRecyclerView();
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

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        // 定义一个线性布局管理器
        LinearLayoutManager manager = new LinearLayoutManager(this);
        // 设置布局管理器
        mRecyclerView.setLayoutManager(manager);
        // 设置adapter
        DemoAdapter adapter = new DemoAdapter(VisualSibRecyclerViewActivity.this);
        mRecyclerView.setAdapter(adapter);
    }

    class DemoAdapter extends RecyclerView.Adapter {

        private Context mContext;

        public DemoAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(VisualSibRecyclerViewActivity.this).inflate(R.layout.list_row_visual, parent, false);
            //返回MyViewHolder的对象
            return new DemoViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            DemoViewHolder demoViewHolder = (DemoViewHolder) holder;
            demoViewHolder.viewBtn.setText((String) mData.get(position).get("btn"));
            demoViewHolder.img.setBackgroundResource((Integer) mData.get(position).get("img"));
            demoViewHolder.title.setText((String) mData.get(position).get("title"));
            demoViewHolder.info.setText((String) mData.get(position).get("info"));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position % 2;
        }

        private class DemoViewHolder extends RecyclerView.ViewHolder {

            public ImageView img;
            public TextView title;
            public TextView info;
            public Button viewBtn;

            public DemoViewHolder(View itemView) {
                super(itemView);
                itemView.setClickable(true);
                img = itemView.findViewById(R.id.img);
                title = itemView.findViewById(R.id.title);
                info = itemView.findViewById(R.id.info);
                viewBtn = itemView.findViewById(R.id.view_btn);
            }
        }
    }

    static class LinearItemDecoration extends RecyclerView.ItemDecoration {

        private static final int[] ATTRS = new int[]{android.R.attr.listDivider};

        public static final int ORIENTATION_HORIZONTAL = LinearLayoutManager.HORIZONTAL;
        public static final int ORIENTATION_VERTICAL = LinearLayoutManager.VERTICAL;

        private Drawable mDrawable;
        private int mOrientation;

        public LinearItemDecoration(Context context, int orientation) {
            final TypedArray typedArray = context.obtainStyledAttributes(ATTRS);
            mDrawable = typedArray.getDrawable(0);
            typedArray.recycle();
            setOrientation(orientation);
        }

        public void setOrientation(int orientation) {
            if (orientation != ORIENTATION_HORIZONTAL && orientation != ORIENTATION_VERTICAL) {
                this.mOrientation = ORIENTATION_VERTICAL;
            }
            this.mOrientation = orientation;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            if (mOrientation == ORIENTATION_HORIZONTAL) {
                drawHorizontal(c, parent);
            } else {
                drawVertical(c, parent);
            }
        }

        private void drawHorizontal(Canvas c, RecyclerView parent) {
            final int top = parent.getPaddingTop();
            final int bottom = parent.getHeight() - parent.getPaddingBottom();
            final int childCount = parent.getChildCount();

            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams layoutManager = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int left = child.getRight() + layoutManager.rightMargin;
                final int right = left + mDrawable.getIntrinsicHeight();
                mDrawable.setBounds(left, top, right, bottom);
                mDrawable.draw(c);
            }
        }

        private void drawVertical(Canvas c, RecyclerView parent) {
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();
            final int childCount = parent.getChildCount();

            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int top = child.getBottom() + layoutParams.bottomMargin;
                final int bottom = top + mDrawable.getIntrinsicHeight();
                mDrawable.setBounds(left, top, right, bottom);
                mDrawable.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (mOrientation == ORIENTATION_HORIZONTAL) {
                outRect.set(0, 0, mDrawable.getIntrinsicWidth(), 0);
            } else {
                outRect.set(0, 0, 0, mDrawable.getIntrinsicHeight());
            }
        }
    }
}
