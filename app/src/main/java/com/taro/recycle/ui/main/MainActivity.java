package com.taro.recycle.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.taro.recycle.R;

/**
 * Created by taro on 16/6/22.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button mBtnHeader;
    Button mBtnScroll;
    Button mBtnSticker;
    Button mBtnAdjust;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnHeader = (Button) findViewById(R.id.btn_main_header);
        mBtnScroll = (Button) findViewById(R.id.btn_main_scroll);
        mBtnSticker = (Button) findViewById(R.id.btn_main_sticker);
        mBtnAdjust = (Button) findViewById(R.id.btn_main_adjust);

        mBtnHeader.setOnClickListener(this);
        mBtnScroll.setOnClickListener(this);
        mBtnSticker.setOnClickListener(this);
        mBtnAdjust.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_main_header:
                Intent headerIntent = new Intent(this, HeaderActivity.class);
                this.startActivity(headerIntent);
                break;
            case R.id.btn_main_scroll:
                Intent scrollIntent = new Intent(this, ScrollActivity.class);
                this.startActivity(scrollIntent);
                break;
            case R.id.btn_main_sticker:
                Intent stickerIntent = new Intent(this, StickerActivity.class);
                this.startActivity(stickerIntent);
                break;
            case R.id.btn_main_adjust:
                Intent adjustIntent = new Intent(this, AdjustCountActivity.class);
                this.startActivity(adjustIntent);
                break;
        }
    }
}
