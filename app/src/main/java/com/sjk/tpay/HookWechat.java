package com.sjk.tpay;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.text.TextUtils;
import android.view.WindowManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sjk.tpay.imp.CallBackDo;
import com.sjk.tpay.po.QrBean;
import com.sjk.tpay.utils.ReflecUtils;
import com.sjk.tpay.utils.LogUtils;
import com.sjk.tpay.utils.PayUtils;
import com.sjk.tpay.utils.XmlToJson;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;


public class HookWechat extends HookBase {

    private static HookWechat mHookWechat;

    public static synchronized HookWechat getInstance() {
        if (mHookWechat == null) {
            mHookWechat = new HookWechat();
        }
        return mHookWechat;
    }


    @Override
    public void hookFirst() throws Error, Exception {
        //关屏也能打码，和打码的实现
        hookQRWindows();
    }

    @Override
    public void hookCreatQr() throws Error, Exception {
        Class<?> clazz = XposedHelpers.findClass("com.tencent.mm.plugin.collect.b.s", mAppClassLoader);
        XposedHelpers.findAndHookMethod(clazz, "a",
                int.class, String.class, org.json.JSONObject.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        try {
                            QrBean qrBean = new QrBean();
                            qrBean.setChannel(QrBean.WECHAT);
                            Double money = ReflecUtils.findField(param.thisObject.getClass(), double.class, 0, false)
                                    .getDouble(param.thisObject);
                            String mark = (String) ReflecUtils.findField(param.thisObject.getClass(), String.class, 1, false)
                                    .get(param.thisObject);
                            String payurl = (String) ReflecUtils.findField(param.thisObject.getClass(), String.class, 2, false)
                                    .get(param.thisObject);


                            LogUtils.show("微信成功生成二维码：" + money.floatValue() + "|" + mark);
                            qrBean.setMark_sell(mark);
                            qrBean.setUrl(payurl);
                            qrBean.setMoney(PayUtils.formatMoneyToCent(money.floatValue() + ""));
                            qrBean.setChannel(QrBean.WECHAT);

                            Intent broadCastIntent = new Intent(RECV_ACTION);
                            broadCastIntent.putExtra(RECV_ACTION_DATE, qrBean.toString());
                            broadCastIntent.putExtra(RECV_ACTION_TYPE, getLocalQrActionType());
                            mContext.sendBroadcast(broadCastIntent);
                        } catch (Error | Exception ignore) {
                            LogUtils.show(ignore.getMessage());
                        }
                    }
                });
    }

    @Override
    public void hookBill() throws Error, Exception {
        XposedHelpers.findAndHookMethod("com.tencent.wcdb.database.SQLiteDatabase",
                mAppClassLoader, "insert", String.class, String.class, ContentValues.class,
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
                            if (type != null && type == 318767153) {
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
                                if (TextUtils.isEmpty(qrBean.getMark_sell())) {
                                    return;
                                }

                                LogUtils.show("微信收到支付订单：" + qrBean.getMoney() + "|" + qrBean.getMark_sell() + "|" + qrBean.getMark_buy());

                                Intent broadCastIntent = new Intent(RECV_ACTION);
                                broadCastIntent.putExtra(RECV_ACTION_DATE, qrBean.toString());
                                broadCastIntent.putExtra(RECV_ACTION_TYPE, getLocalBillActionType());
                                mContext.sendBroadcast(broadCastIntent);
                            }
                        } catch (Error | Exception e) {

                        }
                    }
                });
    }

    @Override
    public void addRemoteTaskI() {
        addRemoteTask(getRemoteQrActionType(), new CallBackDo() {
            @Override
            public void callBack(Intent intent) throws Error, Exception {
                LogUtils.show("获取微信二维码");
                Intent intent2 = new Intent(mContext, XposedHelpers.findClass(
                        "com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI", mContext.getClassLoader()));
                intent2.putExtra("mark", intent.getStringExtra("mark"));
                intent2.putExtra("money", intent.getStringExtra("money"));
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent2);
            }
        });
    }

    @Override
    public void addLocalTaskI() {
        super.addLocalTaskI();
    }

    @Override
    public String getPackPageName() {
        return "com.tencent.mm";
    }

    @Override
    public String getAppName() {
        return "微信";
    }


    private void hookQRWindows() {
        Class<?> clazz = XposedHelpers.findClass("com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI", mAppClassLoader);
        XposedBridge.hookAllMethods(clazz, "onCreate", new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    ((Activity) param.thisObject).getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                } catch (Error | Exception ignore) {

                }
            }
        });

        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI",
                mAppClassLoader, "initView", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        try {
                            Intent intent = ((Activity) param.thisObject).getIntent();
                            String mark = intent.getStringExtra("mark");
                            String money = intent.getStringExtra("money");
                            if (TextUtils.isEmpty(mark)) {
                                return;
                            }
                            Class<?> bs = XposedHelpers.findClass("com.tencent.mm.plugin.collect.b.s", mAppClassLoader);
                            Object obj = XposedHelpers.newInstance(bs, Double.valueOf(money), "1", mark);

                            XposedHelpers.callMethod(param.thisObject, "a", obj, true, true);
                        } catch (Error | Exception ignore) {
                            LogUtils.show(ignore.getMessage()+"");
                        }
                    }
                });
    }
}