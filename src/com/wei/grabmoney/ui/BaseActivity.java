package com.wei.grabmoney.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wei.grabmoney.R;
import com.wei.grabmoney.utils.Log;

import net.youmi.android.listener.Interface_ActivityListener;
import net.youmi.android.offers.OffersManager;

import cn.waps.AppConnect;
import cn.waps.AppListener;
import cn.waps.UpdatePointsListener;

/**
 * 其它Activity的父类
 */
public class BaseActivity extends Activity implements UpdatePointsListener
{
    private String TAG = "";
//    protected final String SHARE_URL = "http://xwei.apps.cn";
    protected final String SHARE_URL = "http://xwei.apps.cn/details?app=f67090b3d70a79a470d85fada8936b33";
    protected Context mContext;
    private LinearLayout viewGroup;
    protected LinearLayout titleCustom;
    protected LinearLayout container;
    protected RelativeLayout titleContainer;
    protected LinearLayout headMid;
    protected View back;
    protected TextView title;
    // 显示相应平台的广告
    protected final int SHOW_YOUMI = 0x11;
    protected final int SHOW_WPAS = 0x12;
    protected final String APP_PID = "default";
    protected final String ALIPAY_PKG = "com.eg.android.AlipayGphone";
    protected float pointsBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mContext = this;
        TAG = getClass().getSimpleName();
        initBaseView();
    }

    private void initBaseView()
    {
        viewGroup = (LinearLayout) View.inflate(this, R.layout.title_bar, null);
        titleCustom = (LinearLayout) viewGroup.findViewById(R.id.title_custom);
        titleContainer = (RelativeLayout) viewGroup.findViewById(R.id.title_container);
        container = (LinearLayout) viewGroup.findViewById(R.id.container);
        headMid = (LinearLayout) viewGroup.findViewById(R.id.ll_head);
        back = viewGroup.findViewById(R.id.title_back);
        back.setVisibility(View.VISIBLE);
        title = (TextView) viewGroup.findViewById(R.id.title);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    public void setContentView(int layoutResID) {
        View contentView = View.inflate(this, layoutResID, null);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, -1);
        contentView.setLayoutParams(layoutParams);
        container.addView(contentView);
        super.setContentView(viewGroup);
    }

    public void hideTitle() {
        titleContainer.setVisibility(View.GONE);
        //title.setVisibility(View.GONE);
    }

    public void setCustomTitle(int resid) {
        title.setText(resid);
        title.setVisibility(View.VISIBLE);
    }

    public void setCustomTitle(String text) {
        title.setText(text);
        title.setVisibility(View.VISIBLE);
    }

    /**
     * 设置广告条广告
     * @param flag
     */
    protected void setupBannerAd(int flag)
    {
        switch (flag)
        {
            case SHOW_YOUMI:
                break;

            case SHOW_WPAS:
                showWpasBanner();
                break;
        }
    }

    // 显示万普广告条
    private void showWpasBanner()
    {
        LinearLayout adlayout = new LinearLayout(this);
        adlayout.setGravity(Gravity.CENTER_HORIZONTAL);
        RelativeLayout.LayoutParams rlayoutParams = new
                RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.RIGHT;

        AppConnect.getInstance(this).showBannerAd(this, adlayout);
        rlayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);//设置顶端或低端
        this.addContentView(adlayout, params);
    }

    protected void showSpotAds(int flag)
    {
        switch (flag)
        {
            case SHOW_YOUMI:
                break;

            case SHOW_WPAS:
                showWpasSpot();
                break;
        }
    }

    // 万普插屏广告
    protected void showWpasSpot()
    {
        AppConnect.getInstance(this).showPopAd(this);
        //调用方式 2：显示揑屏广告时设置关闭揑屏广告癿监听接口 (选用)
        AppConnect.getInstance(this).showPopAd(this, new AppListener(){
            @Override
            public void onPopClose() {super.onPopClose(); }
        });
    }

    protected void showMsg(String msg)
    {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    protected void showAlertDialog(String msg, String negativeTxt, String positiveTxt, final View.OnClickListener clickListener)
    {
        new AlertDialog.Builder(mContext)
                .setTitle("提示")
                .setMessage(msg)
                .setNegativeButton(negativeTxt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(positiveTxt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (null != clickListener)
                        {
                            clickListener.onClick(null);
                        }
                        dialog.dismiss();
                    }
                }).show();
    }

    public void showOffersWall()
    {
//        AppConnect.getInstance(mContext).showGameOffers(this);

        OffersManager.getInstance(this).showOffersWall(new Interface_ActivityListener()
        {
            /**
             * 当积分墙销毁的时候，即积分墙的Activity调用了onDestory的时候回调
             */
            @Override
            public void onActivityDestroy(Context context) {
//                Toast.makeText(context, "全屏积分墙退出了", Toast.LENGTH_SHORT).show();
            }
        });
    }

    final Handler mHandler = new Handler();
    protected String displayPointsText;
    // 创建一个线程
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            Log.e(TAG, displayPointsText);
        }
    };

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
        pointsBalance = pointTotal;
        mHandler.post(mUpdateResults);
    }

    @Override
    public void getUpdatePointsFailed(String error) {
        displayPointsText = error;
        mHandler.post(mUpdateResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        AppConnect.getInstance(this).getPoints(this);
    }

    @Override
    protected void onDestroy() {
        // 释放资源，原finalize()方法名修改为close()
        AppConnect.getInstance(this).close();
        super.onDestroy();
    }
}
