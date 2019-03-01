package com.sjk.tpay.bll;


import android.text.TextUtils;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.sjk.tpay.HKApplication;
import com.sjk.tpay.HookAlipay;
import com.sjk.tpay.HookWechat;
import com.sjk.tpay.po.BaseMsg;
import com.sjk.tpay.po.Configer;
import com.sjk.tpay.po.QrBean;
import com.sjk.tpay.request.FastJsonRequest;
import com.sjk.tpay.utils.LogUtils;
import com.sjk.tpay.utils.SaveUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @ Created by Dlg
 * @ <p>TiTle:  ApiBll</p>
 * @ <p>Description: 和服务端交互的业务类</p>
 * @ date:  2018/9/21
 * @ QQ群：524901982
 */
public class ApiBll {
    private static ApiBll mApiBll;

    private RequestQueue mQueue;

    public static ApiBll getInstance() {
        if (mApiBll == null) {
            mApiBll = new ApiBll();
            mApiBll.mQueue = Volley.newRequestQueue(HKApplication.app);
        }
        return mApiBll;
    }

    /**
     * 检查是否需要发送新二维码
     */
    public void checkQR() {
        if (!Configer.getInstance().getUrl().toLowerCase().startsWith("http")) {
            return;//防止首次启动还没有配置，就一直去轮循
        }

        mQueue.add(new FastJsonRequest(Configer.getInstance().getUrl()
                + Configer.getInstance().getPage() + "?command=ask", succ, null));
        mQueue.start();
    }


    /**
     * 发送服务器所需要的二维码字符串给服务器
     * 服务器如果有新订单，就会立马返回新的订单，手机端就不用再等下次轮循了
     *
     * @param qrBean 要发送的二维码参数
     */
    public void sendQR(QrBean qrBean) throws UnsupportedEncodingException {
        StringBuilder stringBuilder = new StringBuilder(Configer.getInstance().getUrl())
                .append(Configer.getInstance().getPage())
                .append("?command=addqr")
                .append("&url=")
                .append(URLEncoder.encode(qrBean.getUrl(), "utf-8"))
                .append("&mark_sell=")
                .append(URLEncoder.encode(qrBean.getMark_sell(), "utf-8"));
        mQueue.add(new FastJsonRequest(stringBuilder.toString(), succ, null));
        mQueue.start();
        dealTaskList();
        LogUtils.show("发送二维码：" + stringBuilder.toString());
    }


    /**
     * 向服务器发送支付成功的消息
     * 如果因为一些原因，暂时没有通知成功，会保存任务，下次再尝试
     *
     * @param qrBean 订单详情信息
     */
    public void payQR(final QrBean qrBean) {
        StringBuilder url = null;
        try {
            url = new StringBuilder(Configer.getInstance().getUrl())
                    .append(Configer.getInstance().getPage())
                    .append("?command=do")
                    .append("&mark_sell=")
                    .append(URLEncoder.encode(qrBean.getMark_sell(), "utf-8"))
                    .append("&money=")
                    .append(qrBean.getMoney())
                    .append("&order_id=")
                    .append(URLEncoder.encode(qrBean.getOrder_id(), "utf-8"))
                    .append("&mark_buy=")
                    .append(URLEncoder.encode(qrBean.getMark_buy(), "utf-8"));
            mQueue.add(new FastJsonRequest(url.toString(), null, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error == null || error.networkResponse == null || error.networkResponse.statusCode < 500) {
                        //如果是服务器错误，自己检查代码，不然会一直发送成功订单造成多次支付！
                        addTaskList(qrBean);
                    }
                }
            }));
            mQueue.start();
            LogUtils.show("发送订单" + url.toString());
        } catch (Exception e) {
            addTaskList(qrBean);
        }
    }


    /**
     * 处理以前没有完成的任务
     */
    private void dealTaskList() {
        SaveUtils saveUtils = new SaveUtils();
        List<QrBean> list = saveUtils.getJsonArray(SaveUtils.TASK_LIST, QrBean.class);
        if (list != null) {
            //先清空任务，如果呆会儿在payQR里又失败的话，会自动又添加的。
            saveUtils.putJson(SaveUtils.TASK_LIST, null).commit();
            for (QrBean qrBean : list) {
                payQR(qrBean);
            }
        }
    }


    /**
     * 添加未完成的任务列表
     * 一定要用static的synchronized方式，上面的dealTaskList在某情况下可能会有问题
     * 但个人方案就暂不考虑这么极端的情况了
     *
     * @param qrBean
     */
    private synchronized static void addTaskList(QrBean qrBean) {
        SaveUtils saveUtils = new SaveUtils();
        List<QrBean> list = saveUtils.getJsonArray(SaveUtils.TASK_LIST, QrBean.class);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(qrBean);
        saveUtils.putJson(SaveUtils.TASK_LIST, list).commit();
    }


    /**
     * 当询问是否需要生成二维码返回成功后的操作
     */
    private final Response.Listener<BaseMsg> succ = new Response.Listener<BaseMsg>() {
        @Override
        public void onResponse(BaseMsg response) {
            if (response == null) {
                return;
            }
            QrBean qrBean = response.getData(QrBean.class);
            if (qrBean != null && qrBean.getMoney() > 0 && !TextUtils.isEmpty(qrBean.getMark_sell())) {
                LogUtils.show("服务器需要新二维码：" + qrBean.getMoney() + "|" + qrBean.getMark_sell() + "|" + qrBean.getChannel());
                if (qrBean.getChannel().contentEquals(QrBean.WECHAT)) {
                    HookWechat.getInstance().creatQrTask(qrBean.getMoney(), qrBean.getMark_sell());
                } else if (qrBean.getChannel().contentEquals(QrBean.ALIPAY)) {
                    HookAlipay.getInstance().creatQrTask(qrBean.getMoney(), qrBean.getMark_sell());
                }
            }
        }
    };

}
