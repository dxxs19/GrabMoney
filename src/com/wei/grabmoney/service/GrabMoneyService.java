package com.wei.grabmoney.service;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.wei.grabmoney.R;
import com.wei.grabmoney.bean.LuckMoneyInfo;
import com.wei.grabmoney.utils.Log;
import com.wei.grabmoney.ui.MainActivity;
import com.wei.grabmoney.utils.SharedPreUtils;

import java.util.HashMap;
import java.util.List;

//import com.lidroid.xutils.util.LogUtils;

/**
 * @author x-wei
 */
public class GrabMoneyService extends AccessibilityService
{
    private static final String TAG = "GrabMoneyService";
    private static final String WX_PKG = "com.tencent.mm";
    private static final String QQ_PKG = "com.tencent.mobileqq";
    private static final String WX_ID_PREFIX = WX_PKG + ":id/";

    /**
     * "开"按钮的id
     */
    private static final String OPENBTN_ID = WX_ID_PREFIX + "bjj"; //"com.tencent.mm:id/bi3"; //"com.tencent.mm:id/be_"; //"com.tencent.mm:id/bdh"; //"com.tencent.mm:id/bg7";
    //"com.tencent.mm:id/bfi"; //"com.tencent.mm:id/bdg"; //"com.tencent.mm:id/bag";
    /**
     * 打开红包界面的关闭界面按钮ID
     */
    private static final String RECEIVEUI_CLOSEBTN_ID = WX_ID_PREFIX + "bh8"; //"com.tencent.mm:id/bfu"; //"com.tencent.mm:id/bed";  //"com.tencent.mm:id/bdl"; //"com.tencent.mm:id/bga";
    //"com.tencent.mm:id/bfm"; //"com.tencent.mm:id/bdk"; //"com.tencent.mm:id/bak";
    /**
     * 红包详情界面的关闭界面按钮ID
     */
    private static final String DETAILUI_CLOSEBTN_ID = WX_ID_PREFIX + "gv"; //"com.tencent.mm:id/gv"; //"com.tencent.mm:id/gr"; //"com.tencent.mm:id/gp"; //"com.tencent.mm:id/gd";
    // "com.tencent.mm:id/gc";  //"com.tencent.mm:id/fs"; //"com.tencent.mm:id/fa";

    private static final int PERIOD_TIME = 3000;
    private static final String WEIXIN_CLASSNAME = "com.tencent.mm.ui.LauncherUI";
    private static final String WEIXIN_LUCKYMONEYRECEIVEUI = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
    private static final String WEIXIN_MONEY_TEXT = "[微信红包]";
    // 红包详情界面(已领该红包)
    private static final String WEIXIN_LUCKYMONEYDETAILUI = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
    private static final String GETMONEY_TEXT = "领取红包";
    private static final String CHECKMONEY_TEXT = "查看红包";
    private static final String FAILT_TEXT = "手慢了";
    private static final String TIMEOUT_TEXT = "该红包已超过";

    // QQ钱包界面
    private static final String QQ_WALLET = "cooperation.qwallet.plugin.QWalletPluginProxyActivity";
    private static final String QQ_CHAT = "com.tencent.mobileqq.activity.SplashActivity";
    private static final String QQ_TIPS = "点击拆开";
    private static final String QQ_COMMAND = "口令红包";
    private static final String QQ_CLICK_COMMAND = "点击输入口令";
    private static final String QQ_MONEY_TEXT = "[QQ红包]";

    private String className = "";

    private KeyguardManager keyguardManager;
    private KeyguardManager.KeyguardLock keyguardLock;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private Vibrator vibrator;

