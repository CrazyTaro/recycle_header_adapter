package com.taro.recycle.ui.launch;

/**
 * Created by taro on 16/7/5.
 */
public class MainActivity extends BaseActivity {
    @Override
    public String getLaunchMode() {
        return this.getClass().getSimpleName();
    }
}
