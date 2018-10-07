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

    //软件首次启动后，只处理支付最近xxx秒的订单，默认为只处理最近20分钟的订单
    private final static int ALIPAY_BILL_TIME = 1200 * 1000;

    private static PayUtils mPayUtils;

    public synchronized static PayUtils getInstance() {
        if (mPayUtils == null) {
            mPayUtils = new PayUtils();
        }
        return mPayUtils;
    }


    /**
     * @param context
     * @param money   金额，单位为分，范围1-30000000
     * @param mark    收款备注，最长30个字符，不能为空
     */
    public void creatWechatQr(Context context, Integer money, String mark) {
        if (money == null || TextUtils.isEmpty(mark)) {
            return;
        }
        if (mark.length() > 30 || money > 30000000 || money < 1) {
            return;
        }
        Intent broadCastIntent = new Intent();
        broadCastIntent.setAction(HookMain.WECHAT_CREAT_QR);
        broadCastIntent.putExtra("mark", mark);
        broadCastIntent.putExtra("money", String.valueOf(money / 100.0f));
        context.sendBroadcast(broadCastIntent);
    }


    /**
     * 这里为了统一，要求就设置为和微信一样了。
     *
     * @param context
     * @param money   金额，单位为分，范围1-30000000
     * @param mark    收款备注，最长30个字符，不能为空
     */
    public void creatAlipayQr(Context context, Integer money, String mark) {
        if (money == null || TextUtils.isEmpty(mark)) {
            return;
        }
        if (mark.length() > 30 || money > 30000000 || money < 1) {
            return;
        }
        Intent broadCastIntent = new Intent();
        broadCastIntent.setAction(HookMain.ALIPAY_CREAT_QR);
        broadCastIntent.putExtra("mark", mark);
        broadCastIntent.putExtra("money", String.valueOf(money / 100.0f));
        context.sendBroadcast(broadCastIntent);
    }


}
