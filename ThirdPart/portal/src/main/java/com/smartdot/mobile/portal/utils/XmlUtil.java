package com.smartdot.mobile.portal.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.v4.app.Fragment;

import com.smartdot.mobile.portal.bean.TabBean;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 解析Xml的工具类 Created by zhangt on 2016/7/4.
 */
public class XmlUtil {

    private Context mContext;

    /**
     * xml文件名
     */
    private static String xmlFileName;

    /**
     * 返回值
     */
    private static String value = null;

    /**
     * 构造方法
     *
     * @param mContext
     * @param xmlFileName
     *            xml文件名
     */
    public XmlUtil(Context mContext, String xmlFileName) {
        this.mContext = mContext;
        this.xmlFileName = xmlFileName;
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param key
     * @param defaultObject
     * @return
     */
    public Object getXmlParam(String key, Object defaultObject) {
        String type = defaultObject.getClass().getSimpleName();
        AssetManager assetManager = mContext.getAssets();
        Document document;
        SAXReader sr = new SAXReader();
        // 通过read方法读取一个文件 转换成Document对象
        try {
            InputStream input = assetManager.open(xmlFileName);
            document = sr.read(input);
            // 获取根节点元素对象
            Element node = document.getRootElement();
            listNodesUserType(node, key);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (StringUtils.isNull(value)) {
            return defaultObject;
        }

        switch (type) {
        case "String":
            return value;
        case "Integer":
            return Integer.parseInt(value);
        case "Boolean":
            return value.equals("true") ? true : false;
        case "Float":
            return Float.parseFloat(value);
        case "Long":
            return Long.parseLong(value);
        default:
            return defaultObject;
        }
    }

    /**
     * 遍历当前节点元素下面的所有(元素的)子节点
     *
     * @param node
     *            当前节点
     * @param key
     *            key值
     */
    private void listNodesUserType(Element node, String key) {
        List<Attribute> list = node.attributes();
        for (Attribute attr : list) {
            if (attr.getValue().equals(key)) {
                if (!(node.getTextTrim().equals(""))) {
                    value = node.getText();
                }
                return;
            }
        }
        // 当前节点下面子节点迭代器
        Iterator<Element> it = node.elementIterator();
        // 遍历
        while (it.hasNext()) {
            // 获取某个子节点对象
            Element e = it.next();
            // 对子节点进行遍历
            listNodesUserType(e, key);
        }
    }

    /**
     * 解析xml后获取含有底部导航模块的Fragment集合
     */
    public static List<TabBean> getTabBeans(Context mContext) throws DocumentException {
        Document document;
        AssetManager assetManager = mContext.getAssets();
        SAXReader sr = new SAXReader();
        // 通过read方法读取一个文件 转换成Document对象
        try {
            InputStream input = assetManager.open(xmlFileName);
            document = sr.read(input);
            // 获取根节点元素对象
            Element node = document.getRootElement();
            List<Element> elementList = node.elements();
            List<TabBean> tabList = new ArrayList();
            for (Element e : elementList) {
                TabBean tab = new TabBean();
                tab.setIndex(Integer.parseInt(e.elementText("index")));
                tab.setLabel(e.elementText("label"));
                tab.setPicName(e.elementText("picName"));
                Class clazz = mContext.getApplicationContext().getClassLoader()
                        .loadClass(e.elementText("fragment-class"));
                Fragment owner = (Fragment) clazz.newInstance();
                tab.setF(owner);
                tabList.add(tab);
            }
            for (TabBean tab : tabList) {
                System.out.println("index:" + tab.getIndex() + "\t label:" + tab.getLabel());
            }

            return tabList;
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}