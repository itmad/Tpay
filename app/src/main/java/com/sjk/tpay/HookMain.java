package com.sjk.tpay;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.sjk.tpay.utils.LogUtils;

import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @ Created by Dlg
 * @ <p>TiTle:  HookMain</p>
 * @ <p>Description: Xposed的唯一Hook入口</p>
 * @ date:  2018/09/25
 * @ QQ群：524901982
 */
public class HookMain implements IXposedHookLoadPackage {

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam)
            throws Throwable {
        if (lpparam.appInfo == null || (lpparam.appInfo.flags & (ApplicationInfo.FLAG_SYSTEM |
                ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
            return;
        }
        final String packageName = lpparam.packageName;
        final String processName = lpparam.processName;


        for (HookBase hookBase : HookList.getInstance().getmListHook()) {
            //下面的hookCountIndex为2是在vxp里的值，如果手机已root是在xp里运行请改为1，然后把我这行中文删除即可
            hookBase.hook(packageName, processName, 1);
            //LogUtils.show("很多人在倒卖这套系统，大家请不要上当！QQ937013765");
        }
    }
}
