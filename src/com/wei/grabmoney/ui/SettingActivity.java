package com.wei.grabmoney.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.wei.grabmoney.R;
import com.wei.grabmoney.constant.GlobalVariable;

import cn.waps.AppConnect;
import cn.waps.UpdatePointsListener;

/**
 * 设置
 */
public class SettingActivity extends BaseActivity implements UpdatePointsListener
{
    final Handler mHandler = new Handler();
    private TextView pointsTextView;
    private String displayPointsText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        initView();
        //设置广告条
//        setupBannerAd(SHOW_WPAS);
//        setupBannerAd(SHOW_YOUMI);
        initAdvs();
    }

    private void initAdvs() {
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);
    }

    private void initView()
    {
        setContentView(R.layout.activity_setting);
        setCustomTitle("设置");
        pointsTextView = (TextView) findViewById(R.id.txt_integralwall);
    }

    /**
     * 跳转到使用说明界面
     * @param v
     */
    public void openInstructions(View v)
    {
        startActivity(new Intent(this, InstructionsActivity.class));
    }

    /**
     * 跳转到系统设置界面
     * @param v
     */
    public void openSetting(View v)
    {
        startActivity(new Intent(Settings.ACTION_SETTINGS));
    }

    /**
     * 注意事项
     * @param v
     */
    public void showMyApps(View v)
    {
//        AppConnect.getInstance(this).showMore(this);
        startActivity(new Intent(this, BeCareActivity.class));
    }

    public void myApps(View v)
    {
        AppConnect.getInstance(this).showMore(this);
    }

    public void showOffers(View v)
    {
//        AppConnect.getInstance(this).showOffers(this);
//        AppConnect.getInstance(this).showOffers(this, GlobalVariable.APP_ID);
        showOffersWall();
    }

    /**
     * 打赏作者
     * @param v
     */
    public void reward(View v)
    {
        startActivity(new Intent(this, RewardActivity.class));
    }

    /**
     * 跳转到关于界面
     * @param v
     */
    public void openAbout(View v)
    {
        startActivity(new Intent(this, AboutActivity.class));
    }

    /**
     * AppConnect.getPoints()方法的实现，必须实现
     *
     * @param currencyName
     *            虚拟货币名称.
     * @param pointTotal
     *            虚拟货币余额.
     */
    @Override
    public void getUpdatePoints(String currencyName, int pointTotal) {
        displayPointsText = currencyName + ": " + pointTotal;
        mHandler.post(mUpdateResults);
    }

    /**
     * AppConnect.getPoints() 方法的实现，必须实现
     *
     * @param error
     *            请求失败的错误信息
     */
    @Override
    public void getUpdatePointsFailed(String error) {
        displayPointsText = error;
        mHandler.post(mUpdateResults);
    }

    // 创建一个线程
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            if (pointsTextView != null) {
                pointsTextView.setText("积分墙 " + " ( 当前总积分： " + displayPointsText + " ) ");
            }
        }
    };
}
