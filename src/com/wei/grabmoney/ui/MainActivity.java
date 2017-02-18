package com.wei.grabmoney.ui;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.wei.grabmoney.R;
import com.wei.grabmoney.constant.GlobalVariable;
import com.wei.grabmoney.utils.Log;
import com.wei.grabmoney.utils.SharedPreUtils;

import net.youmi.android.AdManager;
import net.youmi.android.listener.Interface_ActivityListener;
import net.youmi.android.offers.EarnPointsOrderInfo;
import net.youmi.android.offers.EarnPointsOrderList;
import net.youmi.android.offers.OffersManager;
import net.youmi.android.offers.PointsChangeNotify;
import net.youmi.android.offers.PointsEarnNotify;
import net.youmi.android.offers.PointsManager;

import java.util.List;

import cn.waps.AppConnect;

public class MainActivity extends BaseActivity implements TextWatcher, SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener, PointsChangeNotify, PointsEarnNotify {
    private final String TAG = "MainActivity";
    private TextView currentStateTxt, delayTimeTxt;
    private EditText contentTxt;
    private final Intent mAccessibleIntent =
            new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
    public static final String MARK_WORK = "mark_work", DELAY_TIME = "delay_time", FASTEST_CHECKED = "fastest_checked", HAS_PAYED = "has_payed";
    private SharedPreUtils mSharedPreUtils;
    private SeekBar mSeekBar;
    private final float MAX_TIME = 2000;
    private Button startServerBtn = null;
    private CheckBox fastestChk = null;
    private RadioButton voice_vibrate, rdoBtn_voice, rdoBtn_vibrate, rdoBtn_nothing;
    public final static String VOICE_VIBRATE = "voice_vibrate";
    public final static String VOICE = "voice";
    public final static String VIBRATE = "vibrate";
    public final static String NOTHING = "nothing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        initYoumi();
        // 初始化控件
        initView();
        // 初始化设置数据
        setData();
        // 初始化广告
        initAdvs();
    }

    private void initView()
    {
        setContentView(R.layout.activity_main);
        back.setVisibility(View.GONE);
        ImageButton setBtn = new ImageButton(this);
        setBtn.setBackgroundResource(R.drawable.btn_setting_selector);
        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, SettingActivity.class));
            }
        });
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleCustom.addView(setBtn, params);
        setCustomTitle("自动抢红包");
        currentStateTxt = (TextView) findViewById(R.id.txt_currentState);
        contentTxt = (EditText) findViewById(R.id.content);
        mSeekBar = (SeekBar) findViewById(R.id.delay_seekBar);
        fastestChk = (CheckBox) findViewById(R.id.chk_fastest);
        fastestChk.setOnCheckedChangeListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);
        delayTimeTxt = (TextView) findViewById(R.id.txt_delayTime);
        startServerBtn = (Button) findViewById(R.id.btn_start);

        voice_vibrate = (RadioButton) findViewById(R.id.voice_vibrate);
        rdoBtn_voice = (RadioButton) findViewById(R.id.rdoBtn_voice);
        rdoBtn_vibrate = (RadioButton) findViewById(R.id.rdoBtn_vibrate);
        rdoBtn_nothing = (RadioButton) findViewById(R.id.rdoBtn_nothing);

        voice_vibrate.setOnCheckedChangeListener(this);
        rdoBtn_voice.setOnCheckedChangeListener(this);
        rdoBtn_vibrate.setOnCheckedChangeListener(this);
        rdoBtn_nothing.setOnCheckedChangeListener(this);
    }

    private void setData() {
        mSharedPreUtils = new SharedPreUtils(mContext);
        String mark = mSharedPreUtils.getString(MARK_WORK, "");
        contentTxt.setText(mark);
        contentTxt.setSelection(mark.length());
        contentTxt.addTextChangedListener(this);

        float delayTime = mSharedPreUtils.getFloat(DELAY_TIME, 0);
        float rate = delayTime/MAX_TIME;
        mSeekBar.setProgress((int) (rate * 100));
        delayTimeTxt.setText(delayTime/1000 + " 秒 ");

        String value = mSharedPreUtils.getString("alarm", NOTHING);
        if (value.equals(MainActivity.NOTHING))
        {
            rdoBtn_nothing.setChecked(true);
        }
        else if (value.equals(MainActivity.VOICE_VIBRATE))
        {
            voice_vibrate.setChecked(true);
        }
        else if (value.equals(MainActivity.VOICE))
        {
            rdoBtn_voice.setChecked(true);
        }
        else if (value.equals(MainActivity.VIBRATE))
        {
            rdoBtn_vibrate.setChecked(true);
        }
    }

    private void initAdvs()
    {
//        AppConnect.getInstance(GlobalVariable.APP_ID, APP_PID,this);
//        AppConnect.getInstance(this).initPopAd(this);
//        initYoumi();

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);
    }

    private void initYoumi()
    {
        AdManager.getInstance(this).init(GlobalVariable.PUBLISH_ID, GlobalVariable.APP_SECURITY, false, true);

        // 如果使用积分广告，请务必调用积分广告的初始化接口:
        OffersManager.getInstance(this).onAppLaunch();

        // (可选)注册积分监听-随时随地获得积分的变动情况
        PointsManager.getInstance(this).registerNotify(this);

        // (可选)注册积分订单赚取监听（sdk v4.10版本新增功能）
        PointsManager.getInstance(this).registerPointsEarnNotify(this);

        // 奖励20积分
//        PointsManager.getInstance(this).awardPoints(20.0f);
        // 查询积分余额
        // 从5.3.0版本起，客户端积分托管将由 int 转换为 float
        pointsBalance = PointsManager.getInstance(this).queryPoints();
//        currentPointsTxt.setText("积分余额：" + pointsBalance);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceStatus();

        isPayPoint = mSharedPreUtils.getBoolean(FASTEST_CHECKED, false);
        Log.e(TAG, "是否开启了极速抢功能：" + isPayPoint);
        fastestChk.setChecked(isPayPoint);

        //设置插屏
//        showWpasSpot();
//        //设置广告条
//        setupBannerAd(SHOW_WPAS);

//        AppConnect.getInstance(this).awardPoints(PAY_POINTS);
//        AppConnect.getInstance(this).spendPoints(120);
    }

    private void updateServiceStatus() {
        boolean serviceActive = false;

        AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> accessibilityServiceInfos =
                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServiceInfos) {
            Log.e(TAG, info.getId());
            if (info.getId().equals(getPackageName() + "/.service.GrabMoneyService")) {
                serviceActive = true;
                break;
            }
        }
        currentStateTxt.setText(serviceActive ? "正在抢红包......" : "服务未启动");
        currentStateTxt.setTextColor(serviceActive ? Color.GREEN : Color.GRAY);
        if (serviceActive)
        {
            startServerBtn.setText("停止服务");
        }
        else
        {
            startServerBtn.setText("开启服务");
        }
    }

    /**
     * 开启服务
     * @param view
     */
    public void startService(View view)
    {
        try {
            mAccessibleIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mAccessibleIntent);
        }catch (Exception e)
        {

        }
    }

    public void stopService(View view) {
        stopService(mAccessibleIntent);
        updateServiceStatus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!TextUtils.isEmpty(s)) {
            String mark = s.toString();
            contentTxt.setSelection(mark.length());
            saveMark(mark);
        } else {
            saveMark("");
        }
    }

    private void saveMark(String mark)
    {
        mSharedPreUtils.putString(MARK_WORK, mark);
    }

    int mProgree;
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser)
        {
            this.mProgree = progress;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "--- onStartTrackingTouch ---");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "--- onStopTrackingTouch ---");
        float delayTime = mProgree * MAX_TIME/100;
        Log.e(TAG, "延迟了：" + delayTime + "毫秒");
        mSharedPreUtils.putFloat(DELAY_TIME, delayTime);
        delayTimeTxt.setText(delayTime/1000 + " 秒 ");
    }

    /**
     * 分享应用
     * @param v
     */
    public void reward(View v)
    {
//        startActivity(new Intent(this, RewardActivity.class));
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享应用");
        intent.putExtra(Intent.EXTRA_TEXT, "微信、QQ红包自动抢！其实,抢红包就是一件随心所欲的事情！"
                + SHARE_URL + "若点击无法跳转，请用浏览器打开！");
        intent.setType("text/plain");

        //设置分享列表的标题，并且每次都显示分享列表
        startActivity(Intent.createChooser(intent, "分享到"));
    }

    private boolean isPayPoint = false;
    public static int PAY_POINTS = 1;
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        switch (buttonView.getId())
        {
            case R.id.chk_fastest:
                mSharedPreUtils.putBoolean(FASTEST_CHECKED, isChecked);
                break;

            case R.id.voice_vibrate:
                if (isChecked)
                {
                    saveAlarm(VOICE_VIBRATE);
                }
                break;

            case R.id.rdoBtn_voice:
                if (isChecked)
                {
                    saveAlarm(VOICE);
                }
                break;

            case R.id.rdoBtn_vibrate:
                if (isChecked)
                {
                    saveAlarm(VIBRATE);
                }
                break;

            case R.id.rdoBtn_nothing:
                if (isChecked)
                {
                    saveAlarm(NOTHING);
                }
                break;
        }
    }

    private void saveAlarm(String value)
    {
        mSharedPreUtils.putString("alarm", value);
    }

    /**
     * 积分余额发生变动时，就会回调本方法（本回调方法执行在UI线程中）
     * <p/>
     * 从5.3.0版本起，客户端积分托管将由 int 转换为 float
     */
    @Override
    public void onPointBalanceChange(float pointsBalance) {
//        currentPointsTxt.setText("积分余额：" + pointsBalance);
        this.pointsBalance = pointsBalance;
    }

    /**
     * 积分订单赚取时会回调本方法（本回调方法执行在UI线程中）
     */
    @Override
    public void onPointEarn(Context context, EarnPointsOrderList list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        // 遍历订单并且toast提示
        for (int i = 0; i < list.size(); ++i) {
            EarnPointsOrderInfo info = list.get(i);
            Toast.makeText(this, info.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 退出时回收资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // （可选）注销积分监听
        // 如果在onCreate调用了PointsManager.getInstance(this).registerNotify(this)进行积分余额监听器注册，那这里必须得注销
//        PointsManager.getInstance(this).unRegisterNotify(this);
//
//        // （可选）注销积分订单赚取监听
//        // 如果在onCreate调用了PointsManager.getInstance(this).registerPointsEarnNotify(this)进行积分订单赚取监听器注册，那这里必须得注销
//        PointsManager.getInstance(this).unRegisterPointsEarnNotify(this);
//
//        // 回收积分广告占用的资源
//        OffersManager.getInstance(this).onAppExit();
    }
}
