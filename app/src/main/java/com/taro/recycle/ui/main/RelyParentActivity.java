package com.taro.recycle.ui.main;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.taro.headerrecycle.adapter.AutoFillAdjustChildAdapterOption;
import com.taro.headerrecycle.adapter.AutoFillAdjustChildHelper;
import com.taro.headerrecycle.adapter.HeaderRecycleAdapter;
import com.taro.headerrecycle.adapter.HeaderRecycleViewHolder;
import com.taro.headerrecycle.adapter.SimpleRecycleAdapter;
import com.taro.headerrecycle.utils.RecyclerViewUtil;
import com.taro.recycle.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by taro on 16/10/4.
 */

public class RelyParentActivity extends AppCompatActivity implements View.OnClickListener {
    LinearLayout mLlRoot;
    EditText mEtItemCount;
    Button mBtnItemCount;
    EditText mEtHeight;
    Button mBtnHeight;
    EditText mEtMargin;
    Button mBtnMargin;
    RecyclerView mRvRely;

    private int mMaxCount;
    private int mTempCount;
    private RelyAdapterOption3 mSimpleOption;
    private AutoFillAdjustChildAdapterOption mAdjustOption;
    private SimpleRecycleAdapter mAdapter;
    private List<String> mDataSource = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rely);

        mLlRoot = (LinearLayout) findViewById(R.id.ll_rely_root);
        mEtItemCount = (EditText) findViewById(R.id.et_rely_rv_item);
        mBtnItemCount = (Button) findViewById(R.id.btn_rely_update_item);
        mEtHeight = (EditText) findViewById(R.id.et_rely_rv_height);
        mBtnHeight = (Button) findViewById(R.id.btn_rely_update_height);
        mBtnMargin = (Button) findViewById(R.id.btn_rely_update_margin);
        mEtMargin = (EditText) findViewById(R.id.et_rely_rv_child_margin);
        mRvRely = (RecyclerView) findViewById(R.id.rv_rely);

        mDataSource = new ArrayList<String>(100);
        for (int i = 0; i < 10; i++) {
            mDataSource.add("");
        }

        mAdjustOption = new RelyAdapterOption2();
        //设置为在bindView时进行计算;否则notifyDataSetChanged不一定会更新界面
        mAdjustOption.setComputeWhen(AutoFillAdjustChildAdapterOption.COMPUTE_WHEN_BIND_VIEW);
        mAdjustOption.setChildMarginForAll(0, 20, 0, 20);

        mSimpleOption = new RelyAdapterOption3();
        //设置为在bindView时进行计算;否则notifyDataSetChanged不一定会更新界面
        mSimpleOption.getHelper().setComputeWhen(AutoFillAdjustChildAdapterOption.COMPUTE_WHEN_BIND_VIEW);
        mSimpleOption.getHelper().setChildMarginForAll(0, 20, 0, 20);

        mAdapter = new SimpleRecycleAdapter<String>(this, mSimpleOption, mDataSource);

        mRvRely.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRvRely.setAdapter(mAdapter);
        mBtnItemCount.setOnClickListener(this);
        mBtnHeight.setOnClickListener(this);
        mBtnMargin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_rely_update_item:
                CharSequence number = mEtItemCount.getText();
                if (TextUtils.isEmpty(number)) {
                    mTempCount = -1;
                } else {
                    mTempCount = Integer.valueOf(mEtItemCount.getText().toString());
                }

                if (mTempCount < 0) {
                    Toast.makeText(this, "小于0使用最大item数", Toast.LENGTH_SHORT).show();
                }
                //TODO:一定要先设置这个方法,否则更新recycleView时会因为缓存的view数量问题报错的.
