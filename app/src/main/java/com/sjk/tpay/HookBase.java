package com.sjk.tpay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;

import com.alibaba.fastjson.JSON;
import com.sjk.tpay.bll.ApiBll;
import com.sjk.tpay.imp.CallBackDo;
import com.sjk.tpay.imp.IHooker;
import com.sjk.tpay.po.QrBean;
import com.sjk.tpay.utils.LogUtils;

import java.util.HashMap;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

import static com.sjk.tpay.utils.PayUtils.formatMoneyToYuan;

public abstract class HookBase implements IHooker {

    private Integer mHookCount = 0;
    private Integer mHookCountIndex = 1;//HOOK第几个进程
    protected ClassLoader mAppClassLoader;
    protected Context mContext;


    //这里是广播的Action,本地的广播我改来就只有一个Action了，根据TYPE来判断类型了。
    public static final String RECV_ACTION = "COM.SJK.RECV_ACTION";
    //本广播的操作TYPE是什么，getSringExtra
    public static final String RECV_ACTION_DATE = "data";
    //本广播的操作TYPE是什么，getSringExtra
    public static final String RECV_ACTION_TYPE = "actiontype";


    //在HOOK程序要进行的操作
    public static HashMap<String, CallBackDo> mTaskInOtherApp = new HashMap<>();


    /**
     * @param packPagename
     * @param processName
     * @param hookCountIndex 第几个进程才HOOK，一般为1，vxp可能要写2，如果不知道，那就写0，都去hook
     */
    public void hook(String packPagename, String processName, Integer hookCountIndex) {
        if (!getPackPageName().contentEquals(packPagename)) {
            return;
        }
        mHookCountIndex = hookCountIndex;
        try {
            hookMainInOtherAppContext(processName);
        } catch (Throwable e) {
            LogUtils.show(getAppName() + "重大错误：" + e.getMessage());
        }
    }

    private void hookMainInOtherAppContext(final String processName) {
        XposedHelpers.findAndHookMethod(ContextWrapper.class, "attachBaseContext", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                mContext = (Context) param.args[0];
                mAppClassLoader = mContext.getClassLoader();
                if (getPackPageName().equals(processName)) {
                    mHookCount = mHookCount + 1;
                    if (mHookCount == mHookCountIndex) {
                        //注册广播
                        IntentFilter intentFilter = new IntentFilter();
                        intentFilter.addAction(getRemoteAction());//主动远程的主动广播Action
                        mContext.registerReceiver(new RemoteReceiver(), intentFilter);

                        try {
                            addRemoteTaskI();
                            hookFirst();
                            hookBill();
                            hookCreatQr();
                            LogUtils.show(getAppName() + "初始化成功");
                        } catch (Error | Exception e) {
                            e.printStackTrace();
                            LogUtils.show(e.getMessage());
                        }
                    }
                }
            }
        });
    }


    /**
     * 创建二维码的广播，注册到hook的进程领空
     *
     * @return
     */
    @Override
    public String getRemoteAction() {
        return "RemoteAction." + getClass().getSimpleName();
    }

    @Override
    public String getRemoteQrActionType() {
        return "RemoteQrActionType." + getClass().getSimpleName();
    }

    /**
     * 收到订单的广播Action
     *
     * @return
     */
    @Override
    public String getRemoteBillActionType() {
        return "RemoteBillActionType." + getClass().getSimpleName();
    }

    /**
     * 收到二维码的广播Action
     *
     * @return
     */
    @Override
    public String getLocalQrActionType() {
        return "LocalQrActionType." + getClass().getSimpleName();
    }

    /**
     * 收到订单的广播Action
     *
     * @return
     */
    @Override
    public String getLocalBillActionType() {
        return "LocalBillActionType." + getClass().getSimpleName();
    }


    /**
     * @param action     也就是RECV_ACTION_TYPE的值
     * @param callBackDo
     * @return
     */
    public HookBase addRemoteTask(String action, CallBackDo callBackDo) {
        mTaskInOtherApp.put(action, callBackDo);
        return this;
    }

    @Override
    public void doOnOtherApp(Intent intent) throws Error, Exception {
        LogUtils.show(intent.getStringExtra(RECV_ACTION_TYPE) + "mTaskInOtherApp" + mTaskInOtherApp);
        Set<String> set = mTaskInOtherApp.keySet();
        for (String str : set) {
            if (intent.getStringExtra(RECV_ACTION_TYPE).contentEquals(str)) {
                mTaskInOtherApp.get(str).callBack(intent);
            }
        }
    }

    /**
     * 通用的创建二维码的广播
     */
    class RemoteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                doOnOtherApp(intent);
            } catch (Error | Exception e) {
                LogUtils.show(getAppName() + "doOnOtherApp错误：" + intent.getStringExtra(RECV_ACTION_TYPE) + e.getMessage());
            }
        }
    }


    @Override
    public void hookFirst() throws Error, Exception {

    }

    @Override
    public void addLocalTaskI() {
        addLocalTask(getLocalQrActionType(), new CallBackDo() {
            @Override
            public void callBack(Intent intent) throws Error, Exception {
                String data = intent.getStringExtra(RECV_ACTION_DATE);
                QrBean qrBean = JSON.parseObject(data, QrBean.class);
                ApiBll.getInstance().sendQR(qrBean);
            }
        });

        addLocalTask(getLocalBillActionType(), new CallBackDo() {
            @Override
            public void callBack(Intent intent) throws Error, Exception {
                String data = intent.getStringExtra(RECV_ACTION_DATE);
                QrBean qrBean = JSON.parseObject(data, QrBean.class);
                ApiBll.getInstance().payQR(qrBean);
            }
        });
    }


    /**
     * 添加本地广播任务
     *
     * @param actionType
     * @param callBackDo
     */
    public void addLocalTask(String actionType, CallBackDo callBackDo) {
        ReceiverMain.mLocalTaskMap.put(actionType, callBackDo);
    }


    /**
     * 通用的创建二维码的广播方式
     *
     * @param money
     * @param mark
     */
    public void creatQrTask(Integer money, String mark) {
        Intent broadCastIntent = new Intent();
        broadCastIntent.setAction(getRemoteAction());
        broadCastIntent.putExtra(RECV_ACTION_TYPE, getRemoteQrActionType());
        broadCastIntent.putExtra("money", formatMoneyToYuan(money + ""));
        broadCastIntent.putExtra("mark", mark);
        HKApplication.app.sendBroadcast(broadCastIntent);
    }


    /**
     * 通用的查订单的广播发送方式
     */
    public void checkBill() {
        Intent broadCastIntent = new Intent();
        broadCastIntent.setAction(getRemoteBillActionType());
        HKApplication.app.sendBroadcast(broadCastIntent);
    }
}
