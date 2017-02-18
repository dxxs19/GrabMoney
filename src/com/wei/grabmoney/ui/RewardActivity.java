package com.wei.grabmoney.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import com.wei.grabmoney.R;
import com.wei.grabmoney.utils.TextOptUtils;

/**
 * 打赏界面
 */
public class RewardActivity extends BaseActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView()
    {
        setContentView(R.layout.activity_reward);
        setCustomTitle("打赏");
    }

    /**
     * 打赏作者
     * @param v
     */
    public void reward(View v)
    {
        boolean isCopySuccess = TextOptUtils.copyText(mContext, getResources().getString(R.string.alipay_account));
        showMsg(isCopySuccess ? "支付宝账户复制成功！" : "账号复制失败！");
        try
        {
            // 打开支付宝
            PackageManager packageManager = getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(ALIPAY_PKG);
            startActivity(intent);
        }
        catch (Exception e)
        {
            showMsg("未安装支付宝！");
        }
    }
}
