package com.taro.recycle.ui.other;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.taro.headerrecycle.adapter.SimpleRecycleAdapter;
import com.taro.recycle.R;
import com.taro.recycle.ui.main.BubbleBoxLayout;
import com.taro.headerrecycle.adapter.HeaderRecycleAdapter;
import com.taro.headerrecycle.adapter.HeaderRecycleViewHolder;

import static com.taro.headerrecycle.adapter.HeaderRecycleAdapter.IAdjustCountOption.NO_USE_ADJUST_COUNT;

/**
 * Created by taro on 16/6/22.
 */
public class HeaderAdapterOption implements HeaderRecycleAdapter.IHeaderAdapterOption<String, String>, HeaderRecycleAdapter.IAdjustCountOption {
    private boolean mIsMultiType = false;
    private boolean mIsSetBgColor = false;
    public int mAdjustCount = NO_USE_ADJUST_COUNT;

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
                return R.layout.item_content_2;
            case 1:
                return R.layout.item_content;
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
    public void setHeaderHolder(int groupId, String header, HeaderRecycleViewHolder holder) {
        TextView tv_header = holder.getView(R.id.tv_header);
        if (tv_header != null) {
            tv_header.setText(header.toString());
        }

        if (mIsSetBgColor) {
            holder.getRootView().setBackgroundColor(Color.parseColor("#ff9900"));
        }
    }

    @Override
    public void setViewHolder(int groupId, int childId, int position, String itemData, HeaderRecycleViewHolder holder) {
        TextView tv_content = holder.getView(R.id.tv_content);
        if (tv_content != null) {
            tv_content.setText(itemData.toString());
        }

        if (holder.getItemViewType() == 1) {
            BubbleBoxLayout layout = (BubbleBoxLayout) holder.getRootView();
            layout.setIsDrawableTest(true);
            layout.setButtomText("小洪是SB,哇咔哫");
        }
        if (mIsSetBgColor) {
            holder.getRootView().setBackgroundColor(Color.parseColor("#99cc99"));
        }

        if (holder.getAdatper() instanceof SimpleRecycleAdapter) {
            mAdjustCount = 5;
        }
    }

    @Override
    public int getAdjustCount() {
        return mAdjustCount;
    }

    @Override
    public void setAdjustCount(int adjustCount) {
        mAdjustCount = adjustCount;
    }

    @Override
    public void onCreateViewEverytime(@NonNull View itemView, @NonNull ViewGroup parentView, @NonNull HeaderRecycleAdapter adapter, int viewType) {

    }
}