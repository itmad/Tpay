package com.sjk.tpay;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.sjk.tpay.po.Configer;
import com.sjk.tpay.utils.LogUtils;

import java.util.Calendar;

/**
 * @ Created by Dlg
 * @ <p>TiTle:  ServiceProtect</p>
 * @ <p>Description: 守护ServiceMain服务，防止出问题，双服务守护，没有用双进程哈。</p>
 * @ date:  2018/9/29
 * @ QQ群：524901982
 */
public class ServiceProtect extends Service {


    @Override
    public void onCreate() {
        super.onCreate();
        handler.sendEmptyMessage(0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (System.currentTimeMillis() - ServiceMain.mLastQueryTime > 60000) {
                startService(new Intent(ServiceProtect.this.getApplicationContext(), ServiceMain.class));
            }
            //0-7点的时候就慢速轮循
            handler.sendEmptyMessageDelayed(0, 50000);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeMessages(0);
        LogUtils.show("Protect服务被杀死");
        Intent intent = new Intent(this.getApplicationContext(), ServiceProtect.class);
        this.startService(intent);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
