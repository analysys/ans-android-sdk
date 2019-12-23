package com.analysys.utils;

import android.text.TextUtils;

import android.annotation.SuppressLint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * Description: 数值转换工具类
 * Author: fengzeyuan
 * Date: 2019-09-30 14:48
 * Version: 1.0
 */
public class NumberFormat {

    private static String[] units = {"", "十", "百", "千", "万", "十万", "百万", "千万", "亿",
            "十亿", "百亿", "千亿", "万亿"};
    private static char[] numArray = {'零', '一', '二', '三', '四', '五', '六', '七', '八', '九'};

    /**
     * 得到一个Double 类型的数 保留小数点后两位
     *
     * @param d
     * @return
     */
    public static double getDouble(Double d) {
        try {
            BigDecimal b = BigDecimal.valueOf(d);
            return b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        } catch (Exception e) {
            ExceptionUtil.exceptionThrow(e);
        }
        return 0.00;
    }

    /**
     * String 转换为 int
     *
     * @param num
     * @return
     */
    public static int getInt(String num) {
        if (TextUtils.isEmpty(num) || !TextUtils.isDigitsOnly(num)) {
            return 0;
        }
        try {
            return Integer.parseInt(num);
        } catch (Exception e) {
            ExceptionUtil.exceptionThrow(e);
        }
        return 0;
    }

    /**
     * String 转为 int(带默认值)
     *
     * @param intStr
     * @param defValue
     * @return
     */
    public static int convertToint(String intStr, int defValue) {
        if (TextUtils.isEmpty(intStr) || !TextUtils.isDigitsOnly(intStr)) {
            return defValue;
        }
        try {
            return Integer.parseInt(intStr);
        } catch (Exception e) {
            ExceptionUtil.exceptionThrow(e);
        }
        return defValue;
    }

    /**
     * String 转为 long(带默认值)
     *
     * @param longStr
     * @param defValue
     * @return
     */
    public static long convertTolong(String longStr, long defValue) {
        try {
            return Long.parseLong(longStr);
        } catch (Exception e) {
            ExceptionUtil.exceptionThrow(e);
        }
        return defValue;
    }

    /**
     * String 转为 float(带默认值)
     *
     * @param fStr
     * @param defValue
     * @return
     */
    public static float convertTofloat(String fStr, float defValue) {
        try {
            return Float.parseFloat(fStr);
        } catch (Exception e) {
            ExceptionUtil.exceptionThrow(e);
        }
        return defValue;
    }

    /**
     * String 转为 double(带默认值)
     *
     * @param dStr
     * @param defValue
     * @return
     */
    public static double convertTodouble(String dStr, double defValue) {
        if (TextUtils.isEmpty(dStr)) {
            return 0.0;
        }
        try {
            return Double.parseDouble(dStr);
        } catch (Exception e) {
            ExceptionUtil.exceptionThrow(e);
        }
        return defValue;
    }

    /**
     * String 转为 Integer
     *
     * @param intStr
     * @return
     */
    public static int convertToInteger(String intStr) {
        if (TextUtils.isEmpty(intStr) || !TextUtils.isDigitsOnly(intStr)) {
            return 0;
        }
        try {
            return Integer.parseInt(intStr);
        } catch (Exception e) {
            ExceptionUtil.exceptionThrow(e);
        }
        return 0;
    }

    /**
     * String 转为 long
     *
     * @param longStr
     * @return
     */
    public static long convertToLong(String longStr) {
        try {
            return Long.parseLong(longStr);
        } catch (Exception e) {
            ExceptionUtil.exceptionThrow(e);
        }
        return 0;
    }

    /**
     * String 转为 float
     *
     * @param fStr
     * @return
     */
    public static float convertToFloat(String fStr) {
        try {
            return Float.parseFloat(fStr);
        } catch (Exception e) {
            ExceptionUtil.exceptionThrow(e);
        }
        return 0;
    }

