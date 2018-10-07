package com.sjk.tpay.po;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @ Created by Dlg
 * @ <p>TiTle:  BaseMsg</p>
 * @ <p>Description: 所有自己服务器数据请求的统一返回基类</p>
 * @ date:  2018/9/21
 * @ QQ群：524901982
 */
public class BaseMsg {

    private String message;

    private Integer status = 0;

    private Object data;

    public String getMessage() {
        return message == null ? "" : message;
    }

    public String getMessage(String defaultmsg) {
        return message == null ? defaultmsg : message;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }


    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return 返回非1就是错误代码，这里不是HttpStatus，是自己服务器返回的status哦。
     */
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * @return @NotNull 不会返回null的，就懒得做判断了。
     */
    public Object getData() {
        if (data == null) {
            data = new Object();
        }
        return data;
    }


    /**
     * @param clazz
     * @param <T>
     * @return 失败返回null
     */
    public <T> T getData(Class<T> clazz) {
        if (data == null) {
            return null;
        }
        return JSONObject.parseObject(JSONObject.toJSONString(data), clazz);
    }


    /**
     * 如果data里是数组，直接这样获取
     *
     * @param clazz
     * @param <T>
     * @return 失败返回null
     */
    public <T> List<T> getDatas(Class<T> clazz) {
        if (data == null) {
            return null;
        }
        return JSONArray.parseArray(JSONObject.toJSONString(data), clazz);
    }


    public void setData(Object data) {
        this.data = data;
    }

}
