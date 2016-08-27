package com.taro.recycle.ui.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.taro.headerrecycle.helper.RecycleViewScrollHelper;
import com.taro.recycle.R;
import com.taro.recycle.ui.other.HeaderAdapterOption;
import com.taro.headerrecycle.adapter.SimpleRecycleAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by taro on 16/6/22.
 */
public class ScrollActivity extends AppCompatActivity implements RecycleViewScrollHelper.OnScrollPositionChangedListener {
    RecyclerView mRvDisplay = null;
    FloatingActionButton mFAB = null;

    List<String> mLongDataSource = null;
    List<String> mShortDataSource = null;
    SimpleRecycleAdapter<String> mSimpleAdapter = null;
    LinearLayoutManager mLinearLayout = null;
    RecycleViewScrollHelper mScrollHelper = null;
    boolean mIsUsingLongDataSource = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll);
        mRvDisplay = (RecyclerView) findViewById(R.id.rv_test);
        mFAB = (FloatingActionButton) findViewById(R.id.fab_test);

        mScrollHelper = new RecycleViewScrollHelper(this);
        mScrollHelper.setCheckScrollToTopBottomTogether(false);
        mScrollHelper.setCheckScrollToTopFirstBottomAfter(false);
        mScrollHelper.setCheckIfItemViewFullRecycleViewForBottom(true);
        mScrollHelper.setCheckIfItemViewFullRecycleViewForTop(true);
        mScrollHelper.setTopOffsetFaultTolerance(100);
        mScrollHelper.setBottomFaultTolerance(100);
        mScrollHelper.attachToRecycleView(mRvDisplay);

        mLongDataSource = new ArrayList<String>(20);
        mShortDataSource = new ArrayList<String>(5);
        for (int i = 0; i < 20; i++) {
            mLongDataSource.add("long \n child - " + i);
        }
        for (int i = 0; i < 5; i++) {
            mShortDataSource.add("short \n child - " + i);
        }


        //无头部普通adapter
        mSimpleAdapter = new SimpleRecycleAdapter<String>(this, new HeaderAdapterOption(false, false), mLongDataSource);
        mLinearLayout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRvDisplay.setLayoutManager(mLinearLayout);
        mRvDisplay.setPadding(50, 50, 50, 50);
        mRvDisplay.setAdapter(mSimpleAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scroll, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_switch_data_source:
                if (mIsUsingLongDataSource) {
                    mSimpleAdapter.setItemList(mShortDataSource);
                    mSimpleAdapter.notifyDataSetChanged();
                    mIsUsingLongDataSource = false;
                } else {
                    mSimpleAdapter.setItemList(mLongDataSource);
                    mSimpleAdapter.notifyDataSetChanged();
                    mIsUsingLongDataSource = true;
                }
                break;
            case R.id.action_unchecked_full_recycle:
                mScrollHelper.setCheckIfItemViewFullRecycleViewForBottom(false);
                mScrollHelper.setCheckIfItemViewFullRecycleViewForTop(false);
                break;
            case R.id.action_checked_full_recycle:
                mScrollHelper.setCheckIfItemViewFullRecycleViewForBottom(true);
                mScrollHelper.setCheckIfItemViewFullRecycleViewForTop(true);
                break;
            case R.id.action_check_top_bottom_together:
                mScrollHelper.setCheckScrollToTopBottomTogether(true);
                break;
            case R.id.action_check_top_first:
                mScrollHelper.setCheckScrollToTopFirstBottomAfter(true);
                break;
            case R.id.action_check_bottom_first:
                mScrollHelper.setCheckScrollToTopFirstBottomAfter(false);
                break;
            case R.id.action_correct_state:
                mScrollHelper.setCheckScrollToTopBottomTogether(false);
                mScrollHelper.setCheckScrollToTopFirstBottomAfter(false);
                mScrollHelper.setCheckIfItemViewFullRecycleViewForBottom(true);
                mScrollHelper.setCheckIfItemViewFullRecycleViewForTop(true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onScrollToTop() {
        mFAB.hide();
        Toast.makeText(this, "滑动到顶部", Toast.LENGTH_SHORT).show();
        Log.i("scroll", "滑动到顶部");
    }

    @Override
    public void onScrollToBottom() {
        Toast.makeText(this, "滑动到底部", Toast.LENGTH_SHORT).show();
        Log.i("scroll", "滑动到底部");
    }

    @Override
    public void onScrollToUnknown(boolean isTopViewVisible, boolean isBottomViewVisible) {
        mFAB.show();
        Log.i("scroll", "滑动未达到底部或者顶部");
    }

}
