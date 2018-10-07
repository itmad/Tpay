package com.sjk.tpay.po;

import com.alibaba.fastjson.JSON;


/**
 * @ Created by Dlg
 * @ <p>TiTle:  QrBean</p>
 * @ <p>Description: 这个最基本的二维码信息Bean类了</p>
 * @ date:  2018/9/21
 * @ QQ群：524901982
 */
public class QrBean {

    //以后自己可以添加更多支付方式，没必要用枚举
    public static final String WECHAT = "wechat";
    public static final String ALIPAY = "alipay";


    /**
     * 这个是服务器上此订单的id，这个暂时不加用处
     */
    private Integer id;

    /**
     * 渠道类型
     */
    private String channel;//wechat,alipay

    /**
     * 二维码的金额,单位为分
     */
    private Integer money;

    /**
     * 此而二维码的链接
     */
    private String url;

    /**
     * 二维码的收款方备注
     */
    private String mark_sell;

    /**
     * 二维码的付款方备注
     */
    private String mark_buy;

    /**
     * 订单id
     */
    private String order_id;

    public Integer getId() {
        return id == null ? 0 : id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getChannel() {
        return channel == null ? "" : channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Integer getMoney() {
        return money == null ? 0 : money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public String getUrl() {
        return url == null ? "" : url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMark_sell() {
        return mark_sell == null ? "" : mark_sell;
    }

    public void setMark_sell(String mark_sell) {
        this.mark_sell = mark_sell;
    }

    public String getMark_buy() {
        return mark_buy == null ? "" : mark_buy;
    }

    public void setMark_buy(String mark_buy) {
        this.mark_buy = mark_buy;
    }

    public String getOrder_id() {
        return order_id == null ? "" : order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
