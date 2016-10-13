package com.taro.headerrecycle.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HeaderViewListAdapter;

/**
 * 调整界面显示的item数接口
 */
public interface IAdjustCountOption {
    /**
     * 无效的item长度,使用此值时不会改变原来的item长度
     */
    public static final int NO_USE_ADJUST_COUNT = -1;

    /**
     * 设置调整后需要显示的itemCount.
     *
     * @param adjustCount count可以是任意值,但是仅有在0-数据量的范围内有效;<br>
     *                    当count>数据量时,多出的数据根本不可能找到填充的对象,将使用原数据量; 当count是负数时不可能有任何的作用,将使用原数据量.;
     *                    默认值为负数{@link #NO_USE_ADJUST_COUNT}
     */
    public void setAdjustCount(int adjustCount);

    /**
     * 返回调整后需要显示的itemCount.
     *
     * @return 返回的count必须小于原始添加数据的量(否则多出的数据根本不可能找到填充的对象), 同时也必须大于1才有效.返回负数无效, 将使用原数据量.
     * 默认值为负数{@link #NO_USE_ADJUST_COUNT}
     */
    public int getAdjustCount();

    /**
     * 每一次当itemView被创建的时候此方法会被回调,根据需要选择是否在这个地方计算并设置需要调整的itemCount
     *
     * @param itemView   childView
     * @param parentView 此处为RecycleView
     * @param adapter    适配器
     * @param viewType
     */
    public void onCreateViewEverytime(@NonNull View itemView, @NonNull ViewGroup parentView, @NonNull HeaderRecycleAdapter adapter, int viewType);

//    /**
//     * 建议通知再次重置adapter或者通知recycleView数据界面需要更新;<br>
//     * 这是在比较特别的情况下会发生的回调;当数据量大于1时,设置当前调整为1后,进行第一次(也是最后一次)数据绑定后RecycleView将不会再有任何数据的更新或者界面的修改;
//     * 所以在这个过程中不管在哪个地方进行了item数量的调整,都不会立即生效,因为不再有任何界面生成或者数据需要绑定了.<br>
//     * 因此这里将会通过接口通知建议再一次重置adapter或者是通知recycleView界面数据刷新,触发其渲染
//     *
//     * @param oldAdjustCount 上一次布局使用的adjustCount,此值一定为1
//     * @param newAdjustCount 更新后需要使用的adjustCount,此值一定大于1或者小于等于0
//     */
//    public void onSuggestResetAdapterOrNotifyDataChangedAgain(@NonNull RecyclerView parentView, @NonNull HeaderRecycleAdapter adapter, int oldAdjustCount, int newAdjustCount);
}
