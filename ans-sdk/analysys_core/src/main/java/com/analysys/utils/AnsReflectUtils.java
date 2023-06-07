package com.analysys.utils;

import android.text.TextUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: 反射辅助类，使用getDeclaredXXX方式本类和所有父类到方法和字段
 * @Create: 2019-11-28 11:30
 * @author: hcq
 */
public class AnsReflectUtils {

    /**
     * 反射获取某个类
     *
     * @param name
     * @return
     */
    public static Class<?> getClassByName(String name) {
        Class<?> clz = null;
        try {
            clz = Class.forName(name);
        } catch (Throwable ignore) {
//            ExceptionUtil.exceptionPrint(ignore);
            return null;
        }
        return clz;
    }

    /**
     * 调用无参静态函数
     */
    public static Object invokeStaticMethod(String clzName, String methodName) {
        Class clz;
        try {
            clz = Class.forName(clzName);
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionPrint(ignore);
            return null;
        }
        return invokeStaticMethod(clz, methodName, null, null);
    }

    /**
     * 调用含参静态函数，单参数
     */
    public static Object invokeStaticMethod(String clzName, String methodName, Class paramType, Object paramValue) {
        Class clz;
        try {
            clz = Class.forName(clzName);
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionPrint(ignore);
            return null;
        }
        return invokeMethod(clz, null, methodName, new Class[]{paramType}, new Object[]{paramValue});
    }

    /**
     * 调用含参静态函数
     */
    public static Object invokeStaticMethod(Class<?> clz, String methodName, Class[] paramTypes, Object[] paramValues) {
        return invokeMethod(clz, null, methodName, paramTypes, paramValues);
    }

    /**
     * 调用无参非成员函数
     */
    public static Object invokeMethod(Object obj, String methodName) {
        return invokeMethod(obj, methodName, null, null);
    }

    /**
     * 调用含参成员函数
     */
    public static Object invokeMethod(Object obj, String methodName, Class[] paramTypes, Object[] paramValues) {
        return invokeMethod(null, obj, methodName, paramTypes, paramValues);
    }

    /**
     * 根据名字调用成员函数
     */
    public static Object invokeMethodByName(Object obj, String methodName, Object[] paramValues) {
        if (obj == null || TextUtils.isEmpty(methodName)) {
            return null;
        }
        Method method = null;
        Class clz = obj.getClass();
        while (clz != null && method == null) {
            try {
                Method[] methods = clz.getDeclaredMethods();
                for (Method mth : methods) {
                    if (methodName.equals(mth.getName())) {
                        method = mth;
                        break;
                    }
                }
            } catch (Throwable ignore) {
                ExceptionUtil.exceptionPrint(ignore);
            }
            if (clz == Object.class) {
                break;
            }
            if (method == null) {
                clz = clz.getSuperclass();
            }
        }
        if (method == null) {
            return null;
        }
        try {
            method.setAccessible(true);
            return method.invoke(obj, paramValues);
        } catch (Throwable ignore) {
        }
        return null;
    }

    /**
     * 查找函数
     */
    public static Method findMethod(Class<?> clz, String methodName, Class[] paramTypes) {
        Method method = null;
        while (clz != null && method == null) {
            try {
                method = clz.getDeclaredMethod(methodName, paramTypes);
            } catch (Throwable ignore) {
            }
            if (clz == Object.class) {
                break;
            }
            if (method == null) {
                clz = clz.getSuperclass();
            }
        }
        return method;
    }

    private static Object invokeMethod(Class<?> clz, Object obj, String methodName, Class[] paramTypes, Object[] paramValues) {
        if (clz == null) {
            if (obj != null) {
                clz = obj.getClass();
            } else {
                return null;
            }
        }
        Method method = findMethod(clz, methodName, paramTypes);
        if (method == null) {
            return null;
        }
        try {
            method.setAccessible(true);
            return method.invoke(obj, paramValues);
        } catch (Throwable ignore) {
        }
        return null;
    }

    /**
     * 获取静态变量
     */
    public static Object getStaticField(Class<?> clz, String fieldName) {
        return getField(clz, null, fieldName);
    }

    /**
     * 获取成员变量
     */
    public static Object getField(Object obj, String fieldName) {
        return getField(null, obj, fieldName);
    }

    private static Object getField(Class<?> clz, Object obj, String fieldName) {
        if (clz == null) {
            if (obj != null) {
                clz = obj.getClass();
            } else {
                return null;
            }
        }
        Field field = findField(clz, fieldName);
        if (field == null) {
            return null;
        }
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionPrint(ignore);
        }
        return null;
    }

    private static Field findField(Class<?> clz, String fieldName) {
        Field field = null;
        while (clz != null && field == null) {
            try {
                field = clz.getDeclaredField(fieldName);
            } catch (Throwable ignore) {
            }
            if (clz == Object.class) {
                break;
            }
            if (field == null) {
                clz = clz.getSuperclass();
            }
        }
        return field;
    }

    /**
     * 设置静态变量
     */
    public static void setStaticField(Class clz, String fieldName, Object fieldValue) {
        setField(clz, null, fieldName, fieldValue);
    }

    /**
     * 设置成员变量
     */
    public static boolean setField(Object obj, String fieldName, Object fieldValue) {
        return setField(null, obj, fieldName, fieldValue);
    }

    private static boolean setField(Class clz, Object obj, String fieldName, Object fieldValue) {
        if (clz == null) {
            if (obj != null) {
                clz = obj.getClass();
            } else {
                return false;
            }
        }
        Field field = findField(clz, fieldName);
        if (field == null) {
            return false;
        }
        try {
            field.setAccessible(true);
            field.set(obj, fieldValue);
            return true;
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionPrint(ignore);
        }
        return false;
    }

    public static boolean isSubClass(String[] parentClzArr, Class<?> clz) {
        if (parentClzArr == null || clz == null) {
            return false;
        }
        for (String clzName : parentClzArr) {
            try {
                Class clzParent = Class.forName(clzName);
                if (clzParent.isAssignableFrom(clz)) {
                    return true;
                }
            } catch (Throwable ignore) {
                ExceptionUtil.exceptionPrint(ignore);
            }
        }
        return false;
    }

    public static boolean isSubClass(Class<?> parentClz, String clzName) {
        if (parentClz == null || clzName == null) {
            return false;
        }
        try {
            Class clz = Class.forName(clzName);
            if (parentClz.isAssignableFrom(clz)) {
                return true;
            }
        } catch (Throwable ignore) {
        }
        return false;
    }
}
