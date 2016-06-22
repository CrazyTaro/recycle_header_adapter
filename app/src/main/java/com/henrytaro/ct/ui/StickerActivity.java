package com.henrytaro.ct.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.henrytaro.ct.R;
import com.henrytaro.ct.other.HeaderAdapterOption;
import com.taro.headerrecycle.adapter.HeaderRecycleAdapter;
import com.taro.headerrecycle.stickerheader.ErrorHeaderItemDecoration;
import com.taro.headerrecycle.stickerheader.StickHeaderItemDecoration;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by taro on 16/6/22.
 */
public class StickerActivity extends AppCompatActivity {
    RecyclerView mRvDisplay = null;

    List<List<String>> mGroupList = null;
    Map<Integer, String> mHeaderMap = new ArrayMap<Integer, String>();
    HeaderRecycleAdapter mNormalAdapter = null;
    HeaderRecycleAdapter mColorAdapter = null;

    StickHeaderItemDecoration mStickDecoration = null;
    ErrorHeaderItemDecoration mErrorDecoration = null;
    LinearLayoutManager mLinearLayout = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_header);
        mRvDisplay = (RecyclerView) findViewById(R.id.rv_test);

        mGroupList = new LinkedList<List<String>>();
        mHeaderMap = new ArrayMap<Integer, String>();
        int groupId = 0;
        int count = 0;
        count = groupId + 15;
        for (; groupId < count; groupId++) {
            int childCount = 8;
            List<String> childList = null;
            if (groupId > count / 2) {
                childList = new ArrayList<String>(childCount);
                for (int j = 0; j < childCount; j++) {
                    childList.add("child - " + j);
                }
            }
            mGroupList.add(childList);
            mHeaderMap.put(groupId, "title - " + groupId);
        }


        List<String> itemList = new ArrayList<String>();
        for (int i = 0; i < 5; i++) {
            itemList.add("single child - " + i);
        }

        //item单类型带头部adapter
        mNormalAdapter = new HeaderRecycleAdapter<String, String>(this, new HeaderAdapterOption(false, false), mGroupList, mHeaderMap);
        //item单类型带颜色头部adapter
        mColorAdapter = new HeaderRecycleAdapter<String, String>(this, new HeaderAdapterOption(false, true), mGroupList, mHeaderMap);
        //固定头部装饰
        mStickDecoration = new StickHeaderItemDecoration(mNormalAdapter);
        mErrorDecoration = new ErrorHeaderItemDecoration(mNormalAdapter);
        mLinearLayout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRvDisplay.setLayoutManager(mLinearLayout);
        mRvDisplay.setPadding(50, 50, 50, 50);
        mRvDisplay.setAdapter(mNormalAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sticker, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_no_stick_header:
                mRvDisplay.removeItemDecoration(mStickDecoration);
                mRvDisplay.removeItemDecoration(mErrorDecoration);
                mNormalAdapter.notifyDataSetChanged();
                break;
            case R.id.action_stick_header:
                mRvDisplay.setAdapter(mNormalAdapter);
                mRvDisplay.removeItemDecoration(mStickDecoration);
                mRvDisplay.removeItemDecoration(mErrorDecoration);
                mStickDecoration = new StickHeaderItemDecoration(mNormalAdapter);
                mRvDisplay.addItemDecoration(mStickDecoration);
                mNormalAdapter.notifyDataSetChanged();
                break;
            case R.id.action_stick_header_bg:
                //headerGridLayoutManger
                mRvDisplay.setAdapter(mColorAdapter);
                mRvDisplay.removeItemDecoration(mStickDecoration);
                mRvDisplay.removeItemDecoration(mErrorDecoration);
                mStickDecoration = new StickHeaderItemDecoration(mColorAdapter);
                mRvDisplay.addItemDecoration(mStickDecoration);
                mColorAdapter.notifyDataSetChanged();
                break;
            case R.id.action_error_stick_find_first_child_view:
                mRvDisplay.setAdapter(mNormalAdapter);
                mRvDisplay.removeItemDecoration(mStickDecoration);
                mRvDisplay.removeItemDecoration(mErrorDecoration);
                mErrorDecoration.setIsFindFirstVisibleChildView(true);
                mRvDisplay.addItemDecoration(mErrorDecoration);
                mNormalAdapter.notifyDataSetChanged();
                break;
            case R.id.action_error_stick_no_find_first_child_view:
                mRvDisplay.setAdapter(mNormalAdapter);
                mRvDisplay.removeItemDecoration(mStickDecoration);
                mRvDisplay.removeItemDecoration(mErrorDecoration);
                mErrorDecoration.setIsFindFirstVisibleChildView(false);
                mRvDisplay.addItemDecoration(mErrorDecoration);
                mNormalAdapter.notifyDataSetChanged();
                break;
            case R.id.action_error_stick_calculate_draw_rect:
                mRvDisplay.setAdapter(mNormalAdapter);
                mRvDisplay.removeItemDecoration(mStickDecoration);
                mRvDisplay.removeItemDecoration(mErrorDecoration);
                mErrorDecoration.setIsFindFirstVisibleChildView(true);
                mRvDisplay.addItemDecoration(mErrorDecoration);
                mNormalAdapter.notifyDataSetChanged();
                break;
            case R.id.action_error_stick_no_calculate_draw_rect:
                mRvDisplay.setAdapter(mNormalAdapter);
                mRvDisplay.removeItemDecoration(mStickDecoration);
                mRvDisplay.removeItemDecoration(mErrorDecoration);
                mErrorDecoration.setIsFindFirstVisibleChildView(true);
                mRvDisplay.addItemDecoration(mErrorDecoration);
                mNormalAdapter.notifyDataSetChanged();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
