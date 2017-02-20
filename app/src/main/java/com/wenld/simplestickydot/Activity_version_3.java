package com.wenld.simplestickydot;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;

import com.wenld.stickydot.DensityUtils;
import com.wenld.stickydot.StickyDotHepler;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by wenld on 2017/2/19.
 */

public class Activity_version_3 extends AppCompatActivity {
    public RecyclerView rlvAtyFilter;
    CommonAdapter adapter;
    List<ItemClass> list = new ArrayList<>();

    HashMap<Integer, StickyDotHepler> map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();

        this.rlvAtyFilter = (RecyclerView) findViewById(R.id.rlv_activity_main);
        TextView tv_activity_windows = (TextView) findViewById(R.id.tv_activity_windows);

        StickyDotHepler hepler = new StickyDotHepler(this, tv_activity_windows, R.layout.include_view)
                .setMaxDragDistance(DensityUtils.dip2px(Activity_version_3.this, 100))
                .setOutListener(new StickyDotHepler.StickyListener() {
                    @Override
                    public void outRangeUp(PointF dragCanterPoint) {
                        RecyclerView.LayoutManager layoutManager = rlvAtyFilter.getLayoutManager();
                        if (layoutManager instanceof LinearLayoutManager) {
                            LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                            int lastItemPosition = linearManager.findLastVisibleItemPosition();
                            int firstItemPosition = linearManager.findFirstVisibleItemPosition();
                            StickyDotHepler hepler;
                            for (int i = firstItemPosition; i <= lastItemPosition; i++) {
                                hepler = map.get(i);
                                if (hepler != null)
                                    hepler.dismiss();
                            }
                        }
                    }
                });


        adapter = new CommonAdapter<ItemClass>(this, R.layout.list_items, list) {
            @Override
            protected void convert(ViewHolder holder, final ItemClass s, final int position) {
                TextView btn = holder.getView(R.id.btn);
                btn.setText(s.name);
                TextView tv_activity_windows = holder.getView(R.id.tv_activity_windows);
                TextView mDragView = (TextView) LayoutInflater.from(mContext).inflate(R.layout.include_view, null, false);

                if (s.b) {
                    StickyDotHepler hepler = map.get(position);
                    if (hepler == null) {
                        hepler = new StickyDotHepler(Activity_version_3.this, tv_activity_windows, mDragView)
                                .setMaxDragDistance(DensityUtils.dip2px(Activity_version_3.this, 100))
                                .setOutListener(new StickyDotHepler.StickyListener() {
                                    @Override
                                    public void outRangeUp(PointF dragCanterPoint) {
                                        Toast.makeText(Activity_version_3.this, "第  " + position + " 个", Toast.LENGTH_LONG).show();
                                        map.remove(position);
                                    }
                                });
                        map.put(position, hepler);
                    }
                } else {
                    tv_activity_windows.setBackgroundColor(Color.parseColor("#94D5EE"));
                    mDragView.setBackgroundColor(Color.parseColor("#94D5EE"));
                    StickyDotHepler hepler = map.get(position);
                    if (hepler == null) {
                        hepler = new StickyDotHepler(Activity_version_3.this, tv_activity_windows, mDragView)
                                .setMaxDragDistance(DensityUtils.dip2px(Activity_version_3.this, 100))
                                .setColor(Color.parseColor("#94D5EE"))
                                .setOutListener(new StickyDotHepler.StickyListener() {
                                    @Override
                                    public void outRangeUp(PointF dragCanterPoint) {
                                        Toast.makeText(Activity_version_3.this, "第  " + position + " 个", Toast.LENGTH_LONG).show();
                                        map.remove(position);
                                    }
                                });
                        map.put(position, hepler);
                    }
                }

            }
        };
        rlvAtyFilter.setLayoutManager(new LinearLayoutManager(this));
        rlvAtyFilter.setAdapter(adapter);
    }

    private void initData() {
        list.add(new ItemClass(" xaioming_1", null));
        list.add(new ItemClass(" xaioming_2", null));
        list.add(new ItemClass(" xaioming_3", null));
        list.add(new ItemClass(" 接收并不提醒", null, false));
    }

    public class ItemClass {
        public String name;
        public Class className;
        public boolean b = true;

        public ItemClass(String name, Class className) {
            this.name = name;
            this.className = className;
        }

        public ItemClass(String name, Class className, boolean b) {
            this.name = name;
            this.className = className;
            this.b = b;
        }
    }
}
