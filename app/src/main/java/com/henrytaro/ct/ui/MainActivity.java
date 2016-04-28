package com.henrytaro.ct.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.henrytaro.ct.*;
import com.henrytaro.ct.other.GridHeaderRecycleAdapter;

import java.util.*;

/**
 * Created by taro on 16/4/19.
 */
public class MainActivity extends AppCompatActivity implements HeaderRecycleViewHolder.OnItemClickListener, SimpleRecycleViewHolder.OnItemClickListener {
    RecyclerView mRvDisplay = null;
    List<List> mGroupList = null;
    Map<Integer, String> mHeaderMap = new ArrayMap<Integer, String>();

    SimpleRecycleAdapter<String> mSimpleAdapter = null;
    HeaderRecycleAdapter mNormalAdapter = null;
    HeaderRecycleAdapter mColorAdapter = null;
    HeaderRecycleAdapter mMultiAdapter = null;
    GridHeaderRecycleAdapter mGridHeaderAdapter = null;
    StickHeaderItemDecoration mStickDecoration = null;

    LinearLayoutManager mLinearLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRvDisplay = (RecyclerView) findViewById(R.id.rv_test);

        mGroupList = new LinkedList<List>();
        mHeaderMap = new ArrayMap<Integer, String>();
        int groupId = 0;
        int count = 0;
        count = groupId + 10;
        for (; groupId < count; groupId++) {
            int childCount = 8;
            List<String> childList = new ArrayList<String>(childCount);
            for (int j = 0; j < childCount; j++) {
                childList.add("child - " + j);
            }
            mGroupList.add(childList);
            mHeaderMap.put(groupId, "title - " + groupId);
        }


        List<String> itemList = new ArrayList<String>();
        for (int i = 0; i < 5; i++) {
            itemList.add("single child - " + i);
        }
        //无头部普通adapter
        mSimpleAdapter = new SimpleRecycleAdapter<String>(this, new HeaderAdapterOption(false, false), itemList, this);
        //item单类型带头部adapter
        mNormalAdapter = new HeaderRecycleAdapter(this, new HeaderAdapterOption(false, false), mGroupList, mHeaderMap, this);
        //item多类型带头部adapter
        mMultiAdapter = new HeaderRecycleAdapter(this, new HeaderAdapterOption(true, false), mGroupList, mHeaderMap, this);
        //item单类型带颜色头部adapter
        mColorAdapter = new HeaderRecycleAdapter(this, new HeaderAdapterOption(false, true), mGroupList, mHeaderMap, this);
        //
        mGridHeaderAdapter = new GridHeaderRecycleAdapter(this, new HeaderAdapterOption(false, false), mGroupList, mHeaderMap, this);
        mGridHeaderAdapter.createHeaderGridLayoutManager(this, 3, GridLayoutManager.VERTICAL);
        //固定头部装饰
        mStickDecoration = new StickHeaderItemDecoration(mNormalAdapter);
        mLinearLayout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRvDisplay.setLayoutManager(mLinearLayout);
        mRvDisplay.setPadding(50, 50, 50, 50);
        mRvDisplay.setAdapter(mNormalAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_multi_item_type, menu);
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
                mRvDisplay.setAdapter(mNormalAdapter);
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
                mRvDisplay.setAdapter(mNormalAdapter);
                mNormalAdapter.setIsShowHeader(true);
                mRvDisplay.removeItemDecoration(mStickDecoration);
                mStickDecoration = new StickHeaderItemDecoration(mNormalAdapter);
                mRvDisplay.addItemDecoration(mStickDecoration);
                mNormalAdapter.notifyDataSetChanged();
                break;
            case R.id.action_stick_header_bg:
                //headerGridLayoutManger
                mRvDisplay.setAdapter(mColorAdapter);
                mRvDisplay.setLayoutManager(mLinearLayout);
                mRvDisplay.removeItemDecoration(mStickDecoration);
                mStickDecoration = new StickHeaderItemDecoration(mColorAdapter);
                mRvDisplay.addItemDecoration(mStickDecoration);
                mColorAdapter.notifyDataSetChanged();

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
    public void onItemClick(int groupId, int childId, int position, boolean isHeader, View rootView, HeaderRecycleViewHolder holder) {
//        RecyclerView.LayoutManager layoutManager = mRvDisplay.getLayoutManager();
//        if (layoutManager instanceof GridLayoutManager) {
//            ((GridLayoutManager) layoutManager).setSpanCount(2);
//            mRvDisplay.getAdapter().notifyDataSetChanged();
//        }
        Toast.makeText(this, "groud = " + groupId + "/child = " + childId + "/pos = " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClickListener(int position, View rootView, SimpleRecycleViewHolder holder) {
        Toast.makeText(this, "/pos = " + position, Toast.LENGTH_SHORT).show();
    }


    private class HeaderAdapterOption implements HeaderRecycleAdapter.IHeaderAdapterOption {
        private boolean mIsMultiType = false;
        private boolean mIsSetBgColor = false;

        public HeaderAdapterOption(boolean isMultiType, boolean isSetBgColor) {
            mIsMultiType = isMultiType;
            mIsSetBgColor = isSetBgColor;
        }

        @Override
        public int getHeaderViewType(int groupId, int position) {
            if (mIsMultiType) {
                if (groupId > 6) {
                    return -3;
                } else if (groupId > 3) {
                    return -1;
                } else {
                    return -2;
                }
            } else {
                return -1;
            }
        }

        @Override
        public int getItemViewType(int position, int groupId, int childId, boolean isHeaderItem, boolean isShowHeader) {
            if (isHeaderItem) {
                return getHeaderViewType(groupId, position);
            } else {
                if (mIsMultiType) {
                    if (childId > 3) {
                        return 0;
                    } else {
                        return 1;
                    }
                } else {
                    return 0;
                }
            }
        }

        @Override
        public int getLayoutId(int viewType) {
            switch (viewType) {
                case 0:
                case NO_HEADER_TYPE:
                    return R.layout.item_content;
                case 1:
                    return R.layout.item_content_2;
                case -1:
                    return R.layout.item_header;
                case -2:
                    return R.layout.item_header_2;
                case -3:
                    return R.layout.item_header_3;
                default:
                    return R.layout.item_content;
            }
        }

        @Override
        public void setHeaderHolder(int groupId, Object header, HeaderRecycleViewHolder holder) {
            TextView tv_header = holder.getView(R.id.tv_header);
            if (tv_header != null) {
                tv_header.setText(header.toString());
            }

            if (mIsSetBgColor) {
                holder.getRootView().setBackgroundColor(Color.parseColor("#ff9900"));
            }
        }

        @Override
        public void setViewHolder(int groupId, int childId, int position, Object itemData, HeaderRecycleViewHolder holder) {
            TextView tv_content = holder.getView(R.id.tv_content);
            tv_content.setText(itemData.toString());

            if (mIsSetBgColor) {
                holder.getRootView().setBackgroundColor(Color.parseColor("#99cc99"));
            }
        }
    }
}
