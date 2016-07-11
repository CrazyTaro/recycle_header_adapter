package com.taro.recycle.ui.main;

import com.taro.recycle.R;
import com.taro.headerrecycle.adapter.HeaderRecycleViewHolder;
import com.taro.headerrecycle.adapter.SimpleRecycleAdapter;

/**
 * Created by taro on 16/6/24.
 */
public class SimpleAdapterOption extends SimpleRecycleAdapter.SimpleAdapterOption<String> {
    @Override
    public int getViewType(int position) {
        return 0;
    }

    @Override
    public void setViewHolder(String itemData, int position, HeaderRecycleViewHolder holder) {
        //绑定数据
    }

    @Override
    public int getLayoutId(int viewType) {
        return R.layout.item_content;
    }
}
