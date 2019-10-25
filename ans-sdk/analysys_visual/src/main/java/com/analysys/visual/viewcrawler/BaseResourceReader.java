package com.analysys.visual.viewcrawler;

import android.content.Context;
import android.util.SparseArray;

import com.analysys.utils.InternalAgent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is for internal use in the library, and should not be imported into
 * client code.
 */
public abstract class BaseResourceReader implements ResourceIds {

    private static final String TAG = "BaseResourceReader";
    private final Context mContext;
    private final Map<String, Integer> mIdNameToId;
    //最终使用的集合，int表示id值，String表示对应id的字符串名字
    private final SparseArray<String> mIdToIdName;

    protected BaseResourceReader(Context context) {
        mContext = context;
        mIdNameToId = new HashMap<String, Integer>();
        mIdToIdName = new SparseArray<String>();
    }

    private static void readClassIds(Class<?> platformIdClass, String namespace, Map<String,
            Integer> namesToIds) {
        try {
            final Field[] fields = platformIdClass.getFields();
            for (int i = 0; i < fields.length; i++) {
                final Field field = fields[i];
                final int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    final Class<?> fieldType = field.getType();
                    if (fieldType == int.class) {
                        final String name = field.getName();
                        final int value = field.getInt(null);
                        final String namespacedName;
                        if (null == namespace) {
                            namespacedName = name;
                        } else {
                            namespacedName = namespace + ":" + name;
                        }
                        namesToIds.put(namespacedName, value);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            InternalAgent.e(TAG, "Can't read built-in id names from " + platformIdClass.getName()
                    , e);
        }
    }

    @Override
    public boolean knownIdName(String name) {
        return mIdNameToId.containsKey(name);
    }

    @Override
    public int idFromName(String name) {
        return mIdNameToId.get(name);
    }

    @Override
    public String nameForId(int id) {
        return mIdToIdName.get(id);
    }

    protected abstract Class<?> getSystemClass();

    protected abstract String getLocalClassName(Context context);

    protected void initialize() {
        mIdNameToId.clear();
        mIdToIdName.clear();

        final Class<?> sysIdClass = getSystemClass();
        //获取系统资源ID,放入mIdNameToId
        readClassIds(sysIdClass, "android", mIdNameToId);

        final String localClassName = getLocalClassName(mContext);
        try {
            final Class<?> rIdClass = Class.forName(localClassName);
            //获取本应用资源ID,放入mIdNameToId
            readClassIds(rIdClass, null, mIdNameToId);
        } catch (ClassNotFoundException e) {
            InternalAgent.w(TAG,
                    "Can't load names for Android view ids from '" + localClassName + "', " +
                    "ids by name will not be available in the events editor.");
        }

        for (Map.Entry<String, Integer> idMapping : mIdNameToId.entrySet()) {
            mIdToIdName.put(idMapping.getValue(), idMapping.getKey());
        }
    }

    public static class Ids extends BaseResourceReader {
        private final String mResourcePackageName;

        public Ids(String resourcePackageName, Context context) {
            super(context);
            mResourcePackageName = resourcePackageName;
            initialize();
        }

        @Override
        protected Class<?> getSystemClass() {
            return android.R.id.class;
        }

        @Override
        protected String getLocalClassName(Context context) {
            return mResourcePackageName + ".R$id";
        }
    }
}
