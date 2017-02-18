package com.wei.grabmoney.utils;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;

/**
 * Created by WEI on 2016/9/2.
 */
public class TextOptUtils
{
    public static boolean copyText(Context context, String txt)
    {
        boolean isSuccess;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setText(txt);
            isSuccess = true;
        }
        else
        {
            android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setText(txt);
            isSuccess = true;
        }
        return isSuccess;
    }
}
