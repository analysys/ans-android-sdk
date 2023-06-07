package com.analysys.visual.utils;

import android.view.View;

import com.analysys.utils.ExceptionUtil;

import java.lang.reflect.Method;

public class ViewMethodReflector {
    private final String mMethodName;
    private final Object[] mMethodArgs;
    private final Class<?> mMethodResultType;
    private final Class<?> mTargetClass;
    private final Method mTargetMethod;

    public ViewMethodReflector(Class<?> targetClass, String methodName, Object[] methodArgs, Class<?> resultType)
            throws NoSuchMethodException {
        mMethodName = methodName;

        mMethodArgs = methodArgs;
        mMethodResultType = resultType;
        mTargetMethod = pickMethod(targetClass);
        if (null == mTargetMethod) {
            throw new NoSuchMethodException("Method " + targetClass.getName() + "." + mMethodName + " not exists");
        }

        mTargetClass = mTargetMethod.getDeclaringClass();
    }

    private static Class<?> assignableArgType(Class<?> type) {
        if (type == Byte.class) {
            type = byte.class;
        } else if (type == Short.class) {
            type = short.class;
        } else if (type == Integer.class) {
            type = int.class;
        } else if (type == Long.class) {
            type = long.class;
        } else if (type == Float.class) {
            type = float.class;
        } else if (type == Double.class) {
            type = double.class;
        } else if (type == Boolean.class) {
            type = boolean.class;
        } else if (type == Character.class) {
            type = char.class;
        }

        return type;
    }

    @Override
    public String toString() {
        return "[ViewMethodReflector " + mMethodName + "(" + mMethodArgs + ")" + "]";
    }

    public Object applyMethod(View target) {
        return applyMethodWithArguments(target, mMethodArgs);
    }

    public Object applyMethodWithArguments(View target, Object[] arguments) {
        final Class<?> klass = target.getClass();
        if (mTargetClass.isAssignableFrom(klass)) {
            try {
                return mTargetMethod.invoke(target, arguments);
            } catch (final Throwable ignore) {
                ExceptionUtil.exceptionPrint(ignore);
            }
        }

        return null;
    }

    private Method pickMethod(Class<?> klass) {
        final Class<?>[] argumentTypes = new Class[mMethodArgs.length];
        for (int i = 0; i < mMethodArgs.length; i++) {
            argumentTypes[i] = mMethodArgs[i].getClass();
        }

        for (final Method method : klass.getMethods()) {
            final String foundName = method.getName();
            final Class<?>[] params = method.getParameterTypes();

            if (!foundName.equals(mMethodName) || params.length != mMethodArgs.length) {
                continue;
            }

            final Class<?> assignType = assignableArgType(mMethodResultType);
            final Class<?> resultType = assignableArgType(method.getReturnType());
            if (!assignType.isAssignableFrom(resultType)) {
                continue;
            }

            boolean assignable = true;
            for (int i = 0; i < params.length && assignable; i++) {
                final Class<?> argumentType = assignableArgType(argumentTypes[i]);
                final Class<?> paramType = assignableArgType(params[i]);
                assignable = paramType.isAssignableFrom(argumentType);
            }

            if (!assignable) {
                continue;
            }

            return method;
        }

        return null;
    }
}
