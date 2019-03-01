package com.sjk.tpay;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sjk.tpay.po.Configer;
import com.sjk.tpay.utils.PayUtils;
import com.sjk.tpay.utils.SaveUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.sjk.tpay.ServiceMain.mIsRunning;

/**
 * @ Created by Dlg
 * @ <p>TiTle:  ActMain</p>
 * @ <p>Description: 启动首页，直接在xml绑定的监听
 * @ 其实我是不推荐这种绑定方式的，哈哈哈，为了项目简洁点还是就这样吧</p>
 * @ date:  2018/09/11
 * @ QQ群：524901982
 */
public class ActMain extends AppCompatActivity {

    private EditText mEdtUrl;

    private EditText mEdtToken;

    private EditText mEdtPage;

    private EditText mEdtTimeNor;

    private EditText mEdtTimeSlow;

    private Button mBtnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        mEdtUrl = ((TextInputLayout) findViewById(R.id.edt_act_main_url)).getEditText();
        mEdtToken = ((TextInputLayout) findViewById(R.id.edt_act_main_token)).getEditText();
        mEdtPage = ((TextInputLayout) findViewById(R.id.edt_act_main_page)).getEditText();
        mEdtTimeNor = ((TextInputLayout) findViewById(R.id.edt_act_main_time_nor)).getEditText();
        mEdtTimeSlow = ((TextInputLayout) findViewById(R.id.edt_act_main_time_slow)).getEditText();
        mBtnSubmit = findViewById(R.id.btn_submit);
        ((TextView) findViewById(R.id.txt_version)).setText("Ver：" + BuildConfig.VERSION_CODE);


        mBtnSubmit.setText(mIsRunning ? "停止服务" : "确认配置并启动");
        getPermissions();
    }

    /**
     * 切换APP服务的运行状态
     *
     * @return
     */
    private boolean changeStatus() {
        mIsRunning = !mIsRunning;
        mBtnSubmit.setText(mIsRunning ? "停止服务" : "确认配置并启动");
        return mIsRunning;
    }

    /**
     * 点确认配置的操作
     *
     * @param view
     */
    public void clsSubmit(View view) {
        if (!changeStatus()) {
            return;
        }

        mEdtUrl.setText(mEdtUrl.getText().toString().trim());
        mEdtToken.setText(mEdtToken.getText().toString().trim());
        if (mEdtUrl.length() < 2 || mEdtToken.length() < 1
                || mEdtTimeNor.length() < 2 || mEdtTimeSlow.length() < 2) {
            Toast.makeText(ActMain.this, "请先输入正确配置！", Toast.LENGTH_SHORT).show();
            changeStatus();
            return;
        }
        if (mEdtToken.length() != 8) {
            Toast.makeText(ActMain.this, "密钥只能为八位数字或字符！", Toast.LENGTH_SHORT).show();
            changeStatus();
            return;
        }
        if (!mEdtUrl.getText().toString().endsWith("/")) {
            mEdtUrl.setText(mEdtUrl.getText().toString() + "/");//保持以/结尾的网址
        }


        //下面开始获取最新配置并启动服务。
        Configer.getInstance()
                .setUrl(mEdtUrl.getText().toString());
        Configer.getInstance()
                .setToken(mEdtToken.getText().toString());
        Configer.getInstance()
                .setPage(mEdtPage.getText().toString());
        Configer.getInstance()
                .setDelay_nor(Integer.valueOf(mEdtTimeNor.getText().toString()));
        Configer.getInstance()
                .setDelay_slow(Integer.valueOf(mEdtTimeSlow.getText().toString()));
        //保存配置
        new SaveUtils().putJson(SaveUtils.BASE, Configer.getInstance()).commit();


        //有的手机就算已经静态注册服务还是不行启动，我再手动启动一下吧。
        startService(new Intent(this, ServiceMain.class));
        startService(new Intent(this, ServiceProtect.class));
    }

    private PackageInfo getPackageInfo(String packageName) {
        PackageInfo pInfo = null;
        try {
            //通过PackageManager可以得到PackageInfo
            PackageManager pManager = getPackageManager();
            pInfo = pManager.getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS);
            return pInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pInfo;
    }

    /**
     * 测试微信获取二维码的功能
     *
     * @param view
     */
    public void clsWechatPay(View view) {
        String time = System.currentTimeMillis() / 1000 + "";
        Toast.makeText(this, "只要能打开收款页面即表示成功，并不会输入和生成二维码", Toast.LENGTH_SHORT).show();
        HookWechat.getInstance().creatQrTask(12, "test" + time);
    }


    /**
     * 测试支付宝获取二维码的功能
     *
     * @param view
     */
    public void clsAlipayPay(View view) {
        String time = System.currentTimeMillis() / 1000 + "";
        Toast.makeText(this, "此功能可以加群获取支付宝版哦", Toast.LENGTH_SHORT).show();
        HookAlipay.getInstance().creatQrTask(12, "test" + time);
    }

    /**
     * 添加QQ群，保留版权哦。
     *
     * @param view
     */
    public void clsAddQq(View view) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D"
                + "oRC0TKyL8gc3a2gwJVxa4J9QN9IIqFSv"));
        try {
            startActivity(intent);
        } catch (Exception ignore) {
            Toast.makeText(this, "请先安装QQ哦才能加群~", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 当获取到权限后才操作的事情
     */
    private void onPermissionOk() {
        mEdtUrl.setText(Configer.getInstance().getUrl());
        mEdtToken.setText(Configer.getInstance().getToken());
        mEdtPage.setText(Configer.getInstance().getPage());
        mEdtTimeNor.setText(Configer.getInstance().getDelay_nor() + "");
        mEdtTimeSlow.setText(Configer.getInstance().getDelay_slow() + "");
        if (getIntent().hasExtra("auto")) {
            clsSubmit(null);
        }
    }

    /**
     * 获取权限。。有些手机很坑，明明是READ_PHONE_STATE权限，却问用户是否允许拨打电话，汗。
     */
    private void getPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onPermissionOk();
            return;
        }
        List<String> sa = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请READ_PHONE_STATE权限。。。。
            sa.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            sa.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            sa.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (sa.size() < 1) {
            onPermissionOk();
            return;
        }
        ActivityCompat.requestPermissions(this, sa.toArray(new String[]{}), 1);
    }


    /**
     * 获取到权限后的回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //获取到了权限之后才可以启动xxxx操作。
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "部分权限未开启\n可能部分功能暂时无法工作。", Toast.LENGTH_SHORT).show();
                //如果被永久拒绝。。。那只有引导跳权限设置页
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!shouldShowRequestPermissionRationale(permissions[i])) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName())); // 根据包名打开对应的设置界面
                        startActivity(intent);
                        onPermissionOk();
                        return;
                    }
                }
                break;
            }
        }
        onPermissionOk();
    }
}
