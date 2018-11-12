package com.sjk.tpay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.sjk.tpay.bll.ApiBll;
import com.sjk.tpay.po.QrBean;
import com.sjk.tpay.utils.LogUtils;
import com.sjk.tpay.utils.PayUtils;

import static com.sjk.tpay.HookMain.RECEIVE_BILL_ALIPAY;
import static com.sjk.tpay.HookMain.RECEIVE_BILL_WECHAT;
import static com.sjk.tpay.HookMain.RECEIVE_QR_ALIPAY;
import static com.sjk.tpay.HookMain.RECEIVE_QR_WECHAT;


/**
 * @ Created by Dlg
 * @ <p>TiTle:  ReceiverMain</p>
 * @ <p>Description: 当HOOK之后的处理结果，只能用此广播来接受，不然很多数据不方便共享的</p>
 * @ date:  2018/09/22
 * @ QQ群：524901982
 */
public class ReceiverMain extends BroadcastReceiver {
    private ApiBll mApiBll;
    public static boolean mIsInit = false;
    private static String lastMsg = "";//防止重启接收广播，一定要用static

    public ReceiverMain() {
        super();
        mIsInit = true;
        LogUtils.show("Receiver创建成功！");
        mApiBll = new ApiBll();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (lastMsg.contentEquals(intent.getStringExtra("data"))) {
                return;
            }
            lastMsg = intent.getStringExtra("data");
            //LogUtils.show("Receiver--->" + intent.getAction());
            switch (intent.getAction()) {
                case RECEIVE_QR_WECHAT:
                    QrBean qrBean = JSON.parseObject(lastMsg, QrBean.class);
                    mApiBll.sendQR(qrBean.getUrl(), qrBean.getMark_sell());
                    break;
                case RECEIVE_BILL_WECHAT:
                    qrBean = JSON.parseObject(lastMsg, QrBean.class);
                    mApiBll.payQR(qrBean);
                    break;
                case RECEIVE_QR_ALIPAY:

                    break;
                case RECEIVE_BILL_ALIPAY:

                    break;
            }
        } catch (Exception e) {
            LogUtils.show(e.getMessage());
        }
    }

}
