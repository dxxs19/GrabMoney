package com.wei.grabmoney.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2016/9/3.
 */
public class SharedPreUtils
{
    private final String PRE_NAME = "mark";
    SharedPreferences sharedPreferences = null;
    SharedPreferences.Editor editor = null;
    public static SharedPreUtils sSharedPreUtils;
    private static Context mContext;

    public static SharedPreUtils getInstance(Context context)
    {
        if (sSharedPreUtils == null)
        {
            mContext = context;
            sSharedPreUtils = new SharedPreUtils(context);
        }
        return sSharedPreUtils;
    }

    public SharedPreUtils(Context context)
    {
        sharedPreferences = context.getSharedPreferences(PRE_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void putString(String key, String value)
    {
        editor.putString(key, value);
        editor.commit();
    }

    public String getString(String key, String defValue)
    {
        String result = sharedPreferences.getString(key, defValue);
        return result;
    }

    public void putInt(String key, int value)
    {
        editor.putInt(key, value);
        editor.commit();
    }

    public int getInt(String key, int defValue)
    {
        return sharedPreferences.getInt(key, defValue);
    }

    public void putFloat(String key, float value)
    {
        editor.putFloat(key, value);
        editor.commit();
    }

    public float getFloat(String key, float defValue)
    {
        return sharedPreferences.getFloat(key, defValue);
    }

    public void putBoolean(String key, boolean value)
    {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean getBoolean(String key, boolean defValue)
    {
        return sharedPreferences.getBoolean(key, defValue);
    }
}
