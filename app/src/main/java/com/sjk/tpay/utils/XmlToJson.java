package com.sjk.tpay.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.List;


/**
 * @ Created by Dlg
 * @ <p>TiTle:  XmlToJson</p>
 * @ <p>Description: 把xml转为json的处理，
 * @ 这里对网上代码优化了一下，不然json解析出来全是数组形式很不方便代码书写</p>
 * @ date:  2018/9/22
 * @ QQ群：524901982
 */
public class XmlToJson {

    /**
     * String 转 org.dom4j.Document
     *
     * @param xml
     * @return
     * @throws DocumentException
     */
    public static Document strToDocument(String xml) throws DocumentException {
        return DocumentHelper.parseText(xml);
    }

    /**
     * org.dom4j.Document 转  com.alibaba.fastjson.JSONObject
     *
     * @param xml
     * @return
     * @throws DocumentException
     */
    public static JSONObject documentToJSONObject(String xml) throws DocumentException {
        return elementToJSONObject(strToDocument(xml).getRootElement());
    }

    /**
     * org.dom4j.Element 转  com.alibaba.fastjson.JSONObject
     *
     * @param node
     * @return
     */
    public static JSONObject elementToJSONObject(Element node) {
        JSONObject result = new JSONObject();
        // 当前节点的名称、文本内容和属性
        List<Attribute> listAttr = node.attributes();// 当前节点的所有属性的list
        for (Attribute attr : listAttr) {// 遍历当前节点的所有属性
            result.put(attr.getName(), attr.getValue());
        }
        // 递归遍历当前节点所有的子节点
        List<Element> listElement = node.elements();// 所有一级子节点的list
        if (!listElement.isEmpty()) {
            for (Element e : listElement) {// 遍历所有一级子节点
                if (e.attributes().isEmpty() && e.elements().isEmpty()) // 判断一级节点是否有属性和子节点
                    result.put(e.getName(), e.getTextTrim());// 沒有则将当前节点作为上级节点的属性对待
                else {
                    if (!result.containsKey(e.getName())) { // 判断父节点是否存在该一级节点名称的属性
                        if (getKeyCount(e.getName(), listElement) > 1) {
                            result.put(e.getName(), new JSONArray());// 没有则创建
                        } else {
                            result.put(e.getName(), elementToJSONObject(e));// 没有则创建
                            continue;
                        }
                    }
                    ((JSONArray) result.get(e.getName())).add(elementToJSONObject(e));// 将该一级节点放入该节点名称的属性对应的值中
                }
            }
        }
        return result;
    }


    /**
     * 用于较准确的判断这个节点是数组还是类，不然xml会全被解析为数组结构
     *
     * @param key
     * @param listElement
     * @return 返回这个结点，key名称的子节点有几个，如果大于1个就是数组
     */
    private static int getKeyCount(String key, List<Element> listElement) {
        int count = 0;
        for (Element e : listElement) {// 遍历所有一级子节点
            if (e.getName().equals(key)) {
                count++;
            }
        }
        return count;
    }
}
