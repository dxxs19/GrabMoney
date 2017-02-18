package com.wei.grabmoney.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import com.wei.grabmoney.R;

/**
 * 注意事项
 */
public class BeCareActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_be_care);
        setCustomTitle("注意事项");
    }

    /**
     * 跳转到系统设置界面
     * @param v
     */
    public void openSetting(View v)
    {
        startActivity(new Intent(Settings.ACTION_SETTINGS));
    }
}
