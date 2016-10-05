package com.taro.recycle.ui.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.taro.headerrecycle.adapter.ExtraViewWrapAdapter;
import com.taro.headerrecycle.adapter.HeaderRecycleAdapter;
import com.taro.recycle.R;
import com.taro.recycle.ui.other.HeaderAdapterOption;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by taro on 16/9/8.
 */
public class AdjustCountActivity extends AppCompatActivity {
    RecyclerView mRvDisplay = null;

    List<List<String>> mGroupList = null;
    Map<Integer, String> mHeaderMap = new ArrayMap<Integer, String>();
    HeaderRecycleAdapter mNormalAdapter = null;
    HeaderAdapterOption mHeaderOption = null;

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
        mHeaderOption = new HeaderAdapterOption(false, false);
        mNormalAdapter = new HeaderRecycleAdapter<String, String>(this, mHeaderOption, mGroupList, mHeaderMap);
        mRvDisplay.setLayoutManager(new LinearLayoutManager(this));
        mRvDisplay.setAdapter(new ExtraViewWrapAdapter(mNormalAdapter));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_adjust, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_adjust_default:
                mHeaderOption.setAdjustCount(HeaderRecycleAdapter.IAdjustCountOption.NO_USE_ADJUST_COUNT);
                mRvDisplay.setAdapter(mNormalAdapter);
                break;
            case R.id.action_adjust_beyond:
                mHeaderOption.setAdjustCount(Integer.MAX_VALUE);
                mRvDisplay.setAdapter(mNormalAdapter);
                break;
            case R.id.action_adjust_zero:
                mHeaderOption.setAdjustCount(0);
                mRvDisplay.setAdapter(mNormalAdapter);
                break;
            case R.id.action_adjust_one_second:
                mHeaderOption.setAdjustCount(mNormalAdapter.getOriginalItemCount() / 2);
                mRvDisplay.setAdapter(mNormalAdapter);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
