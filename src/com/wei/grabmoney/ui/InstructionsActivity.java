package com.wei.grabmoney.ui;

import android.os.Bundle;

import com.wei.grabmoney.R;

/**
 * 使用说明
 */
public class InstructionsActivity extends BaseActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        initView();
        //设置广告条
//        setupBannerAd(SHOW_WPAS);
//        setupBannerAd(SHOW_YOUMI);
    }

    private void initView()
    {
        setContentView(R.layout.activity_instructions);
        setCustomTitle("使用说明");
    }
}
