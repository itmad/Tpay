package com.sjk.tpay;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.WindowManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sjk.tpay.po.QrBean;
import com.sjk.tpay.utils.LogUtils;
import com.sjk.tpay.utils.XmlToJson;

import java.lang.reflect.Field;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * @ Created by Dlg
 * @ <p>TiTle:  HookWechat</p>
 * @ <p>Description:微信的主要HOOK类</p>
 * @ date:  2018/09/22
 * @ QQ群：524901982
 */
public class HookWechat {

    protected void hook(final ClassLoader appClassLoader, final Context context) {
        try {
            hookBill(appClassLoader, context);
            hookQRCreat(appClassLoader, context);
            hookQRWindows(appClassLoader);
        } catch (Exception e) {
        }
    }


    /**
     * Hook微信收到订单消息后的处理
     *
     * @param appClassLoader
     * @param context
     */
    private void hookBill(final ClassLoader appClassLoader, final Context context) {
        XposedHelpers.findAndHookMethod("com.tencent.wcdb.database.SQLiteDatabase", appClassLoader, "insert", String.class, String.class, ContentValues.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {
                        try {
                            ContentValues contentValues = (ContentValues) param.args[2];
                            String tableName = (String) param.args[0];
                            if (TextUtils.isEmpty(tableName) || !tableName.equals("message")) {
                                return;
                            }
                            Integer type = contentValues.getAsInteger("type");
                            if (null == type) {
                                return;
                            }
                            if (type == 318767153) {
                                JSONObject msg = XmlToJson.documentToJSONObject(contentValues.getAsString("content"))
                                        .getJSONObject("appmsg");
                                if (!msg.getString("type").equals("5")) {
                                    //首款类型type为5
                                    return;
                                }
                                QrBean qrBean = new QrBean();
                                qrBean.setChannel(QrBean.WECHAT);
                                qrBean.setMoney((int) (Float.valueOf(msg.getJSONObject("mmreader")
                                        .getJSONObject("template_detail")
                                        .getJSONObject("line_content")
                                        .getJSONObject("topline")
                                        .getJSONObject("value")
                                        .getString("word")
                                        .replace("￥", "")) * 100));

                                qrBean.setOrder_id(msg.getString("template_id"));
                                JSONArray lines = msg.getJSONObject("mmreader")
                                        .getJSONObject("template_detail")
                                        .getJSONObject("line_content")
                                        .getJSONObject("lines")
                                        .getJSONArray("line");

                                for (int i = 0; i < 2; i++) {
                                    if (lines.size() < i + 1 && lines.getJSONObject(i) == null) {
                                        break;
                                    }
                                    if (lines.getJSONObject(i)
                                            .getJSONObject("key")
                                            .getString("word").contains("付款方")) {
                                        qrBean.setMark_buy(lines.getJSONObject(i)
                                                .getJSONObject("value")
                                                .getString("word"));
                                    } else if (lines.getJSONObject(i)
                                            .getJSONObject("key")
                                            .getString("word").contains("收款方")) {
                                        qrBean.setMark_sell(lines.getJSONObject(i)
                                                .getJSONObject("value")
                                                .getString("word"));
                                    }
                                }

                                LogUtils.show("微信收到支付订单：" + qrBean.getOrder_id()
                                        + "|" + qrBean.getMoney() + "|" + qrBean.getMark_sell() + "|" + qrBean.getMark_buy());

                                Intent broadCastIntent = new Intent();
                                broadCastIntent.putExtra("data", qrBean.toString());
                                broadCastIntent.setAction(HookMain.RECEIVE_BILL_WECHAT);
                                context.sendBroadcast(broadCastIntent);
                            }
                        } catch (Exception e) {
                            LogUtils.show(e.getMessage());
                        }
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                    }
                });
    }

    /**
     * hook二维码生成后操作，目的是为了得到创建二维码的url链接
     *
     * @param appClassLoader
     * @param context
     */
    private void hookQRCreat(final ClassLoader appClassLoader, final Context context) {
        Class<?> clazz = XposedHelpers.findClass("com.tencent.mm.plugin.collect.b.s", appClassLoader);
        XposedBridge.hookAllMethods(clazz, "a", new XC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
//				  double money=XposedHelpers.getDoubleField(param.thisObject, "llG");
//                来源在源码搜索类似如下
//                hashMap.put("fee", Math.round(100.0d * d));
//                hashMap.put("fee_type", str);
//                hashMap.put("desc", URLEncoder.encode(str2, "UTF-8"));
//               也可以搜索jSONObject.optString("pay_url");这个很准

                QrBean qrBean = new QrBean();
                qrBean.setChannel(QrBean.WECHAT);

                Field moneyField = XposedHelpers.findField(param.thisObject.getClass(), "iqu");
                Double money = (Double) moneyField.get(param.thisObject);

                Field markField = XposedHelpers.findField(param.thisObject.getClass(), "desc");
                String mark = (String) markField.get(param.thisObject);

                Field payurlField = XposedHelpers.findField(param.thisObject.getClass(), "iqt");
                String payurl = (String) payurlField.get(param.thisObject);


                LogUtils.show("微信成功生成二维码：" + money.floatValue() + "  " + mark + "  " + payurl);
                qrBean.setMark_sell(mark);
                qrBean.setUrl(payurl);

                Intent broadCastIntent = new Intent();
                broadCastIntent.putExtra("data", qrBean.toString());
                broadCastIntent.setAction(HookMain.RECEIVE_QR_WECHAT);
                context.sendBroadcast(broadCastIntent);
            }
        });
    }

    /**
     * 开始Hook二维码创建窗口，目的是为了创建生成二维码
     *
     * @param appClassLoader
     * @throws Exception
     */
    private void hookQRWindows(final ClassLoader appClassLoader) {
        Class<?> clazz = XposedHelpers.findClass("com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI", appClassLoader);
        XposedBridge.hookAllMethods(clazz, "onCreate", new XC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param) {

            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                LogUtils.show("Hook到微信窗口");
                ((Activity) param.thisObject).getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            }
        });

        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI", appClassLoader, "initView",
                new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        LogUtils.show("Hook微信开始......");
                        Intent intent = ((Activity) param.thisObject).getIntent();
                        String mark = intent.getStringExtra("mark");
                        String money = intent.getStringExtra("money");

                        Class<?> bs = XposedHelpers.findClass("com.tencent.mm.plugin.collect.b.s", appClassLoader);
                        Object obj = XposedHelpers.newInstance(bs, Double.valueOf(money), "1", mark);
                        XposedHelpers.callMethod(param.thisObject, "a", obj, true, true);
                    }
                });
    }


    /**
     * 这里是模拟备用方案，放着万一有用时再查看
     */
    private void noMock() {
        //下面是模拟实现的方案，备用，暂时是没有用的。

        //获取WalletFormView控件
//        Field WalletFormViewField = XposedHelpers.findField(param.thisObject.getClass(), "itl");
//        Object WalletFormView = WalletFormViewField.get(param.thisObject);
//        Class<?> WalletFormViewClass = XposedHelpers.findClass("com.tencent.mm.wallet_core.ui.formview.WalletFormView", appClassLoader);
//        //获取金额控件，类型是TenpaySecureEditText
//        Field AefField = XposedHelpers.findField(WalletFormViewClass, "vQa");
//        Object editView = AefField.get(WalletFormView);
//        //call设置金额方法
//        XposedHelpers.callMethod(editView, "setText", money);
//
//        //call设置备注方法，源代码如下
////                        public final boolean m(CharSequence charSequence) {
////                            if (bj.bl(charSequence.toString())) {
////                                CollectCreateQRCodeUI.a(this.ito.itn, "");
////                                CollectCreateQRCodeUI.c(this.ito.itn);
////                            } else {
////                                CollectCreateQRCodeUI.a(this.ito.itn, charSequence.toString());
////                                CollectCreateQRCodeUI.c(this.ito.itn);
////                            }
////                            return true;
////                        }
//        Class<?> clazz = XposedHelpers.findClass("com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI", appClassLoader);
//        XposedHelpers.callStaticMethod(clazz, "a", param.thisObject, mark);
//        XposedHelpers.callStaticMethod(clazz, "c", param.thisObject);
//
//        //点击确定，这个数字来源于如下源码
//        //CollectCreateQRCodeUI的initView里面有下面一句，找到next_btn即可
//        //((Button) findViewById(a$f.next_btn)).setOnClickListener(new CollectCreateQRCodeUI$3(this));
//        Button click = (Button) XposedHelpers.callMethod(param.thisObject, "findViewById", 2131756941);
//        click.performClick();
    }

}