//                mAdjustOption.setAdjustCount(mTempCount);
//                mAdjustOption.setIsAutoCompute(false);

                mSimpleOption.getHelper().setAdjustCount(mTempCount, mSimpleOption);
                mSimpleOption.getHelper().setIsAutoCompute(false);
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.btn_rely_update_height:
                CharSequence height = mEtHeight.getText();
                if (TextUtils.isEmpty(height)) {
                    return;
                } else {
                    int viewHeight = Integer.valueOf(height.toString());
                    if (viewHeight > 0) {
                        float dpx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, viewHeight, this.getResources().getDisplayMetrics());
                        if (dpx > mRvRely.getWidth()) {
                            Toast.makeText(this, "设置高度不能超过宽度", Toast.LENGTH_LONG).show();
                            return;
                        }
                        RecyclerViewUtil.setViewWidthAndHeight(mRvRely, ViewGroup.LayoutParams.MATCH_PARENT, (int) dpx);
//                        mAdjustOption.requestForceRecompute();
//                        mAdjustOption.setIsAutoCompute(true);
                        mSimpleOption.getHelper().requestForceRecompute();
                        mSimpleOption.getHelper().setIsAutoCompute(true);
                        mAdapter.notifyDataSetChanged();
                    }
                }
                break;
            case R.id.btn_rely_update_margin:
                CharSequence margin = mEtMargin.getText();
                if (TextUtils.isEmpty(margin)) {
                    return;
                } else {
                    int viewMargin = Integer.valueOf(margin.toString());
                    float dpx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, viewMargin, this.getResources().getDisplayMetrics());
