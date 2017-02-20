package com.wenld.simplestickydot;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wenld.stickydot.DensityUtils;
import com.wenld.stickydot.StickyDotHepler;

/**
 * <p/>
 * Author: 温利东 on 2017/2/17 14:39.
 * blog: http://blog.csdn.net/sinat_15877283
 * github: https://github.com/LidongWen
 */

public class Activity_Version_2 extends AppCompatActivity {
    private TextView tv_activity_windows;
    private Button btn_close;

    StickyDotHepler hepler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stickydot_version_two);
        initView();
    }

    private void initView() {
        tv_activity_windows = (TextView) findViewById(R.id.tv_activity_windows);
        btn_close = (Button) findViewById(R.id.btn_close);
        TextView mDragView = (TextView) LayoutInflater.from(this).inflate(R.layout.include_view, null, false);
        hepler = new StickyDotHepler(this, tv_activity_windows, mDragView)
                .setMaxDragDistance(DensityUtils.dip2px(this, 200))
                .setColor(Color.parseColor("#94D5EE"))
//                .setDraged(false)  //设置是否可以被拖拽
                .setOutListener(new StickyDotHepler.StickyListener() {
                    @Override
                    public void outRangeUp(PointF dragCanterPoint) {
                        Toast.makeText(Activity_Version_2.this, "完成了", Toast.LENGTH_LONG).show();
                    }
                });

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hepler.dismiss();
            }
        });
    }

}
