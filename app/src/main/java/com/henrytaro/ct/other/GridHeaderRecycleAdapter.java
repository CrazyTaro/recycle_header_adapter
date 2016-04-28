package com.henrytaro.ct.other;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.henrytaro.ct.HeaderRecycleAdapter;
import com.henrytaro.ct.HeaderRecycleViewHolder;
import com.henrytaro.ct.HeaderSpanSizeLookup;

import java.util.List;
import java.util.Map;

/**
 * Created by taro on 16/4/28.
 */
public class GridHeaderRecycleAdapter extends HeaderRecycleAdapter {
    private HeaderSpanSizeLookup mLookup = null;
    private boolean mIsGridLayout = false;
    private RecyclerView.LayoutManager mLayoutManager = null;


    /**
     * 创建带header用于GridLayoutManager的Adapter
     *
     * @param context
     * @param option    操作配置接口
     * @param groupList 分组数据
     * @param headerMap 头部信息
     */
    public GridHeaderRecycleAdapter(Context context, IHeaderAdapterOption option, List<List> groupList, Map<Integer, ? extends Object> headerMap) {
        this(context, option, groupList, headerMap, null);
    }

    /**
     * 创建可以显示分组头部的recycleAdapter,其中Context与option不可为空
     *
     * @param context
     * @param option    分组头部需要的配置接口
     * @param groupList 分组数据
     * @param headerMap 分组头部匹配的Map
     * @param listener  每项单击的回调事件,此处也可以不进行设置,在绑定view的时候再设置onClick事件
     */
    public GridHeaderRecycleAdapter(Context context, IHeaderAdapterOption option, List<List> groupList, Map<Integer, ? extends Object> headerMap, HeaderRecycleViewHolder.OnItemClickListener listener) {
        super(context, option, groupList, headerMap, listener);
    }

    /**
     * 设置使用的LayoutManager,当且仅当需要使用分组的GridLayoutManager时才使用此方法,否则不建议使用.
     *
     * @param isGridLayout  是否使用GridLayoutManger
     * @param layoutManager 用于设置的GridLayoutManager
     * @return 返回参数layoutManager, 当layoutManager为null时, 将不会有任何设置
     */
    public RecyclerView.LayoutManager setUsingLayoutManager(boolean isGridLayout, RecyclerView.LayoutManager layoutManager) {
        GridLayoutManager gridLayoutManager = null;
        if (layoutManager instanceof GridLayoutManager) {
            gridLayoutManager = (GridLayoutManager) layoutManager;
            this.mIsGridLayout = isGridLayout && layoutManager != null;
            if (mIsGridLayout) {
                //创建或者设置分组头部占用空格的计算类
                if (mLookup == null) {
                    mLookup = new HeaderSpanSizeLookup(this, gridLayoutManager.getSpanCount());
                } else {
                    mLookup.setSpanCount(gridLayoutManager.getSpanCount());
                }
                gridLayoutManager.setSpanSizeLookup(mLookup);
            }
        }
        this.mLayoutManager = layoutManager;
        return layoutManager;
    }

    /**
     * 创建一个用于显示分组头部的GridLayoutManager
     *
     * @param context
     * @param spanCount   列数
     * @param orientation 方向,默认为竖向.{@link GridLayoutManager#VERTICAL}
     * @return
     */
    public GridLayoutManager createHeaderGridLayoutManager(Context context, int spanCount, int orientation) {
        GridLayoutManager layoutManager = new GridLayoutManager(context, spanCount, orientation, false);
        this.setUsingLayoutManager(true, layoutManager);
        return layoutManager;
    }

    @Override
    public void setGroupList(List<List> groupList) {
        super.setGroupList(groupList);
        setUsingLayoutManager(mIsGridLayout, this.getUsingLayoutManager());
    }

    @Override
    public void setIsShowHeader(boolean isShowHeader) {
        super.setIsShowHeader(isShowHeader);
        setUsingLayoutManager(mIsGridLayout, this.getUsingLayoutManager());
    }

    /**
     * 获取保存的LayoutManager
     *
     * @param <T> 此方法会将Manager自动转换成需要的类型,但是必须确保使用的类型是正确的,否则会造成转换异常
     * @return
     */
    public <T extends RecyclerView.LayoutManager> T getUsingLayoutManager() {
        return (T) this.mLayoutManager;
    }

    /**
     * 设置spanCount
     *
     * @param spanCount
     */
    public void setSpanCount(int spanCount) {
        setUsingLayoutManager(mIsGridLayout, this.getUsingLayoutManager());
    }
}
