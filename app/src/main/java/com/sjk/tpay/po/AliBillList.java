package com.sjk.tpay.po;

import com.alibaba.fastjson.JSON;

import java.util.Date;

/**
 * @ Created by Dlg
 * @ <p>TiTle:  AliBillList</p>
 * @ <p>Description: 支付宝的订单Bean类 ，但其实很多项对我们没什么用，就注释掉没必要记录了</p>
 * @ date:  2018/9/29
 * @ QQ群：524901982
 */
public class AliBillList {

    private String consumerName;
    //    private String id;
//    private String tradeStatus;
//    private String gmtCreate;
//    private String tradeRefundAmount;
    private String tradeNo;
    private Date gmtCreateStamp;
    //    private String tradeTransAmount;
    private Float totalAmount;
//    private String tradeStatusView;
//    private String totalPayedAmount;
//    private String url;


    public String getConsumerName() {
        return consumerName == null ? "" : consumerName;
    }

    public void setConsumerName(String consumerName) {
        this.consumerName = consumerName;
    }

    public String getTradeNo() {
        return tradeNo == null ? "" : tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public Date getGmtCreateStamp() {
        return gmtCreateStamp;
    }

    public void setGmtCreateStamp(Date gmtCreateStamp) {
        this.gmtCreateStamp = gmtCreateStamp;
    }

    public Float getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Float totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
