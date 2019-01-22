package com.sjk.tpay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sjk.tpay.imp.CallBackDo;
import com.sjk.tpay.utils.LogUtils;

import java.util.HashMap;
import java.util.Set;


/**
 * @ Created by Dlg
 * @ <p>TiTle:  ReceiverMain</p>
 * @ <p>Description: 当HOOK之后的处理结果，只能用此广播来接受，不然很多数据不方便共享的</p>
 * @ date:  2018/09/22
 * @ QQ群：524901982
 */
public class ReceiverMain extends BroadcastReceiver {
    private static String lastMsg = "";//防止重启接收广播，一定要用static

    //本地广播的任务列表
    public static HashMap<String, CallBackDo> mLocalTaskMap = new HashMap<>();


    @Override
    public void onReceive(Context context, Intent intent) {
        String data = intent.getStringExtra(HookBase.RECV_ACTION_DATE) + intent.getStringExtra(HookBase.RECV_ACTION_TYPE);
        if (lastMsg.contentEquals(data)) {
            return;
        } else {
            lastMsg = data;
        }

        try {
            String type = intent.getStringExtra(HookBase.RECV_ACTION_TYPE);
            LogUtils.show("onReceive：" + type + "|" + mLocalTaskMap);
            Set<String> set = mLocalTaskMap.keySet();
            for (String str : set) {
                if (type.contentEquals(str)) {
                    mLocalTaskMap.get(str).callBack(intent);
                    return;
                }
            }
        } catch (Error | Exception e) {
            LogUtils.show("ReceiverMain错误：" + e.getMessage());
        }
    }

}