//                    mAdjustOption.setChildMarginForAll((int) dpx);
//                    mAdjustOption.requestForceRecompute();
                    mSimpleOption.getHelper().setChildMarginForAll((int) dpx);
                    mSimpleOption.getHelper().requestForceRecompute();
                    mAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    private class RelyAdapterOption2 extends AutoFillAdjustChildAdapterOption<String> {
        @Override
        public int getViewType(int position) {
            return 0;
        }

        @Override
        public int getLayoutId(int viewType) {
            return R.layout.item_rely;
        }

        @Override
        public void setViewHolder(String itemData, int position, @NonNull HeaderRecycleViewHolder holder) {
            super.setViewHolder(itemData, position, holder);

            ImageView itemView = (ImageView) holder.getRootView();
            itemView.setImageResource(R.drawable.bg);
            itemView.setScaleType(ImageView.ScaleType.FIT_XY);
        }
    }

    private class RelyAdapterOption3 extends SimpleRecycleAdapter.SimpleAdapterOption<String> {
        AutoFillAdjustChildHelper mAdjustHelper = null;

        public RelyAdapterOption3() {
            this.mAdjustHelper = new AutoFillAdjustChildHelper();
        }

        public AutoFillAdjustChildHelper getHelper() {
            return mAdjustHelper;
        }

        @Override
        public int getViewType(int position) {
            return 0;
        }

        @Override
        public void onCreateViewEverytime(@NonNull View itemView, @NonNull ViewGroup parentView, @NonNull HeaderRecycleAdapter adapter, int viewType) {
            super.onCreateViewEverytime(itemView, parentView, adapter, viewType);
            mAdjustHelper.computeOnCreateViewHolder(this, itemView, parentView);
        }

        @Override
        public void setViewHolder(String itemData, int position, @NonNull HeaderRecycleViewHolder holder) {
            mAdjustHelper.computeOnBindViewHolder(this, holder);

            ImageView itemView = (ImageView) holder.getRootView();
            itemView.setImageResource(R.drawable.bg);
            itemView.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        @Override
        public int getLayoutId(int viewType) {
            return R.layout.item_rely;
        }
    }

    //历史测试使用的文件,不再使用
    @Deprecated
    private class RelyAdapterOption extends SimpleRecycleAdapter.SimpleAdapterOption<String> {
        private boolean mIsRecompute = true;
        private Point mParentParams;
        private Point mChildParams;

        public RelyAdapterOption() {
            mParentParams = new Point();
            mChildParams = new Point();
            resetRvHeight();
        }

        public void setIsRecompute(boolean isRecompute) {
            mIsRecompute = isRecompute;
        }

        public void resetRvHeight() {
            mParentParams.set(0, 0);
            mMaxCount = -1;
            mTempCount = -1;
            mIsRecompute = true;
        }

        @Override
        public void setAdjustCount(int adjustCount) {
            if (adjustCount >= 0 && adjustCount <= mMaxCount) {
                super.setAdjustCount(adjustCount);
            } else {
                super.setAdjustCount(mMaxCount);
            }
        }

        @Override
        public int getViewType(int position) {
            return 0;
        }

        @Override
        public void setViewHolder(String itemData, int position, HeaderRecycleViewHolder holder) {
            if (mParentParams.equals(0, 0)) {
                //计算recycleView界面的宽高
                RecyclerViewUtil.computeParentViewDrawArea(holder.getAdatper().getParentRecycleView(), mParentParams);
            }
            if (mMaxCount <= 0) {
                //计算基于宽/高可填充的item数量
                mMaxCount = RecyclerViewUtil.computeSquareChildViewCountOnParentView(mParentParams.x, mParentParams.y, false);
            }
            //若当前的指定item数量不合法,使用可填充的最大item数量代替
            if (mTempCount < 0 || mTempCount > mMaxCount) {
                mTempCount = mMaxCount;
            }
            //根据标志或数据判断是否需要重新计算一次界面item之前的边距值(即额外的margin部分)
            if (mIsRecompute || mChildParams.y == Integer.MIN_VALUE) {
                mChildParams.y = RecyclerViewUtil.computeSquareChildViewEdgeSize(mParentParams.x, mParentParams.y, mTempCount, false);
                mIsRecompute = false;
                //更新调整的界面item数量
                this.setAdjustCount(mTempCount);
            }
            //设置每一个创建itemView的参数,宽高/margin/额外的margin部分
            RecyclerViewUtil.computeSquareChildViewLayoutParamsWithSet(holder.getRootView(), 0, 0, 0, 0, mChildParams.y, mParentParams.x, mParentParams.y, false);


            ImageView itemView = (ImageView) holder.getRootView();
            itemView.setImageResource(R.drawable.bg);
            itemView.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        @Override
        public int getLayoutId(int viewType) {
            return R.layout.item_rely;
        }

        @Override
        public void onCreateViewEverytime(@NonNull View itemView, @NonNull ViewGroup parentView, @NonNull HeaderRecycleAdapter adapter, int viewType) {
            //每一次view被创建时都会回调此方法,这里是进行view宽高设置的好地方;
            //但是需要注意根据需求来确定当前的计算和设置的代码应该放在哪里;
            //setViewHolder是每一次绑定数据时都会进行调用的地方;而onCreate则只有创建时会调用;
            //如果创建缓存的view足够多时,在动态更新item数量时onCreate并不会被调用,而setViewHolder是一定会被调用的;
            //所以应该根据需求确定界面相关的更新代码应该放在何处会更好.
//            if (mParentParams.equals(0, 0)) {
//                //计算recycleView界面的宽高
//                RecyclerViewUtil.computeParentViewDrawArea(parentView, mParentParams);
//            }
//            if (mMaxCount <= 0) {
//                //计算基于宽/高可填充的item数量
//                mMaxCount = RecyclerViewUtil.computeSquareChildViewCountOnParentView(mParentParams.x, mParentParams.y, false);
//            }
//            //若当前的指定item数量不合法,使用可填充的最大item数量代替
//            if (mTempCount < 0 || mTempCount > mMaxCount) {
//                mTempCount = mMaxCount;
//            }
//            //根据标志或数据判断是否需要重新计算一次界面item之前的边距值(即额外的margin部分)
//            if (mIsRecompute || mChildParams.y == Integer.MIN_VALUE) {
//                mChildParams.y = RecyclerViewUtil.computeSquareChildViewEdgeSize(mParentParams.x, mParentParams.y, mTempCount, false);
//                mIsRecompute = false;
//                //更新调整的界面item数量
//                this.setAdjustCount(mTempCount);
//            }
//            //设置每一个创建itemView的参数,宽高/margin/额外的margin部分
//            RecyclerViewUtil.computeSquareChildViewLayoutParamsWithSet(itemView, 0, 0, 0, 0, mChildParams.y, mParentParams.x, mParentParams.y, false);
        }
    }
}
