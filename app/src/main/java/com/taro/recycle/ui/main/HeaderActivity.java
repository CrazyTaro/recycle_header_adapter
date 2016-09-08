package com.taro.recycle.ui.main;

import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.taro.recycle.R;
import com.taro.recycle.ui.other.GridHeaderRecycleAdapter;
import com.taro.recycle.ui.other.HeaderAdapterOption;
import com.taro.headerrecycle.adapter.ExtraViewWrapAdapter;
import com.taro.headerrecycle.layoutmanager.HeaderGridLayoutManager;
import com.taro.headerrecycle.adapter.HeaderRecycleAdapter;
import com.taro.headerrecycle.adapter.HeaderRecycleViewHolder;
import com.taro.headerrecycle.adapter.SimpleRecycleAdapter;
import com.taro.headerrecycle.stickerheader.StickHeaderItemDecoration;
import com.taro.headerrecycle.layoutmanager.HeaderSpanSizeLookup;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by taro on 16/4/19.
 */
public class HeaderActivity extends AppCompatActivity implements HeaderRecycleViewHolder.OnItemClickListener {
    RecyclerView mRvDisplay = null;

    List<List<String>> mGroupList = null;
    Map<Integer, String> mHeaderMap = new ArrayMap<Integer, String>();

    SimpleRecycleAdapter<String> mSimpleAdapter = null;
    HeaderRecycleAdapter mNormalAdapter = null;
    HeaderRecycleAdapter mMultiAdapter = null;
    GridHeaderRecycleAdapter mGridHeaderAdapter = null;
    StickHeaderItemDecoration mStickDecoration = null;
    ExtraViewWrapAdapter mExtraAdapter = null;

    LinearLayoutManager mLinearLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_header);
        mRvDisplay = (RecyclerView) findViewById(R.id.rv_test);

        mGroupList = new LinkedList<List<String>>();
        mHeaderMap = new ArrayMap<Integer, String>();
        int groupId = 0;
        int count = 0;
        count = groupId + 10;
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
        for (int i = 0; i < 10; i++) {
            itemList.add("single child - " + i);
        }


        //无头部普通adapter
        mSimpleAdapter = new SimpleRecycleAdapter<String>(this, new HeaderAdapterOption(false, false), itemList);
        //item单类型带头部adapter
        mNormalAdapter = new HeaderRecycleAdapter<String, String>(this, new HeaderAdapterOption(false, false), mGroupList, mHeaderMap);
        //item多类型带头部adapter
        mMultiAdapter = new HeaderRecycleAdapter<String, String>(this, new HeaderAdapterOption(true, false), mGroupList, mHeaderMap);


        //TODO:封装了gridLayoutManager的adapter,不推荐使用此类,使用 HeaderGridLayoutManager 代替
        mGridHeaderAdapter = new GridHeaderRecycleAdapter(this, new HeaderAdapterOption(false, false), mGroupList, mHeaderMap);
        mGridHeaderAdapter.createHeaderGridLayoutManager(this, 3, GridLayoutManager.VERTICAL);
        //固定头部装饰
        mStickDecoration = new StickHeaderItemDecoration(mNormalAdapter);
        mLinearLayout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRvDisplay.setLayoutManager(mLinearLayout);
        mRvDisplay.setPadding(50, 50, 50, 50);
        mRvDisplay.setAdapter(mNormalAdapter);

        mExtraAdapter = new ExtraViewWrapAdapter(mNormalAdapter);
        mExtraAdapter.addHeaderView(R.id.action_grid_layout, LayoutInflater.from(this).inflate(R.layout.item_extra, mRvDisplay, false));
        mExtraAdapter.addHeaderView(R.id.action_grid_layout_no_header, LayoutInflater.from(this).inflate(R.layout.item_extra, mRvDisplay, false));
        mExtraAdapter.addHeaderView(R.id.action_linear_horizontal, LayoutInflater.from(this).inflate(R.layout.item_extra, mRvDisplay, false));
        mExtraAdapter.addHeaderView(R.id.action_linear_layout, LayoutInflater.from(this).inflate(R.layout.item_extra, mRvDisplay, false));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_header, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_linear_horizontal:
                int orientation = mLinearLayout.getOrientation();
                if (orientation == LinearLayoutManager.HORIZONTAL) {
                    mLinearLayout.setOrientation(LinearLayoutManager.VERTICAL);
                } else {
                    mLinearLayout.setOrientation(LinearLayoutManager.HORIZONTAL);
                }
                break;
            case R.id.action_simple_type:
                mRvDisplay.setLayoutManager(mLinearLayout);
                mRvDisplay.setAdapter(mSimpleAdapter);
                mRvDisplay.removeItemDecoration(mStickDecoration);
                mSimpleAdapter.notifyDataSetChanged();
                break;
            case R.id.action_multi_item_type:
                mRvDisplay.setLayoutManager(mLinearLayout);
                mRvDisplay.setAdapter(mMultiAdapter);
                mRvDisplay.removeItemDecoration(mStickDecoration);
                mNormalAdapter.notifyDataSetChanged();
                break;
            case R.id.action_linear_layout:
                mRvDisplay.setLayoutManager(mLinearLayout);
                mRvDisplay.setAdapter(mExtraAdapter);
                mNormalAdapter.setIsShowHeader(true);
                mRvDisplay.removeItemDecoration(mStickDecoration);
                mNormalAdapter.notifyDataSetChanged();
                break;
            case R.id.action_grid_layout_no_header:
                //以下两种方式都可以达到gridLayoutManager分组的目的,但是更推荐第一种方式;
                //分组的根本在于使用了 重写了 SpanSizeLookup 类的抽象方法,如果要保持分组有效,请继承 HeaderSpanSizeLookup
                //headerGridLayoutManger
                mRvDisplay.setLayoutManager(new HeaderGridLayoutManager(this, 3, mNormalAdapter));
                mRvDisplay.setAdapter(mNormalAdapter);
                mNormalAdapter.setIsShowHeader(false);
                mRvDisplay.removeItemDecoration(mStickDecoration);
                mNormalAdapter.notifyDataSetChanged();

                //GridHeaderRecycleAdapter
