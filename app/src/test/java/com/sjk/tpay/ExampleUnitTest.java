package com.sjk.tpay;

import com.alibaba.fastjson.JSON;
import com.sjk.tpay.po.AliBillList;
import com.sjk.tpay.po.QrBean;
import com.sjk.tpay.utils.StrEncode;

import org.junit.Test;

import java.util.Date;
import java.util.List;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
//        assertEquals(4, 2 + 2);

        System.out.println(StrEncode.encoderByDES("bswv54dfsgdfsrtgt4egfgfgtygfg", "DXV83nmdfe3"));
    }


}