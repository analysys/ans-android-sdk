package com.analysys.process;

import android.content.res.Resources;
import android.text.TextUtils;
import android.util.SparseArray;

import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.ExceptionUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @Copyright © 2019 analysys Inc. All rights reserved.
 * @Description: 所有的资源文件id(view.getID)和对应唯一资源id(代码里生命的android : id)
 * @Version: 1.0
 * @Create: Apr 2, 2019 4:38:57 PM
 * @Author: sanbo
 */
public class SystemIds {

    //    private boolean isParser;
//
    private final Map<String, Integer> mIdNameToId = new HashMap<>();
    private final SparseArray<String> mIdToIdName = new SparseArray<>();
//    private final boolean isDebug = false;

    private String mPkgName;

    private SystemIds() {
        mPkgName = AnalysysUtil.getContext().getPackageName();
    }

    public static SystemIds getInstance() {
        return Holder.INSTANCE;
    }


//    /**
//     * 解析id
//     */
//    public void parserId() {
//        // 值执行一次
//        if (isParser) return;
//        isParser = true;
//
//        Class<?> sysIdClass = getDrawablesSystemClass();
//        readClassIds(sysIdClass, "android", mIdNameToId);
//        String localClassName = getLocalDrawablesClassName();
//        try {
//            if (localClassName != null) {
//                final Class<?> rIdClass = Class.forName(localClassName);
//                readClassIds(rIdClass, null, mIdNameToId);
//            }
//        } catch (Throwable ignore) {
////            ExceptionUtil.exceptionThrow(ignore);
//        }
//        sysIdClass = getIdSystemClass();
//        readClassIds(sysIdClass, "android", mIdNameToId);
//        localClassName = getLocalIdClassName();
//        try {
//            if (localClassName != null) {
//                final Class<?> rIdClass = Class.forName(localClassName);
//                readClassIds(rIdClass, null, mIdNameToId);
//            }
//        } catch (Throwable ignore) {
////            ExceptionUtil.exceptionThrow(ignore);
//        }
//        for (Map.Entry<String, Integer> idMapping : mIdNameToId.entrySet()) {
//            mIdToIdName.put(idMapping.getValue(), idMapping.getKey());
//        }
//    }
//
//    private void readClassIds(Class<?> platformIdClass, String namespace,
//                              Map<String, Integer> namesToIds) {
//        try {
//            final Field[] fields = platformIdClass.getFields();
//            for (final Field field : fields) {
//                final int modifiers = field.getModifiers();
//                if (Modifier.isStatic(modifiers)) {
//                    final Class<?> fieldType = field.getType();
//                    if (fieldType == int.class) {
//                        final String name = field.getName();
//                        final int value = field.getInt(null);
//                        final String namespacedName;
//                        if (null == namespace) {
//                            namespacedName = name;
//                        } else {
//                            namespacedName = namespace + ":" + name;
//                        }
//                        namesToIds.put(namespacedName, value);
//                        if (isDebug) {
//                            ANSLog.i("readClassIds:  " + namespacedName + "----" + value);
//                        }
//                    }
//                }
//            }
//        } catch (Throwable ignore) {
//            ExceptionUtil.exceptionThrow(ignore);
//            //ANSLog.e(e);
//        }
//    }
//
//    public boolean knownIdName(String name) {
//        return mIdNameToId.containsKey(name);
//    }

//    public int idFromName(String name) {
//        return mIdNameToId.get(name);
//    }
//
//    String nameForId(int id) {
//        return mIdToIdName.get(id);
//    }

    public String nameFromId(Resources res, int id) {
        String value = mIdToIdName.get(id);
        if (value != null) {
            return value;
        }
        try {
            if (res == null) {
                res = AnalysysUtil.getContext().getResources();
            }
            String resName = res.getResourceName(id);
            if (TextUtils.isEmpty(resName)) {
                value = null;
            } else {
                String[] arr = resName.split("/");
                if (arr.length != 2) {
                    value = resName;
                } else if (arr[0].equals(mPkgName + ":id")) {
                    value = arr[1];
                } else {
                    value = resName.replaceAll("id/", "");
                }
            }
            mIdToIdName.put(id, value);
            return value;
        } catch (Throwable ignore) {
//            ExceptionUtil.exceptionPrint(ignore);
        }
        return null;
    }

    public int idFromName(Resources res, String name) {
        Integer value = mIdNameToId.get(name);
        if (value != null) {
            return value;
        }
        try {
            if (res == null) {
                res = AnalysysUtil.getContext().getResources();
            }
            int id = res.getIdentifier(name, "id", mPkgName);
            if (id <= 0) {
                id = -1;
            }
            mIdNameToId.put(name, id);
            return id;
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
        return -1;
    }
//
//    private Class<?> getDrawablesSystemClass() {
//        return android.R.drawable.class;
//    }
//
//    private String getLocalDrawablesClassName() {
//        Context context = AnalysysUtil.getContext();
//        if (context != null) {
//            return context.getPackageName() + ".R$drawable";
//        } else {
//            return null;
//        }
//    }
//
//    private Class<?> getIdSystemClass() {
//        return android.R.id.class;
//    }
//
//    private String getLocalIdClassName() {
//        Context context = AnalysysUtil.getContext();
//        if (context != null) {
//            return context.getPackageName() + ".R$id";
//        } else {
//            return null;
//        }
//    }
//
//    boolean isRegister() {
//        return !mIdNameToId.isEmpty() && mIdToIdName.size() > 0;
//    }

    private static class Holder {
        private static final SystemIds INSTANCE = new SystemIds();
    }
}