//                mRvDisplay.setLayoutManager(mGridHeaderAdapter.getUsingLayoutManager());
//                mRvDisplay.setAdapter(mGridHeaderAdapter);
//                mGridHeaderAdapter.setIsShowHeader(false);
//                mRvDisplay.removeItemDecoration(mStickDecoration);
//                mGridHeaderAdapter.notifyDataSetChanged();
                break;
            case R.id.action_grid_layout:
                //headerGridLayoutManger
                mRvDisplay.setLayoutManager(new HeaderGridLayoutManager(this, 3, mNormalAdapter));
                mRvDisplay.setAdapter(mNormalAdapter);
                mNormalAdapter.setIsShowHeader(true);
                mRvDisplay.removeItemDecoration(mStickDecoration);
                mNormalAdapter.notifyDataSetChanged();


                //GridHeaderRecycleAdapter
//                mRvDisplay.setLayoutManager(mGridHeaderAdapter.getUsingLayoutManager());
//                mRvDisplay.setAdapter(mGridHeaderAdapter);
//                mGridHeaderAdapter.setIsShowHeader(true);
//                mRvDisplay.removeItemDecoration(mStickDecoration);
//                mGridHeaderAdapter.notifyDataSetChanged();
                break;
            case R.id.action_stick_header:
                RecyclerView.Adapter adapter = mRvDisplay.getAdapter();
                if (adapter == null || !(adapter instanceof StickHeaderItemDecoration.IStickerHeaderDecoration)) {
                    adapter = mExtraAdapter;
                }
                if (mRvDisplay.getLayoutManager() instanceof HeaderGridLayoutManager) {
                    if (!(adapter instanceof HeaderSpanSizeLookup.ISpanSizeHandler)) {
                        adapter = mExtraAdapter;
                    }
                    ((HeaderGridLayoutManager) mRvDisplay.getLayoutManager()).setISpanSizeHandler((HeaderSpanSizeLookup.ISpanSizeHandler) adapter);
                }
                mRvDisplay.setAdapter(adapter);
                mNormalAdapter.setIsShowHeader(true);
                mRvDisplay.removeItemDecoration(mStickDecoration);
                mStickDecoration = new StickHeaderItemDecoration((StickHeaderItemDecoration.IStickerHeaderDecoration) adapter);
                mRvDisplay.addItemDecoration(mStickDecoration);
                mNormalAdapter.notifyDataSetChanged();
                break;

            //GridHeaderRecycleAdapter
//                mGridHeaderAdapter.setHeaderAdapterOption(new HeaderAdapterOption(false, true));
//                mRvDisplay.setLayoutManager(mGridHeaderAdapter.getUsingLayoutManager());
//                mRvDisplay.setAdapter(mGridHeaderAdapter);
//                mGridHeaderAdapter.setIsShowHeader(true);
//                mRvDisplay.removeItemDecoration(mStickDecoration);
//                mStickDecoration = new StickHeaderItemDecoration(mColorAdapter);
//                mRvDisplay.addItemDecoration(mStickDecoration);
//                mGridHeaderAdapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(int groupId, int childId, int position, int viewId, boolean isHeader, View rootView, HeaderRecycleViewHolder holder) {
//        RecyclerView.LayoutManager layoutManager = mRvDisplay.getLayoutManager();
//        if (layoutManager instanceof GridLayoutManager) {
//            ((GridLayoutManager) layoutManager).setSpanCount(2);
//            mRvDisplay.getAdapter().notifyDataSetChanged();
//        }
        Toast.makeText(this, "groud = " + groupId + "/child = " + childId + "/pos = " + position, Toast.LENGTH_SHORT).show();
    }
}