    private LuckMoneyInfo luckMoneyInfo = new LuckMoneyInfo();
    private boolean isAllClosed = false, hasOpenBtn = false, isFromHome = false;
    private SharedPreUtils mSharedPreUtils;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event)
    {
        int eventType = event.getEventType();
		Log.e(TAG, "eventType = " + eventType);
        openOrClose();
//        Log.e(TAG, "eventType begin");
        switch (eventType) {
            // 监听通知栏消息,如果有“【微信红包】”4个字，则跳转到微信聊天界面，开始抢红包
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                isFromHome = true;
                dealNotificationEven(event);
                break;

            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                luckMoneyInfo.cleanInfos();
                isAllClosed = false;
                hasOpenBtn = false;
                break;

            // 如果当前界面是微信聊天界面，则进行以下操作
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                className = event.getClassName().toString();
                break;

            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
				Log.e("className", className + ", hasOpenBtn : " + hasOpenBtn);
                // 以下是抢微信红包
                if (className.equals(WEIXIN_CLASSNAME)) {
                    // 如果是聊天界面，则搜索有没有红包，有则点击红包
                    Log.e(TAG, "--- 开始点红包 ---");
                    getMoney();
                } else if (className.equals(WEIXIN_LUCKYMONEYRECEIVEUI)) {
                    // 如果是领取红包界面，则打开红包
                    Log.e(TAG, "--- 准备开红包 ---");
                    openMoney();
                }
                else if (className.equals(WEIXIN_LUCKYMONEYDETAILUI))
                {
                    if (hasOpenBtn)
                    { // 自动点开的，要自动关掉
                        Log.e(TAG, "--- 准备关闭详情界面 ---");
                        closeUI(DETAILUI_CLOSEBTN_ID);
                        hasOpenBtn = false;
                        if (isFromHome)
                        {
                            isFromHome = false;
                            performGlobalAction(GLOBAL_ACTION_HOME);
                        }
                    }
                    else
                    {
                        return;
                    }
                }
                // 以下是抢QQ红包
                else if (className.equals(QQ_CHAT))
                {
                    isAllClosed = false;
                    getQQMoney();
                }
                else if (className.equals(QQ_WALLET))
                {
                    if (isAllClosed)
                        return;
                    try {
                        isAllClosed = true;
                        Thread.sleep(PERIOD_TIME * 2);
                        closeUI("com.tencent.mobileqq:id/close_btn");
                        closeUI("com.tencent.mobileqq:id/ivTitleBtnLeft");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void openOrClose()
    {
        AccessibilityNodeInfo openButtons = getOpenButtons();
        if (null != openButtons)
        {
            if (!isFastDoubleClick(3))
            {
                float delayTime = mSharedPreUtils.getFloat(MainActivity.DELAY_TIME, 0);
                try {
                    Thread.sleep((long) delayTime);
                    hasOpenBtn = true;
                    openButtons.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.e(TAG, "打开红包");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (null != root)
            {
                List<AccessibilityNodeInfo> failtInfos = getFailtInfos(root);

                if (null != failtInfos && failtInfos.size() > 0)
                {
                    List<AccessibilityNodeInfo> closeInfos = root.findAccessibilityNodeInfosByViewId(RECEIVEUI_CLOSEBTN_ID);
                    if (null != closeInfos && closeInfos.size() > 0)
                    {
                        try {
                            Thread.sleep(1000);
                            Log.e(TAG, "关闭开红包界面");
                            closeInfos.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private List<AccessibilityNodeInfo> getFailtInfos(AccessibilityNodeInfo root)
    {
        // 手慢了
        List<AccessibilityNodeInfo> slowInfos = root.findAccessibilityNodeInfosByText(FAILT_TEXT);
        // 已超时
        List<AccessibilityNodeInfo> timeOutInfos = root.findAccessibilityNodeInfosByText(TIMEOUT_TEXT);
        return ( null != slowInfos && slowInfos.size() > 0 ) ? slowInfos : timeOutInfos;
    }

    /**
     * 处理通知栏信息
     * @param event
     */
    private void dealNotificationEven(AccessibilityEvent event)
    {
        List<CharSequence> texts = event.getText();
//                LogUtils.e(texts.toString());
        if (!texts.isEmpty()) {
            for (CharSequence text : texts) {
                String content = text.toString();
//                        LogUtils.e("content : " + content);
                if (content.contains(WEIXIN_MONEY_TEXT) || content.contains(QQ_MONEY_TEXT))
                {
                    // 判断屏幕是否处于锁屏状态
                    if (keyguardManager.inKeyguardRestrictedInputMode()) {
                        Log.e(TAG, "屏幕处于锁屏状态！");
                        unlockAndVib(true);
                    }

                    // 模拟打开通知栏信息
                    if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                        Notification notification = (Notification) event.getParcelableData();
                        PendingIntent pendingIntent = notification.contentIntent;
                        try {
                            if (keyguardManager.inKeyguardRestrictedInputMode())
                            {
                                pendingIntent.send();
                                pendingIntent.send();
                            }
                            else
                            {
                                pendingIntent.send();
                            }
                            luckMoneyInfo.cleanInfos();
                            isAllClosed = false;
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * 关闭UI
     * @param id
     */
    private void closeUI(String id)
    {
        hasOpenBtn = false;
        // 如果是红包详情界面，则关闭
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (null != nodeInfo) {
            List<AccessibilityNodeInfo> infos = nodeInfo.findAccessibilityNodeInfosByViewId(id);
            if (infos != null) {
                for (AccessibilityNodeInfo info :
                        infos) {
                    info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
        else
        {
            performGlobalAction(GLOBAL_ACTION_BACK);
        }
    }

    /**
     * 获取“开”按钮，
     * @return
     */
    private AccessibilityNodeInfo getOpenButtons()
    {
        boolean isFastestChecked = SharedPreUtils.getInstance(this).getBoolean(MainActivity.FASTEST_CHECKED, false);
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();

        if (null != nodeInfo)
        {
            if (isFastestChecked)
            { // 极速抢(针对特定版本，速度更快)
                List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByViewId(OPENBTN_ID);
                if (nodes.size() > 0)
                {
                    return nodes.get(0);
                }
            }
            else if (className.equals(WEIXIN_LUCKYMONEYRECEIVEUI))
            { // 普通抢(与微信版本无关)
                int childCount = nodeInfo.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    AccessibilityNodeInfo node = nodeInfo.getChild(i);
                    if (node != null && node.getClassName() != null) {
                        if (node.getClassName().equals("android.widget.Button")) {
                            return node;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 找到可领取的红包并模拟点击
     */
    private void getMoney()
    {
        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
        if (null != rootInActiveWindow)
        {
            // 获取最新的那个红包进行点击
            AccessibilityNodeInfo targetNode = getTheLastNode(GETMONEY_TEXT, CHECKMONEY_TEXT);

            if (null != targetNode)
            {
                SharedPreferences sharedPreferences = getSharedPreferences("mark", MODE_PRIVATE);
                String mark = (sharedPreferences == null) ? "":sharedPreferences.getString("mark_work", "");
                if (luckMoneyInfo.isNodeCanOpen(targetNode, mark))
                {
                    Log.e(TAG, "luckMoneyInfo.isNodeCanOpen");
                    if (!isFastDoubleClick(1))
                    {
                        clickWallet(targetNode);
                        hasOpenBtn = true;
                    }
                }
            }
        }
    }

    /**
     * 找到可领取的QQ红包并模拟点击
     */
    private void getQQMoney()
    {
        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
        if (null != rootInActiveWindow)
        {
            // 普通红包
            List<AccessibilityNodeInfo> targetNodes = rootInActiveWindow.findAccessibilityNodeInfosByText(QQ_TIPS);
            if (null != targetNodes && targetNodes.size() > 0)
            {
                for (AccessibilityNodeInfo node:targetNodes)
                {
                    if (isCanGet(node))
                    {
                        float delayTime = mSharedPreUtils.getFloat(MainActivity.DELAY_TIME, 0);
                        try {
                            Thread.sleep((long) delayTime);
                            clickWallet(node);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            // 口令红包
            List<AccessibilityNodeInfo> cmdNodes = rootInActiveWindow.findAccessibilityNodeInfosByText(QQ_COMMAND);
            if (null != cmdNodes && cmdNodes.size() > 0)
            {
                List<AccessibilityNodeInfo> sendBtns = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/fun_btn");
                AccessibilityNodeInfo sendBtn = (sendBtns == null ? null : sendBtns.get(0));

                for (AccessibilityNodeInfo cmdNode:cmdNodes)
                {
                    String cmdText = cmdNode.getText().toString();
                    if (cmdText.equals(QQ_COMMAND))
                    {
                        clickWallet(cmdNode);
                    }
                }

                List<AccessibilityNodeInfo> clickCmdNodes = rootInActiveWindow.findAccessibilityNodeInfosByText(QQ_CLICK_COMMAND);
                if (null != clickCmdNodes && clickCmdNodes.size() > 0)
                {
                    for (AccessibilityNodeInfo clickCmdNode : clickCmdNodes)
                    {
                        clickWallet(clickCmdNode);
                        if (sendBtn != null && sendBtn.isEnabled())
                        {
                            float delayTime = mSharedPreUtils.getFloat(MainActivity.DELAY_TIME, 0);
                            try {
                                Thread.sleep((long) delayTime);
                                sendBtn.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private void clickWallet(AccessibilityNodeInfo node)
    {
        AccessibilityNodeInfo parent = node.getParent();
        while (null != parent)
        {
            if (parent.isClickable())
            {
                Log.e(TAG, "--- clickWallet ---");
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return;
            }
            parent = parent.getParent();
        }
    }

    private boolean isCanGet(AccessibilityNodeInfo node)
    {
        boolean isCan = true;
        SharedPreferences sharedPreferences = getSharedPreferences("mark", MODE_PRIVATE);
        String mark = (sharedPreferences == null) ? "":sharedPreferences.getString("mark_work", "");
        if ("".equals(mark))
        {
            isCan = true;
        }
        else
        {
            AccessibilityNodeInfo parent = node.getParent();
            String content = parent.getChild(0).getText().toString();
//            Log.e(TAG, "content = " + content);
            String[] works = mark.split("，");
            for (String work:works)
            {
                if (content.contains(work))
                {
                    isCan = false;
                    break;
                }
            }
        }
        return isCan;
    }

    /**
     * 找到最新的红包节点
     *
     * @param texts
     * @return
     */
    private AccessibilityNodeInfo getTheLastNode(String... texts)
    {
        int bottom = 0;
        AccessibilityNodeInfo lastNode = null;
        AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();

        for (String text : texts)
        {
            if (text == null || rootNodeInfo == null)
                continue;

            List<AccessibilityNodeInfo> nodes = rootNodeInfo.findAccessibilityNodeInfosByText(text);
            if (null == nodes)
            {
                lastNode = null;
            }
            else
            {
                if (!nodes.isEmpty()) {
                    AccessibilityNodeInfo node = nodes.get(nodes.size() - 1);
                    Rect bounds = new Rect();
                    node.getBoundsInScreen(bounds);
                    if (bounds.bottom > bottom) {
                        bottom = bounds.bottom;
                        lastNode = node;
                        if (text.equals(GETMONEY_TEXT)) {
                            luckMoneyInfo.others = true;
                        } else {
                            luckMoneyInfo.others = false;
                        }
                    }
                }
            }
        }
        return lastNode;
    }

    /**
     * 打开红包
     */
    private void openMoney()
    {
        Log.e(TAG, "--- openMoney ---");
        // 点击繁体的“开”。现在新版本微信红包是点“开”来领取
        AccessibilityNodeInfo info = getOpenButtons();
        if (null == info)
        {
            // 如果没有“开”可点，则直接关闭当前窗口，回到微信聊天界面
            if (!className.equals(WEIXIN_CLASSNAME) && !className.equals(WEIXIN_LUCKYMONEYDETAILUI))
            {
                try {
                    Thread.sleep(1000);
                    Log.e(TAG, "没有'开'按钮");
                    closeUI(RECEIVEUI_CLOSEBTN_ID);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            if (!isFastDoubleClick(2))
            {
                float delayTime = mSharedPreUtils.getFloat(MainActivity.DELAY_TIME, 0);
                try {
                    Thread.sleep((long) delayTime);
                    Log.e(TAG, "正在开红包...");
                    info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    hasOpenBtn = true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        // TODO Auto-generated method stub
        showMsg("抢红包服务已被中断！！！");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        keyguardLock = keyguardManager.newKeyguardLock("unLock");
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        mSharedPreUtils = new SharedPreUtils(this);
        initSoundPool();
        showMsg("服务已开启！！！");

//        setServiceToForeground();
    }

    private SoundPool mSoundPool;
    private HashMap<Integer, Integer> soundMap = new HashMap<>();
    private void initSoundPool() {
        mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
        soundMap.put(1, mSoundPool.load(this, R.raw.hongbao_arrived, 1));
    }

    /**
     * 解锁并震动3s
     */
    private void unlockAndVib(boolean b)
    {
        if (b)
        {
            String value = mSharedPreUtils.getString("alarm", "");
            if (value.equals(MainActivity.NOTHING))
                return;

            // 如果是锁屏状态，则解锁
            keyguardLock.disableKeyguard();
            if (!powerManager.isScreenOn()) {
                wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "bright");
                // 点亮屏幕
                wakeLock.acquire();
                Log.e(TAG, "亮屏");
            }

            if (value.equals(MainActivity.VOICE_VIBRATE))
            {
                mSoundPool.play(soundMap.get(1), 1, 1, 0, 0, 1);
                vibrator.vibrate(PERIOD_TIME * 2);
            }
            else if (value.equals(MainActivity.VOICE))
            {
                mSoundPool.play(soundMap.get(1), 1, 1, 0, 0, 1);
            }
            else if (value.equals(MainActivity.VIBRATE))
            {
                vibrator.vibrate(PERIOD_TIME * 2);
            }
//            // 播放系统自带声音
//            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//            Ringtone r = RingtoneManager.getRingtone(this, notification);
//            r.play();

        }

        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
            Log.e(TAG, "释放锁");
        }
    }

    private void showMsg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    protected long lastClickTime, lastClickTime2, lastClickTime3;
    /**
     * 判断控件在短时间内是否被连续点击了两次
     * @return
     */
    public boolean isFastDoubleClick(int flag)
    {
        long timeD = 0l;
        long time = System.currentTimeMillis();

        switch (flag)
        {
            case 1:
                timeD = time - lastClickTime;
                lastClickTime = time;
                break;

            case 2:
                timeD = time - lastClickTime2;
                lastClickTime2 = time;
                break;

            case 3:
                timeD = time - lastClickTime3;
                lastClickTime3 = time;
                break;
        }

        if (0 < timeD && timeD < PERIOD_TIME) {
            return true;
        }
        return false;
    }

    //
    private void setServiceToForeground()
    {
        Intent mAccessibleIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, mAccessibleIntent, 0);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .setContentTitle(getText(R.string.app_name))
                .setContentText("正在抢红包...");
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(103, builder.build());
        startForeground(103, builder.build());
    }
}
