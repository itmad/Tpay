package com.sjk.tpay.po;

import com.alibaba.fastjson.JSON;
import com.sjk.tpay.utils.SaveUtils;


/**
 * @ Created by Dlg
 * @ <p>TiTle:  Configer</p>
 * @ <p>Description: 用户的首页的配置Bean，单例模式类</p>
 * @ date:  2018/9/21
 * @ QQ群：524901982
 */
public class Configer {

    private static Configer mConfiger;

    private String url = "http://127.0.0.1/";

    /**
     * 长度为8位，和服务端要设置为一样
     */
    private String token = "";

    /**
     * 服务器phone.php文件的真实文件名，改了的话，别人不方便恶意去访问
     */
    private String page = "phone.php";

    /**
     * 白天普通情况下每多少毫秒检测一次
     */
    private Integer delay_nor = 5000;

    /**
     * 夜间00:00-7:00,每多少秒检测一次
     */
    private Integer delay_slow = 15000;


    public synchronized static Configer getInstance() {
        if (mConfiger == null) {
            mConfiger = new SaveUtils().getJson(SaveUtils.BASE, Configer.class);
            if (mConfiger == null) {
                mConfiger = new Configer();
            }
        }
        return mConfiger;
    }

    public String getUrl() {
        return url == null ? "" : url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getToken() {
        return token == null ? "" : token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPage() {
        return page == null ? "" : page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public Integer getDelay_nor() {
        return delay_nor == null ? 0 : delay_nor;
    }

    public void setDelay_nor(Integer delay_nor) {
        this.delay_nor = delay_nor;
    }

    public Integer getDelay_slow() {
        return delay_slow == null ? 0 : delay_slow;
    }

    public void setDelay_slow(Integer delay_slow) {
        this.delay_slow = delay_slow;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
