package com.analysys.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 系统工具类
 * @Version: 1.0
 * @Create: 2018/3/7
 * @Author: Wang-X-C
 */

public class CommonUtils {

    /**
     * 应用上线渠道
     */
    public static String getManifestData(Context context, String type) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (!TextUtils.isEmpty(type)) {
                return appInfo.metaData.getString(type);
            }
        } catch (Throwable throwable) {
        }
        return null;
    }

    /**
     * 判断是否 自动采集 mac imei
     */
    public static boolean isAutoCollect(Context context, String type) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (!TextUtils.isEmpty(type)) {
                return appInfo.metaData.getBoolean(type);
            }
        } catch (Throwable throwable) {
        }
        return true;
    }

    /**
     * 获取当前时间,格式 yyyy-MM-dd hh:mm:ss.SSS
     */
    public static String getTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    public static Set<String> toSet(String names) {
        Set<String> set = null;
        if (!isEmpty(names)) {
            set = new HashSet<>();
            if (names.contains("$$")) {
                String[] array = names.split("\\$\\$");
                Collections.addAll(set, array);
            } else {
                set.add(names);
            }
        }
        return set;
    }

    public static String toString(Set<String> set) {
        String names = "";
        if (set != null && !set.isEmpty()) {
            for (String name : set) {
                if (isEmpty(names)) {
                    names = name;
                } else {
                    names += "$$" + name;
                }
            }
        }
        return names;
    }

    /**
     * 字符串转集合
     */
    public static List<String> toList(String names) {
        List<String> list = new ArrayList<>();
        if (!isEmpty(names)) {
            if (names.contains("$$")) {
                String[] array = names.split("\\$\\$");
                Collections.addAll(list, array);
            } else {
                list.add(names);
            }
        }
        return list;
    }

    /**
     * 集合转成字符串
     */
    public static String toString(List<String> list) {
        String names = "";
        if (list != null && !list.isEmpty()) {
            for (String name : list) {
                if (isEmpty(names)) {
                    names = name;
                } else {
                    names += "$$" + name;
                }
            }
        }
        return names;
    }

    /**
     * Json 转 Map
     */
    public static Map<String, Object> jsonToMap(JSONObject jsonObject) {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            Object key = keys.next();
            if (key == null) {
                continue;
            }
            String sKey = key.toString();
            map.put(sKey, jsonObject.optString(sKey));
        }
        return map;
    }

    /**
     * gzip压缩 base64编码
     */
    public static String messageZip(String message) throws IOException {
        String baseMessage = null;
        if (!isEmpty(message)) {
            byte[] gzipMessage = ZipUtils.compressForGzip(message);
            byte[] base64 = Base64.encode(gzipMessage, Base64.DEFAULT);
            baseMessage = new String(base64);
            if (isEmpty(baseMessage)) {
                return "";
            }
        }
        return baseMessage;
    }

    /**
     * base64解码 gzip解压缩
     */
    public static String messageUnzip(String message) {
        if (isEmpty(message)) {
            return null;
        }
        byte[] base64Message = Base64.decode(message, Base64.DEFAULT);
        String gzipMessage = ZipUtils.decompressForGzip(base64Message);
        if (isEmpty(gzipMessage)) {
            return message;
        }
        return gzipMessage;
    }

    /**
     * 获取上传头
     */
    public static String getSpvInfo(Context context) {
        try {
            String appId = getAppKey(context);
            String sdkVersion = Constants.DEV_SDK_VERSION;
            String policyVersion = SharedUtil.getString(context, Constants.SP_SERVICE_HASH,
                    null);

            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String appVersion = info.versionName;
            String spv =
                    Constants.PLATFORM + "|" + appId + "|" + sdkVersion + "|" + policyVersion +
                            "|" + appVersion;
            return new String(Base64.encode(spv.getBytes(), Base64.DEFAULT));
        } catch (Throwable throwable) {
        }
        return null;
    }

    /**
     * 是否首次启动
     */
    public static boolean isFirstStart(Context context) {
        if (!isEmpty(context)) {
            String first = SharedUtil.getString(context, Constants.SP_FIRST_START_TIME,
                    null);
            return isEmpty(first);
        }
        return false;
    }

    /**
     * 获取首次启动时间
     */
    public static String getFirstStartTime(Context context) {
        String firstTime = SharedUtil.getString(context, Constants.SP_FIRST_START_TIME,
                Constants.EMPTY);
        if (isEmpty(firstTime)) {
            firstTime = getTime();
            SharedUtil.setString(context, Constants.SP_FIRST_START_TIME, firstTime);
        }
        return firstTime;
    }

    /**
     * 获取当前日期,格式 yyyy/MM/dd
     */
    public static String getDay() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault());
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    public static String checkUrl(String url) {
        if (isEmpty(url)) {
            return null;
        }
        url = url.trim();
        int lastDex = url.lastIndexOf("/");
        if (lastDex != -1 && lastDex == (url.length() - 1)) {
            return checkUrl(url.substring(0, url.length() - 1));
        }
        return url;
    }

    /**
     * 判断数据是否为空值
     */
    public static boolean isEmpty(Object object) {
        try {
            if (object == null) {
                return true;
            } else if (object instanceof String) {
                String val = object.toString();
                return (TextUtils.isEmpty(val)
                        || TextUtils.isEmpty(val.trim()));
            } else if (object instanceof JSONObject) {
                return ((JSONObject) object).length() < 1;
            } else if (object instanceof JSONArray) {
                return ((JSONArray) object).length() < 1;
            } else if (object instanceof Map) {
                return ((Map) object).isEmpty();
            } else if (object instanceof List) {
                return ((List) object).isEmpty();
            } else if (object instanceof Set) {
                return ((Set) object).isEmpty();
            } else {
                return false;
            }
        } catch (Throwable throwable) {
        }
        return true;
    }

    /**
     * 判断是否为主进程
     */
    public static boolean isMainProcess(Context context) {
        if (context == null) {
            return false;
        }
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = null;
        if (activityManager != null) {
            runningApps = activityManager.getRunningAppProcesses();
        }
        if (runningApps == null) {
            return false;
        }
        String process = "";
        for (ActivityManager.RunningAppProcessInfo proInfo : runningApps) {
            if (proInfo.pid == android.os.Process.myPid()) {
                if (proInfo.processName != null) {
                    process = proInfo.processName;
                }
            }
        }
        return context.getPackageName().equals(process);
    }

    /**
     * 获取网络类型
     */
    public static String networkType(Context context) {
        String netType = "";
        // 检测权限
        if (!checkPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
            return "Unknown";
        }
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = "WIFI";
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {
            int nSubType = networkInfo.getSubtype();
            return String.valueOf(nSubType);
        }
        return netType;
    }

    /**
     * 检测权限
     *
     * @param context    Context
     * @param permission 权限名称
     * @return true:已允许该权限; false:没有允许该权限
     */
    public static boolean checkPermission(Context context, String permission) {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class<?> clazz = Class.forName("android.content.Context");
                Method method = clazz.getMethod("checkSelfPermission", String.class);
                int rest = (Integer) method.invoke(context, permission);
                result = rest == PackageManager.PERMISSION_GRANTED;
            } catch (Throwable throwable) {
                result = false;
            }
        } else {
            PackageManager pm = context.getPackageManager();
            if (pm.checkPermission(permission,
                    context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                result = true;
            }
        }
        return result;
    }

    /**
     * 获取 AppKey
     */
    public static String getAppKey(Context context) {
        try {
            return SharedUtil.getString(context, Constants.SP_APP_KEY, null);
        } catch (Throwable throwable) {
        }
        return null;
    }

    /**
     * 检测当的网络状态
     *
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        if (!checkPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
            return false;
        }
        ConnectivityManager connectivity = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                // 当前网络是连接的
                return networkInfo.getState() == NetworkInfo.State.CONNECTED;
            }
        }
        return false;
    }

    /**
     * 两个json合并,source 参数合并到 dest参数
     */
    public static void mergeJson(final JSONObject source, JSONObject dest) throws JSONException {
        if (isEmpty(source)) {
            return;
        } else if (dest == null) {
            return;
        }
        Iterator<String> keys = source.keys();
        String key;
        while (keys.hasNext()) {
            key = keys.next();
            dest.put(key, source.opt(key));
        }
    }

    /**
     * 读取流
     */
    public static String readStream(InputStream is) {
        StringBuffer sb = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            sb = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
        } catch (Throwable throwable) {
        } finally {
            try {
                is.close();
            } catch (Throwable throwable) {
            }
        }
        return String.valueOf(sb);
    }

    /**
     * 带参数 反射
     */
    public static Object reflexUtils(String classPath, String methodName, Class[] classes,
                                     Object... objects) {
        try {
            Class<?> cl = Class.forName(classPath);
            Method method = cl.getDeclaredMethod(methodName, classes);
            //类中的成员变量为private,必须进行此操作
            method.setAccessible(true);
            Object object = cl.newInstance();
            return method.invoke(object, objects);
        } catch (Throwable throwable) {
        }
        return null;
    }

    /**
     * 无参 反射
     */
    public static Object reflexUtils(String classPath, String methodName) {
        try {
            Class<?> cl = Class.forName(classPath);
            Method method = cl.getDeclaredMethod(methodName);
            //类中的成员变量为private,必须进行此操作
            method.setAccessible(true);
            Object object = cl.newInstance();
            return method.invoke(object);
        } catch (Throwable throwable) {
        }
        return null;
    }

    /**
     * 获取 originalId，优先取distinctId,无值，取UUID
     */
    public static String getDistinctId(Context context) {
        String id = getIdFile(context, Constants.SP_DISTINCT_ID);
        if (isEmpty(id)) {
            id = getIdFile(context, Constants.SP_UUID);
        }
        return id;
    }

    /**
     * 过滤掉value为空的数据
     */
    public static void pushToJSON(JSONObject json, String key, Object value) {
        try {
            if (value == null) {
                return;
            }
            if (value instanceof String) {
                String v = String.valueOf(value);
                if (!TextUtils.isEmpty(v) && !"unknown".equalsIgnoreCase(v)) {
                    if (!json.has(key)) {
                        json.put(key, value);
                    }
                }
            } else {
                json.put(key, value);
            }
        } catch (Throwable throwable) {
        }
    }

    /**
     * 获取上传地址
     * 服务器下发 > 用户设置 > 默认设置
     */
    public static String getUrl(Context context) {
        String url = SharedUtil.getString(context, Constants.SP_SERVICE_URL, null);
        if (!isEmpty(url)) {
            return url;
        }
        url = SharedUtil.getString(context, Constants.SP_USER_URL, null);
        if (!isEmpty(url)) {
            return url;
        }
        return null;
    }

    /**
     * 获取服务器时间计算与设备时间差
     */
    public static void timeCalibration(long time) {
        if (time != 0) {
            Constants.TIME_DIFFERENCE = time - System.currentTimeMillis();
        }
    }

    /**
     * 读取模板
     */
    public static String getMould(Context context, String fileName) {
        if (!TextUtils.isEmpty(fileName)) {
            InputStream inputStream = null;
            try {
                inputStream = context.getResources().getAssets().open(fileName);
                int size = inputStream.available();
                byte[] bytes = new byte[size];
                inputStream.read(bytes);
                return new String(bytes);
            } catch (Throwable throwable) {
            } finally {
                CloseUtils.closeItQuietly(inputStream);
            }
        }
        return "";
    }

    /**
     * 检测传入参数对应的类是否存在
     */
    public static boolean checkClass(String packageName, String className) {
        boolean result = true;
        try {
            Class.forName(packageName + "." + className);
        } catch (Throwable throwable) {
            result = false;
        }
        return result;
    }

    public static void setIdFile(Context context, String key, String value) {
        try {
            if (context != null && !TextUtils.isEmpty(key)) {
                String filePath = context.getFilesDir().getPath() + Constants.FILE_NAME;
                String info = readFile(filePath);
                JSONObject job;
                if (!TextUtils.isEmpty(info)) {
                    job = new JSONObject(info);
                } else {
                    job = new JSONObject();
                }
                if (TextUtils.isEmpty(value)) {
                    job.remove(key);
                } else {
                    job.put(key, value);
                }
                writeFile(filePath, String.valueOf(job));
            }
        } catch (Throwable throwable) {

        }
    }

    /**
     * 获取 id 信息
     */
    public static String getIdFile(Context context, String key) {
        String filePath = context.getFilesDir().getPath() + Constants.FILE_NAME;
        try {
            String info = readFile(filePath);
            if (!TextUtils.isEmpty(info)) {
                JSONObject job = new JSONObject(info);
                return job.optString(key);
            }
        } catch (Throwable throwable) {
            writeFile(filePath, null);
        }
        return null;
    }

    /**
     * 写入数值
     */
    public static void writeCount(String path, String content) {
        try {
            writeFile(path + Constants.COUNT_FILE_NAME, content);
        } catch (Throwable throwable) {
        }
    }

    /**
     * 读取数值
     */
    public static int readCount(String path) {
        try {
            String count = readFile(path + Constants.COUNT_FILE_NAME);
            return NumberFormat.convertToInteger(count);
        } catch (Throwable throwable) {
            return 0;
        }
    }

    /**
     * 重置数值
     */
    public static void resetCount(String path) {
        writeCount(path, "0");
    }

    /**
     * 文件存储数据
     */
    private synchronized static void writeFile(String filePath, String content) {
        RandomAccessFile randomAccessFile = null;
        FileChannel fileChannel = null;
        if (isEmpty(filePath)) {
            return;
        }
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            if (TextUtils.isEmpty(content)) {
                file.delete();
            }
            randomAccessFile = new RandomAccessFile(file, "rw");
            fileChannel = randomAccessFile.getChannel();
            final FileLock fileLock = fileChannel.lock(0L, Long.MAX_VALUE, false);
            if (fileLock != null) {
                if (fileLock.isValid()) {
                    fileChannel.truncate(0);
                    fileChannel.write(ByteBuffer.wrap(content.getBytes()));
                    fileLock.release();
                }
            }
        } catch (Throwable throwable) {
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (Throwable throwable) {
                }
            }
            if (fileChannel != null) {
                try {
                    fileChannel.close();
                } catch (Throwable throwable) {
                }
            }
        }
    }

    /**
     * 文件读取数据
     */
    private synchronized static String readFile(String filePath) {
        FileChannel fileChannel = null;
        RandomAccessFile randomAccessFile = null;
        if (isEmpty(filePath)) {
            return null;
        }
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            randomAccessFile = new RandomAccessFile(file, "rw");
            fileChannel = randomAccessFile.getChannel();
            FileLock fileLock = fileChannel.lock(0, Long.MAX_VALUE, true);
            if (null != fileLock) {
                if (fileLock.isValid()) {
                    randomAccessFile.seek(0);
                    byte[] buf = new byte[(int) randomAccessFile.length()];
                    randomAccessFile.read(buf);
                    return new String(buf, "utf-8");
                }
            }
        } catch (Throwable e) {
        } finally {
            if (fileChannel != null) {
                try {
                    fileChannel.close();
                } catch (Throwable throwable) {
                }
            }
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (Throwable throwable) {
                }
            }
        }
        return null;
    }

    /**
     * 获取完整类path
     */
    public static String getClassPath(String path) {
        int index = path.lastIndexOf(".");
        return path.substring(0, index);
    }

    /**
     * 获取方法名称
     */
    public static String getMethod(String path) {
        int index = path.lastIndexOf(".");
        return path.substring(index + 1);
    }

    //.............................................................

    /**
     * 获取 Channel
     */
    public static String getChannel(Context context) {
        String channel = "";
        try {
            channel = SharedUtil.getString(context, Constants.SP_CHANNEL, null);
            if (isEmpty(channel)) {
                channel = getManifestData(context, Constants.DEV_CHANNEL);
                if (!isEmpty(channel)) {
                    SharedUtil.setString(context, Constants.SP_CHANNEL, channel);
                }
            }
        } catch (Throwable throwable) {
        }
        return channel;
    }

    /**
     * 获取distinct id 如果用户没有调用，获取androidId
     */
    public static String getUserId(Context context) {
        String id = getIdFile(context, Constants.SP_ALIAS_ID);
        if (!isEmpty(id)) {
            return id;
        }
        id = getIdFile(context, Constants.SP_DISTINCT_ID);
        if (!isEmpty(id)) {
            return id;
        }
        id = getIdFile(context, Constants.SP_UUID);
        if (!isEmpty(id)) {
            return id;
        }
        if (TextUtils.isEmpty(id)) {
            id = transSaveId(context);
        }
        return id;
    }

    /**
     * 转存id
     */
    private static String transSaveId(Context context) {
        String aliasId = SharedUtil.getString(context, Constants.SP_ALIAS_ID, null);
        if (!TextUtils.isEmpty(aliasId)) {
            setIdFile(context, Constants.SP_ALIAS_ID, aliasId);
        }
        String distinctId = SharedUtil.getString(context, Constants.SP_DISTINCT_ID, null);
        if (!TextUtils.isEmpty(distinctId)) {
            setIdFile(context, Constants.SP_DISTINCT_ID, distinctId);
        }
        String uuid = SharedUtil.getString(context, Constants.SP_UUID, null);
        if (TextUtils.isEmpty(uuid)) {
            uuid = String.valueOf(java.util.UUID.randomUUID());
        }
        setIdFile(context, Constants.SP_UUID, uuid);

        if (!TextUtils.isEmpty(aliasId)) {
            return aliasId;
        }
        if (!TextUtils.isEmpty(distinctId)) {
            return distinctId;
        }
        return uuid;
    }

    /**
     * 获取应用版本名称
     */
    public static String getVersionName(Context context) {
        try {
            final PackageManager packageManager = context.getPackageManager();
            final PackageInfo packageInfo = packageManager.
                    getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Throwable throwable) {
            return Constants.EMPTY;
        }
    }

    /**
     * 获取当前的运营商
     */
    public static String getCarrierName(Context context) {
        if (checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
            TelephonyManager mTelephonyMgr = (TelephonyManager)
                    context.getSystemService(Context.TELEPHONY_SERVICE);
            String imsi = mTelephonyMgr.getSubscriberId();
            if (!isEmpty(imsi)) {
                if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
                    return "中国移动";
                } else if (imsi.startsWith("46001")) {
                    return "中国联通";
                } else if (imsi.startsWith("46003")) {
                    return "中国电信";
                }
            }
        }
        return null;
    }

    /**
     * 获取屏幕宽度
     */
    public static Object getScreenWidth(Context context) {
        int width;
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            width = metrics.widthPixels;
        } else {
            WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            width = dm.widthPixels;
        }
