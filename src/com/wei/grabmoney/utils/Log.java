package com.wei.grabmoney.utils;

/**
 * Created by Administrator on 2016/9/4.
 */
public class Log
{
    // 需要打印信息则将此变量设为true;
    private final static boolean IS_DEBUG = true;

    public static void e(String tag, String msg)
    {
        if (IS_DEBUG)
        {
            android.util.Log.e(tag, msg);
        }
    }

    public static void d(String tag, String msg)
    {
        if (IS_DEBUG)
        {
            android.util.Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg)
    {
        if (IS_DEBUG)
        {
            android.util.Log.i(tag, msg);
        }
    }

    public static void e(String tag, String s, Throwable e) {
        if (IS_DEBUG)
        {
            android.util.Log.e(tag, s, e);
        }
    }
}
