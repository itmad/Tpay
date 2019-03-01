package com.sjk.tpay.utils;

import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflecUtils {

    public static Field findField(Class clazz, Class<?> fieldClazz, int offset, boolean allFields) {
        List<Field> fields = Arrays.asList(clazz.getDeclaredFields());
        if (allFields) {
            Class tempClass = clazz.getSuperclass();
            Field[] list;
            while (tempClass != null) {//当父类为null的时候说明到达了最上层的父类(Object类).
                list = tempClass.getDeclaredFields();
                tempClass = tempClass.getSuperclass(); //得到父类,然后赋给自己
                if (list == null || list.length < 1) {
                    continue;
                }
                fields.addAll(Arrays.asList(list));
            }
        }

        int count = 0;
        for (Field field : fields) {
            if (field.getType().equals(fieldClazz)) {
                if (count == offset) {
                    field.setAccessible(true);
                    return field;
                }
                count++;
            }
        }
        return null;
    }

}
