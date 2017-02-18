package com.wei.grabmoney.bean;

import com.wei.grabmoney.utils.Log;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by x-wei on 2016/3/20.
 */
public class LuckMoneyInfo
{
    private final String TAG = getClass().getSimpleName();
    private final String TARGET_SYMBOL = "，";
    public String mSender, mContent, mTime;
    public boolean others;
    public AccessibilityNodeInfo mNodeInfo;

    public boolean isNodeCanOpen(AccessibilityNodeInfo node, String mark)
    {
        try
        {
            AccessibilityNodeInfo nodeParent = node.getParent();
            if (!"android.widget.LinearLayout".equals(nodeParent.getClassName()))
            {
                return false;
            }

            String content = nodeParent.getChild(0).getText().toString(); // 获取文字红包内容
            Log.e(TAG, "红包内容 : " + content);
            Log.e(TAG, "mark : " + mark );
            if (!"".equals(mark))
            {
                if (mark.contains(TARGET_SYMBOL))
                {
                    String[] arrs = mark.split(TARGET_SYMBOL);
                    for (String arr:arrs)
                    {
                        if (content.contains(arr))
                        {
                            // 含关键字
                            return false;
                        }
                    }
                }
//                else
//                {
//                    if (content.contains(mark))
//                    {
//                        // 含关键字
//                        return false;
//                    }
//                    return true;
//                }
            }

            String info = getSenderAndTime(nodeParent.getParent());
            String currentInfos = info + "," + content + "," + node.hashCode();
            String lastInfos = mSender + "," + mTime + "," + mContent + "," + ( mNodeInfo == null ? 1 : mNodeInfo.hashCode());
            Log.e("currentInfos", currentInfos);
            Log.e("lastInfos", lastInfos);
            if (currentInfos.equals(lastInfos))
            {
                return false;
            }
            else
            {
                mContent = content;
                String[] infos = info.split(",");
                if (infos.length == 2)
                {
                    mSender = infos[0];
                    mTime = infos[1];
                }
                mNodeInfo = node;
            }
            return true;
        }
        catch (Exception e)
        {
            Log.e("异常：", e.getMessage()  + "");
            return false;
        }
    }

    private String getSenderAndTime(AccessibilityNodeInfo parent)
    {
        String time = "", sender = "";
        AccessibilityNodeInfo senderNode = null;

        try {
            AccessibilityNodeInfo root = parent;
            AccessibilityNodeInfo timeNode = root.getChild(0);
            if (timeNode != null && timeNode.getClassName().equals("android.widget.TextView"))
            {
                time = timeNode.getText().toString();
                senderNode = root.getChild(1);
            }
            else
            {
                senderNode = root.getChild(0);
                time = "xx";
            }
            sender = senderNode.getContentDescription() + "";
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.e("布局解析出错！", e.getMessage());
        }
        return sender + "," + time;
    }

    public void cleanInfos()
    {
        mContent = "";
        mSender = "";
        mTime = "";
        mNodeInfo = null;
    }
}
