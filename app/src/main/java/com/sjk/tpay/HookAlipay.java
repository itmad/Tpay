package com.sjk.tpay;

import com.sjk.tpay.utils.LogUtils;

public class HookAlipay extends HookBase {

    private static HookAlipay mHookAlipay;

    public static synchronized HookAlipay getInstance() {
        if (mHookAlipay == null) {
            mHookAlipay = new HookAlipay();
        }
        return mHookAlipay;
    }

    @Override
    public void hookCreatQr() throws Error, Exception {
        LogUtils.show("支付宝开源版暂不免费提供学习,http://www.paohuituan.com/pay/");
    }

    @Override
    public void hookBill() throws Error, Exception {

    }

    @Override
    public void addRemoteTaskI() {

    }

    @Override
    public String getPackPageName() {
        return "com.eg.android.AlipayGphone";
    }

    @Override
    public String getAppName() {
        return "支付宝";
    }
}
