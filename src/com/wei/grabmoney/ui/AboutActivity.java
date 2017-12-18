package com.wei.grabmoney.ui;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.wei.grabmoney.R;

/**
 * 关于
 */
public class AboutActivity extends BaseActivity
{
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

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }

    private void initAdvs() {
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);
    }

    private void initView()
    {
        setContentView(R.layout.activity_about);
        setCustomTitle("关于");
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String appName = getResources().getString(R.string.app_name);
            String version = appName + " " + packageInfo.versionName;
            TextView versionTx = (TextView) findViewById(R.id.version);
            versionTx.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
