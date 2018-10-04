package com.sjk.tpay.request;

import android.support.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.sjk.tpay.po.BaseMsg;
import com.sjk.tpay.po.Configer;
import com.sjk.tpay.utils.StrEncode;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * @ Created by Dlg
 * @ <p>TiTle:  FastJsonRequest</p>
 * @ <p>Description: 和服务器HTTP请求的返回统一构造类
 * @ 统一设置好超时和重试次数，自动添加token和返回序列化好的BaseMsg</p>
 * @ date:  2018/9/30
 * @ QQ群：524901982
 */
public class FastJsonRequest extends JsonRequest<BaseMsg> {

    public FastJsonRequest(String url, Response.Listener<BaseMsg> listener, @Nullable Response.ErrorListener errorListener) {
        super(Method.GET, url, null, listener, errorListener);
        setRetryPolicy(new DefaultRetryPolicy(5000, 0, 0));
    }


    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("token", StrEncode
                .encoderByDES(System.currentTimeMillis() + "|" + getUrl()
                        , Configer.getInstance().getToken()));
        headers.putAll(super.getHeaders());
        return headers;
    }

    @Override
    protected Response<BaseMsg> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            return Response.success(
                    JSON.parseObject(jsonString, BaseMsg.class), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }


}