package com.henrytaro.ct.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.henrytaro.ct.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by taro on 16/4/19.
 */
public class MainActivity extends Activity implements HeaderRecycleViewHolder.OnItemClickListener, SimpleRecycleViewHolder.OnItemClickListener {
    RecyclerView rv = null;
    List<List> groupList = null;
    Map<Integer, String> headerMap = new ArrayMap<Integer, String>();

    SimpleRecycleAdapter<String> simpleAdapter = null;
    HeaderRecycleAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rv = (RecyclerView) findViewById(R.id.rv_test);

        groupList = new LinkedList<List>();
        headerMap = new ArrayMap<Integer, String>();
        int groupId = 0;
        int count = groupId + 8;
//        for (; groupId < count; groupId++) {
//            groupList.add(null);
//            headerMap.put(groupId, "title - " + groupId);
//        }
        count = groupId + 10;
        for (; groupId < count; groupId++) {
            int childCount = 8;
            List<String> childList = new ArrayList<String>(childCount);
            for (int j = 0; j < childCount; j++) {
                childList.add("child - " + j);
            }
            groupList.add(childList);
            headerMap.put(groupId, "title - " + groupId);
        }


        List<String> itemList = new ArrayList<String>();
        for (int i = 0; i < 5; i++) {
            itemList.add("single child - " + i);
        }
        simpleAdapter = new SimpleRecycleAdapter<String>(this, new HeaderAdapterOption(), itemList, this);
        adapter = new HeaderRecycleAdapter(this, new HeaderAdapterOption(), groupList, headerMap, this);
//        adapter.setHoldLayoutManager(adapter.createHeaderGridLayoutManager(this, 3, GridLayoutManager.VERTICAL));
        adapter.setHoldLayoutManager(new LinearLayoutManager(this));
        StickHeaderItemDecoration decoration = new StickHeaderItemDecoration(adapter);
        rv.setLayoutManager(adapter.getHoldLayoutManager());
        rv.addItemDecoration(decoration);
        rv.setPadding(50, 50, 50, 50);
        rv.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int groupId, int childId, int position, boolean isHeader, View rootView, HeaderRecycleViewHolder holder) {
        adapter.setSpanCount((GridLayoutManager) adapter.getHoldLayoutManager(), 2);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "groud = " + groupId + "/child = " + childId + "/pos = " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClickListener(int position, View rootView, SimpleRecycleViewHolder holder) {
        Toast.makeText(this, "/pos = " + position, Toast.LENGTH_SHORT).show();
    }


    private class HeaderAdapterOption implements HeaderRecycleAdapter.IHeaderAdapterOption {


        @Override
        public int getHeaderViewType(int groupId) {
            return -1;
        }

        @Override
        public int getItemViewType(int position, boolean isShowHeader) {
            return 0;
        }

        @Override
        public int getLayoutId(int viewType) {
            switch (viewType) {
                case 0:
                case NO_HEADER_TYPE:
                    return R.layout.item_content;
                case -1:
                    return R.layout.item_header;
                default:
                    return R.layout.item_content;
            }
        }

        @Override
        public void setHeaderHolder(int groupId, Object header, HeaderRecycleViewHolder holder) {
            TextView tv_header = holder.getView(R.id.tv_header);
            tv_header.setText(header.toString());
        }

        @Override
        public void setViewHolder(int groupId, int childId, int position, Object itemData, HeaderRecycleViewHolder holder) {
            TextView tv_content = holder.getView(R.id.tv_content);
            tv_content.setText(itemData.toString());
        }
    }
}
