package com.sjk.tpay;

import com.alibaba.fastjson.JSON;
import com.sjk.tpay.po.QrBean;
import com.sjk.tpay.utils.ReflecUtils;
import com.sjk.tpay.utils.StrEncode;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
//        assertEquals(4, 2 + 2);
        Method f = ReflecUtils.findMethod(StrEncode.class, null,void.class
                , String.class, String.class);
        if (f != null)
            System.out.println(f.getName());
//        System.out.println(JSON.toJSONString(QrBean.class.getDeclaredFields()));


    }


}