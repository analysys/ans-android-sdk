package com.analysys.thread;

import java.util.concurrent.Callable;

/**
 * @Copyright © 2020 EGuan Inc. All rights reserved.
 * @Description: 易观优先级线程提交类
 * @Version: 1.0
 * @Create: 2020/9/26 11:44
 * @Author: wp
 */
public abstract class PriorityCallable<T> implements Callable<T> {

    private volatile Object result;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    private boolean waitResult;

    public boolean isWaitResult() {
        return waitResult;
    }

    public void setWaitResult(boolean waitResult) {
        this.waitResult = waitResult;
    }

    private PriorityCallable() {

    }

    public PriorityCallable(long priority) {
        this.priority = priority;
    }

    private long priority;          //0、1、2（0代表set、get类高优先级，1代表pv、end中低优先级，2代表track类事件）

    public long getPriority() {
        return priority;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
