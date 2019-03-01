package com.sjk.tpay.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.sjk.tpay.HKApplication;

import java.util.List;

/**
 * @ Created by Dlg
 * @ <p>TiTle:  SaveUtils</p>
 * @ <p>Description: 简单的保存一些用户配置和待发送的数据队列</p>
 * @ date:  2018/9/23
 * @ QQ群：524901982
 */
public class SaveUtils {
    private SharedPreferences mSharedPreferences;
    private Editor mEditor;
    /**
     * 保存用户首页设置的基本配置
     */
    public final static String BASE = "BASE";
    /**
     * 保存用户支付成功消息，却没有成功通知服务器的列表，下次会继续尝试发送通知
     */
    public final static String TASK_LIST = "TASK_LIST";

    public SaveUtils() {
        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(HKApplication.app);
        mEditor = mSharedPreferences.edit();
    }

    public boolean contains(String key) {
        return mSharedPreferences.contains(key);
    }


    public SaveUtils(String shareName) {
        mSharedPreferences = HKApplication.app.getSharedPreferences(shareName, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }


    /**
     * @param key
     * @param value 任意类，如果为null,显示清除本key
     * @return
     */
    public SaveUtils put(String key, Object value) {
        if (value == null)
            mEditor.remove(key);
        else
            mEditor.putString(key, value.toString());
        return this;
    }

    /**
     * 插入Json数据
     *
     * @param key
     * @param value 任意类，如果为null,显示清除本key
     * @return
     */
    public SaveUtils putJson(String key, Object value) {
        if (value == null) {
            mEditor.remove(key);
        } else {
            mEditor.putString(key, JSONObject.toJSONString(value));
        }
        return this;
    }


    /**
     * 获取Json类，失败返回空
     *
     * @param key
     * @param clazz
     * @param <T>   不能是Array数组
     * @return 失败返回null
     */
    public <T> T getJson(String key, Class<T> clazz) {
        try {
            return JSON.parseObject(mSharedPreferences.getString(key, ""), clazz);
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * 获取Json类，失败返回空
     *
     * @param key
     * @param clazz
     * @param <T>   Array数组类
     * @return 失败返回null
     */
    public <T> List<T> getJsonArray(String key, Class<T> clazz) {
        try {
            return JSONArray.parseArray(mSharedPreferences.getString(key, ""), clazz);
        } catch (JSONException e) {
            return null;
        }
    }

    public int getint(String key, int defVaule) {
        return Integer.valueOf(mSharedPreferences.getString(key, String.valueOf(defVaule)));
    }

    public long getlong(String key, long defVaule) {
        return Long.valueOf(mSharedPreferences.getString(key, String.valueOf(defVaule)));
    }

    public double getdouble(String key, double defVaule) {
        return Double.valueOf(mSharedPreferences.getString(key, String.valueOf(defVaule)));
    }

    public float getfloat(String key, float defVaule) {
        return Float.valueOf(mSharedPreferences.getString(key, String.valueOf(defVaule)));
    }

    public String getString(String key, String defVaule) {
        return mSharedPreferences.getString(key, defVaule);
    }

    public boolean getboolean(String key, boolean defVaule) {
        return mSharedPreferences.getString(key, String.valueOf(defVaule)).equals("true");
    }

    public SaveUtils commit() {
        mEditor.commit();
        return this;
    }
}
