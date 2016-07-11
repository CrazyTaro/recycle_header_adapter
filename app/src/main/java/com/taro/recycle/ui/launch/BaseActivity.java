package com.taro.recycle.ui.launch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.taro.recycle.R;

/**
 * Created by taro on 16/7/5.
 */
public abstract class BaseActivity extends Activity implements View.OnClickListener {
    public static final String ACTION_STANDARD = "1_intent_standard";
    public static final String ACTION_SINGLE_TOP = "1_intent_single_top";
    public static final String ACTION_SINGLE_TASK = "1_intent_single_task";
    public static final String ACTION_SINGLE_INSTANCE = "1_intent_single_instance";

    private static int mActivityStartIndex = 1;
    private int mActivityIndex = -1;

    private TextView mTvTaskId;
    private TextView mTvStartMode;
    private CheckBox mCbOutside;
    private Button mBtnStandard;
    private Button mBtnSingleTop;
    private Button mBtnSingleTask;
    private Button mBtnSingleInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        mTvTaskId = (TextView) findViewById(R.id.tv_launch_task_id);
        mTvStartMode = (TextView) findViewById(R.id.tv_launch_launch_mode);
        mCbOutside = (CheckBox) findViewById(R.id.cb_launch_outside_app);
        mBtnStandard = (Button) findViewById(R.id.btn_launch_standard);
        mBtnSingleTop = (Button) findViewById(R.id.btn_launch_single_top);
        mBtnSingleTask = (Button) findViewById(R.id.btn_launch_single_task);
        mBtnSingleInstance = (Button) findViewById(R.id.btn_launch_single_instance);

        setActivityIndex(getIntent());
        showActivityInfo();

        mBtnStandard.setOnClickListener(this);
        mBtnSingleTop.setOnClickListener(this);
        mBtnSingleTask.setOnClickListener(this);
        mBtnSingleInstance.setOnClickListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (mTvTaskId != null) {
            mTvTaskId.append("\nnew intent 启动");
        }
    }

    private void showActivityInfo() {
        mTvTaskId.append("tastId = " + getTaskId());
        mTvStartMode.append("mode = " + getLaunchMode() + "\nindex = " + mActivityIndex);
    }

    private void setActivityIndex(Intent intent) {
        if (mActivityIndex != -1) {
            mTvTaskId.append("\n已经启动过了");
        }
        if (intent != null) {
            mActivityIndex = intent.getIntExtra("index", -1);
        }
    }

    public void newStartActivity() {
        mActivityStartIndex++;
    }

    public abstract String getLaunchMode();

    @Override
    public void onClick(View v) {
        Intent startIntent = null;
        if (mCbOutside.isChecked()) {
            switch (v.getId()) {
                case R.id.btn_launch_standard:
                    startIntent = new Intent(ACTION_STANDARD);
                    break;
                case R.id.btn_launch_single_top:
                    startIntent = new Intent(ACTION_SINGLE_TOP);
                    break;
                case R.id.btn_launch_single_task:
                    startIntent = new Intent(ACTION_SINGLE_TASK);
                    break;
                case R.id.btn_launch_single_instance:
                    startIntent = new Intent(ACTION_SINGLE_INSTANCE);
                    break;
            }
        } else {
            switch (v.getId()) {
                case R.id.btn_launch_standard:
                    startIntent = new Intent(this, StandardActivity.class);
                    break;
                case R.id.btn_launch_single_top:
                    startIntent = new Intent(this, SingleTopActivity.class);
                    break;
                case R.id.btn_launch_single_task:
                    startIntent = new Intent(this, SingleTaskActivity.class);
                    break;
                case R.id.btn_launch_single_instance:
                    startIntent = new Intent(this, SingleInstanceActivity.class);
                    break;
            }
        }
        newStartActivity();
        startIntent.putExtra("index", mActivityStartIndex);
        startActivity(startIntent);
    }
}
