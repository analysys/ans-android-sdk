package com.analysys.utils;

import org.json.JSONArray;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-08-14 20:29
 * @Author: Wang-X-C
 */
public class ParameterCheckTest {

    @Test
    public void checkEventName() {
        String value = "aaaaaa";
        ParameterCheck.checkEventName(value);

        String value2 = "中文测试";
        ParameterCheck.checkEventName(value2);

        String value3 = null;
        for (int i = 0; i < 100; i++) {
            value3 += i;
        }
        ParameterCheck.checkEventName(value3);
    }

    @Test
    public void checkKey() {
        ParameterCheck.checkKey(null);
        String value = null;
        for (int i = 0; i < 100; i++) {
            value += i;
        }
        ParameterCheck.checkKey(value);
        String value1 = "-aaaaaa";
        ParameterCheck.checkKey(value1);
        ParameterCheck.checkKey("xwhat");
    }

    @Test
    public void checkValue() throws Throwable{
        ParameterCheck.checkValue(null);

        ParameterCheck.checkValue(true);

        String value = "aaaaa";
        ParameterCheck.checkValue(value);

        String value2 = null;
        for (int i = 0; i < 8199; i++) {
            value2+="i";
        }
        ParameterCheck.checkValue(value2);

        List<String> list = new ArrayList<>();
        list.add("test");
        ParameterCheck.checkValue(list);

        String[] array = {"test1","test2"};
        ParameterCheck.checkValue(array);

        JSONArray jar = new JSONArray();
        jar.put("test1");
        jar.put("test2");
        ParameterCheck.checkValue(jar);

        Map<String,Object> map = new HashMap<String, Object>();
        ParameterCheck.checkValue(map);
    }
}