//        if (width == -1) {
//            DisplayMetrics dm = context.getResources().getDisplayMetrics();
//            width = dm.widthPixels;
//        }
        return width;
    }

    /**
     * 获取 屏幕高度
     * 如果context是Activity获取的是物理的屏幕尺寸 如果不是获取的是Activity的尺寸
     */
    public static Object getScreenHeight(Context context) {
        int height;
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            height = metrics.heightPixels;
        } else {
            WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            height = dm.heightPixels;
        }
//        if (height == -1) {
//            DisplayMetrics dm = context.getResources().getDisplayMetrics();
//            height = dm.heightPixels;
//        }
        return height;
    }

    /**
     * 是否首日访问
     */
    public static Object isFirstDay(Context context) {
        String nowTime = getDay();
        String firstDay = SharedUtil.getString(context, Constants.DEV_IS_FIRST_DAY, null);
        if (isEmpty(firstDay)) {
            SharedUtil.setString(context, Constants.DEV_IS_FIRST_DAY, nowTime);
            return true;
        } else {
            return firstDay.equals(nowTime);
        }
    }

    /**
     * 获取当前时间
     */
    public static Object getCurrentTime(Context context) {
        return System.currentTimeMillis() + Constants.TIME_DIFFERENCE;
    }

    /**
     * 数据是否被校准
     */
    public static Object isCalibrated(Context context) {
        return Constants.TIME_DIFFERENCE != 0;
    }

    /**
     * 获取debug状态 服务器设置 > 用户设置 > 默认设置
     */
    public static Object getDebugMode(Context context) {
        int debug = SharedUtil.getInt(context, Constants.SP_SERVICE_DEBUG, -1);
        if (debug != -1) {
            return debug;
        }
        debug = SharedUtil.getInt(context, Constants.SP_USER_DEBUG, -1);
        if (debug != -1) {
            return debug;
        }
        return 0;
    }

    /**
     * 是否登录
     */
    public static boolean getLogin(Context context) {
        int isLogin = SharedUtil.getInt(context, Constants.SP_IS_LOGIN, 0);
        return isLogin == 1;
    }

    /**
     * 获取 IMEI
     */
    public static String getIMEI(Context context) {
        try {
            if (checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                TelephonyManager telephonyMgr =
                        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                return telephonyMgr.getDeviceId();
            }
        } catch (Throwable throwable) {
        }
        return Constants.EMPTY;
    }

    /**
     * 获取mac地址
     */
    public static Object getMac(Context context) {
        try {
            if (Build.VERSION.SDK_INT < 23) {
                return getMacBySystemInterface(context);
            } else if (Build.VERSION.SDK_INT == 23) {
                return getMacByFileAndJavaAPI(context);
            } else {
                return getMacByJavaAPI();
            }
        } catch (Throwable throwable) {
        }
        return Constants.EMPTY;
    }

    private static String getMacBySystemInterface(Context context) {
        if (context != null) {
            WifiManager wifi = (WifiManager) context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            if (checkPermission(context, Manifest.permission.ACCESS_WIFI_STATE)) {
                WifiInfo info = wifi.getConnectionInfo();
                return info.getMacAddress();
            }
        }
        return Constants.EMPTY;
    }

    private static String getMacByFileAndJavaAPI(Context context) throws Exception {
        String mac = getMacShell();
        return !isEmpty(mac) ? mac : getMacByJavaAPI();
    }

    private static String getMacByJavaAPI() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface netInterface = interfaces.nextElement();
            if ("wlan0".equals(netInterface.getName()) || "eth0".equals(netInterface.getName())) {
                byte[] addr = netInterface.getHardwareAddress();
                if (addr == null || addr.length == 0) {
                    continue;
                }
                StringBuilder buf = new StringBuilder();
                for (byte b : addr) {
                    buf.append(String.format("%02X:", b));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                return buf.toString().toLowerCase(Locale.getDefault());
            }
        }
        return Constants.EMPTY;
    }

    private static String getMacShell() throws IOException {

        String[] urls = new String[]{
                "/sys/class/net/wlan0/address",
                "/sys/class/net/eth0/address",
                "/sys/devices/virtual/net/wlan0/address"
        };
        String mc;
        for (String url : urls) {
            mc = reaMac(url);
            if (mc != null) {
                return mc;
            }
        }
        return Constants.EMPTY;
    }

    private static String reaMac(String url) throws IOException {
        String macInfo;
        FileReader fstream = new FileReader(url);
        BufferedReader in = null;
        try {
            in = new BufferedReader(fstream, 1024);
            macInfo = in.readLine();
        } finally {
            CloseUtils.closeItQuietly(fstream, in);
        }
        return macInfo;
    }

    /**
     * 获取original id
     */
    public static Object getOriginalId(Context context) {
        String originalId = SharedUtil.getString(context, Constants.SP_ORIGINAL_ID, null);
        if (!isEmpty(originalId)) {
            return originalId;
        } else {
            return getDistinctId(context);
        }
    }

    /**
     * 本地数据加密
     */
    public static String dbEncrypt(String data) {
        if (!TextUtils.isEmpty(data)) {
            byte[] bytes = Base64.encode(data.getBytes(), Base64.NO_WRAP);
            String baseData = String.valueOf(new StringBuffer(new String(bytes)).reverse());
            int length = baseData.length();
            String subA = baseData.substring(0, length / 10);
            String subB = baseData.substring(length / 10, length);
            return subB + subA;
        }
        return null;
    }

    /**
     * 本地数据解密
     */
    public static String dbDecrypt(String data) {
        try {
            if (!TextUtils.isEmpty(data)) {
                // 兼容老版本不做加密只做编码的数据
                try {
                    String baseData = new String(Base64.decode(data.getBytes(), Base64.DEFAULT));
                    if (!TextUtils.isEmpty(baseData)
                            && !isEmpty(new JSONObject(baseData))) {
                        return baseData;
                    }
                } catch (Throwable throwable) {
                }
                // 当前版本解密
                int length = data.length();
                int l = length - (length / 10);
                String subA = data.substring(0, l);
                String subB = data.substring(l, length);
                String dd = String.valueOf(new StringBuffer(subB + subA).reverse());
                return new String(Base64.decode(dd, Base64.NO_WRAP));
            }
        } catch (Throwable throwable) {
        }
        return null;
    }

    /**
     * 获取Application
     */
    public static Application getApplication() {
        try {
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object at = activityThread.getMethod("currentActivityThread").invoke(null);
            Object app = activityThread.getMethod("getApplication").invoke(at);
            if (app != null) {
                return (Application) app;
            }
        } catch (Throwable throwable) {
        }
        return null;
    }

    /**
     * 删除key/value值为空的值
     */
    public static void clearEmptyValue(Map<String, Object> map) {
        if (!isEmpty(map)) {
            Set<String> keys = map.keySet();
            String key;
            Object value;
            for (Iterator<String> iterator = keys.iterator(); iterator.hasNext(); ) {
                key = iterator.next();
                value = map.get(key);
                if (isEmpty(key)) {
                    iterator.remove();
                    continue;
                }
                if (isInvalidValue(value)) {
                    iterator.remove();
                }
            }
        }
    }

    private static boolean isInvalidValue(Object value) {
        return isEmpty(value)
                || "unknown".equalsIgnoreCase(value.toString())
                || "null".equalsIgnoreCase(value.toString());
    }

    /**
     * map 拷贝
     */
    public static <String, T> Map<String, T> deepCopy(Map<String, T> map) {
        try {
            if (map == null) {
                return new HashMap<>();
            }
            return new HashMap<>(map);
        } catch (Throwable throwable) {
            return new HashMap<>();
        }
    }

    /**
     * 获取 证书
     */
    private static String certName = null;

    public static SSLSocketFactory getSSLSocketFactory(Context context) {
        if (TextUtils.isEmpty(certName)) {
            certName = getManifestData(context, Constants.DEV_KEYSTONE);
        }
        if (TextUtils.isEmpty(certName)) {
            return getDefaultSSLSocketFactory();
        } else {
            return getUserSSLSocketFactory(context, certName);
        }
    }

    /**
     * 默认信任所有证书
     */
    private static SSLSocketFactory getDefaultSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] x509Certificates,
                                                       String s) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] x509Certificates,
                                                       String s) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            }, null);
            return sslContext.getSocketFactory();
        } catch (Throwable throwable) {
        }
        return null;
    }

    /**
     * 需要配置证书
     */
    private static SSLSocketFactory getUserSSLSocketFactory(Context context, String certName) {
        try {
            //读取证书
            AssetManager am = context.getAssets();
            if (am != null) {
                // 读取证书文件
                InputStream is = am.open(certName);
                InputStream input = new BufferedInputStream(is);
                CertificateFactory cerFactory = CertificateFactory.getInstance("X.509");
                Certificate cer = cerFactory.generateCertificate(input);
                // 创建keystore，包含我们的证书
                String keySoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keySoreType);
                keyStore.load(null);
                keyStore.setCertificateEntry("cert", cer);
                // 创建一个 trustManager，仅把 keystore 中的证书 作为信任的锚点
                String algorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory trustManagerFactory =
                        TrustManagerFactory.getInstance(algorithm);
                trustManagerFactory.init(keyStore);
                TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
                // 用 TrustManager 初始化一个SSLContext
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustManagers, null);
                return sslContext.getSocketFactory();
            }
        } catch (Throwable throwable) {
        }
        return null;
    }
}