    /**
     * String 转为 double
     *
     * @param dStr
     * @return
     */
    public static double convertToDouble(String dStr) {
        try {
            return Double.parseDouble(dStr);
        } catch (Exception e) {
            ExceptionUtil.exceptionThrow(e);
        }
        return 0;
    }

    /**
     * 转换至金钱模式，保留小数点后2位
     *
     * @param d
     * @return
     */
    public static String castPoint2(double d) {
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
        String dString = df.format(d);
        if (dString.charAt(0) == '.') {
            return "0" + dString;
        }
        return dString;
    }

    /**
     * 转换至金钱模式，保留小数点后2位
     *
     * @param d
     * @return
     */

    public static String castMoneyFormat(double d) {
        String str = String.valueOf(d);
        String[] split = str.split("\\.");
        if (split[1].length() == 1) {
            return str + "0";
        } else {
            return str;
        }
    }

    /**
     * 将double的字符串格式化后转出
     * 格式类型：小数点后保留两位小数。
     *
     * @param doubleString double类型的字符串
     * @return 格式化后的字符串
     */
    public static CharSequence castMoneyFormat(String doubleString) {
        try {
            // 转换成double 类型，保留小数点后两位小数。
            BigDecimal priceDecimal = new BigDecimal(doubleString).multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal[] bigDecimals = priceDecimal.divideAndRemainder(new BigDecimal("100"));

            int priceInt = bigDecimals[0].setScale(2, RoundingMode.HALF_UP).intValue();
            int priceRemainder = bigDecimals[1].setScale(2, RoundingMode.HALF_UP).intValue();

            return String.format(Locale.CHINA, "%d.%02d", priceInt, priceRemainder);
        } catch (Exception e) {
            return doubleString;
        }
    }

    /**
     * 转换至金钱模式，保留小数点后2位
     *
     * @param doubleString double类型的字符串
     * @param rate         转换率，如果doubleString 单位是元，rate 填写1。如果doubleString 单位是分，rate
     *                     填写100
     * @return
     */

    @SuppressLint("DefaultLocale")
    public static String castMoneyFormat(String doubleString, int rate) {
        try {
            double tempDoubleString = Double.parseDouble(doubleString);
            tempDoubleString = getDouble(tempDoubleString / rate);
            return String.format("%.2f", tempDoubleString);
        } catch (Exception e) {
            return doubleString;
        }
    }

    /**
     * 字符串转Double
     *
     * @param doubleString double类型的字符串
     * @return
     */
    public static double strToDouble(String doubleString) {
        try {
            return Double.parseDouble(doubleString);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 字符串转Double，四舍五入后保留两位小数
     *
     * @param doubleString double类型的字符串
     * @return
     */
    public static double strToDouble_2decimal(String doubleString) {
        try {
            double parseDouble = Double.parseDouble(doubleString);
            return getDouble(parseDouble);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 去除数字多余的零
     * <p>
     * 00012340--->12340 00012.340-->12.34 1.2e3------>1200
     *
     * @param number
     * @return
     */
    public static String getPrettyNumber(String number) {
        return BigDecimal.valueOf(Double.parseDouble(number)).stripTrailingZeros().toPlainString();
    }

    /**
     * 数字转汉字
     *
     * @param num
     * @return
     */
    public static String formatInteger(int num) {
        char[] val = String.valueOf(num).toCharArray();
        int len = val.length;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            String m = val[i] + "";
            int n = Integer.valueOf(m);
            boolean isZero = n == 0;
            String unit = units[(len - 1) - i];
            if (isZero) {
                if ('0' == val[i - 1]) {
                    // not need process if the last digital bits is 0
                    continue;
                } else {
                    // no unit for 0
                    sb.append(numArray[n]);
                }
            } else {
                sb.append(numArray[n]);
                sb.append(unit);
            }
        }
        return sb.toString();
    }
}
