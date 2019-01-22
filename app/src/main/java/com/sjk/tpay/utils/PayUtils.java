package com.sjk.tpay.utils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.sjk.tpay.HookMain;
import com.sjk.tpay.bll.ApiBll;
import com.sjk.tpay.po.AliBillList;
import com.sjk.tpay.po.QrBean;
import com.sjk.tpay.request.StringRequestGet;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.robv.android.xposed.XposedHelpers;

/**
 * @ Created by Dlg
 * @ <p>TiTle:  PayUtils</p>
 * @ <p>Description: </p>
 * @ date:  2018/9/23
 * @ QQ群：524901982
 */
public class PayUtils {

    //软件首次启动后，只处理支付最近xxx秒的订单，默认为只处理最近1小时的订单
    private final static int ALIPAY_BILL_TIME = 3600 * 1000;

    private static PayUtils mPayUtils;

    public synchronized static PayUtils getInstance() {
        if (mPayUtils == null) {
            mPayUtils = new PayUtils();
        }
        return mPayUtils;
    }


    /**
     * 格式化金钱，把元变为分的单位
     *
     * @param money
     * @return
     */
    public static Integer formatMoneyToCent(String money) {
        return Integer.valueOf(new DecimalFormat("#").format(Float.valueOf(money.trim()) * 100));
    }

    /**
     * 格式化金钱，把分变为元的单位
     *
     * @param money
     * @return
     */
    public static String formatMoneyToYuan(String money) {
        String yuan = new DecimalFormat("#.00").format(Float.valueOf(money.trim()) / 100f);
        if (yuan.startsWith(".")) {
            yuan = "0" + yuan;
        }
        return yuan;
    }
}
