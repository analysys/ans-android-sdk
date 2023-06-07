package com.analysys.thread;

import java.util.LinkedList;

/**
 * @Copyright © 2020 EGuan Inc. All rights reserved.
 * @Description: 易观高、中、低优先级线程处理
 * @Version: 1.0
 * @Create: 2020/9/26 11:44
 * @Author: wp
 */
public class AnsLogicThread {
//    private static LinkedList<PriorityCallable> linkedList = new LinkedList<>();
//    private static boolean flag = false;    //代表线程池是否执行完毕，true表示执行完成
//
//    private static boolean flag_thread = false;     //进行线程池阻塞操作是否进行中
//
//    private static final byte[] logic_byte = new byte[1];

    public static class PriorityLevel {
        public static final int HIGH = 0;
        public static final int MIDDLE = 1;
        public static final int LOW = 2;
    }

    /**
     * 异步线程处理
     * @param priorityCallable
     */
    public static void async(PriorityCallable priorityCallable) {
        ThreadUtils.async(priorityCallable);

//        synchronized (AnsLogicThread.class) {
//            //1、高优先级队列有东西======>把中、低优先级队列执行完毕，再执行高优先级队列
//
//            //2、高优先级队列没有东西====>直接分配中、低优先级队列执行
//            if (priorityCallable != null && priorityCallable.getPriority() == PriorityLevel.HIGH) {
//                executeLink(priorityCallable);
//
//            } else {
//                if (linkedList.size() > 0) {
//
//                    executeLink(priorityCallable);
//
//                } else {
////                System.out.println("asyncPriorityExecutor");
//                    //到LinkedBlocking队列中的数据不能有优先级
//                    if (priorityCallable != null) {
//                        AnsThreadPool.asyncPriorityExecutor(priorityCallable);
//                    }
//
//                }
//            }
//
//        }
    }

    /**
     * 同步线程处理
     * @param priorityCallable
     * @return
     */
    public static Object sync(PriorityCallable priorityCallable) {
        return ThreadUtils.sync(priorityCallable);

//        //1、把线程池中任务执行完成
//        //2、把队列加入线程池中，等待线程池执行完成
//        //3、拿到值返回
//
//        AnsThreadPool.isThreadSuccess();
//
//        linkedExecute();
//        AnsThreadPool.isThreadSuccess();
//
//        Object object = AnsThreadPool.syncPriorityExecutor(priorityCallable);
//
//        return object;
    }


//    private static void addLinked(PriorityCallable priorityCallable) {
//        synchronized (logic_byte) {
//            if (priorityCallable != null) {
//                linkedList.add(priorityCallable);
//            }
//        }
//    }
//
//
//    private static void linkedExecute() {
//
//        synchronized (logic_byte) {
//            Iterator iterator = linkedList.iterator();
//            while (iterator.hasNext()) {
//                PriorityCallable priorityCallableTmp = (PriorityCallable) iterator.next();
//                priorityCallableTmp.setPriority(PriorityLevel.MIDDLE);//把所有队列中的都按照1，进行中优先级处理
//                AnsThreadPool.asyncPriorityExecutor(priorityCallableTmp);
//            }
//            linkedList.clear();
//        }
//    }
//
//    private static void executeLink(PriorityCallable priorityCallable) {
//        //判断最后一个Future.get是否获取到，获取到把整个高优先级队列赋值过去    （获取到，则中、低优先级队列执行完成）     (问题关键点)
//        //判断最后一个Future.get是否获取到，未获取到直接加入当前高优先级队列  (未获取到则中、低优先级队列未执行完成)
//
//        if (priorityCallable != null) {
////                    priorityCallable.setPriority();   //在迭代执行的时候完成
////                linkedList.add(priorityCallable);
//            addLinked(priorityCallable);
//        }
//
//        if (!flag) {
//
//            //等待中低优先级执行完成（多线程的情况会导致多次执行）
//            if (!flag_thread) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        flag = AnsThreadPool.isThreadSuccess();
//
//                        //发送一条触发条件，来触发队列执行(避免最后未执行)
//                        executeLink(null);
//                    }
//                }).start();
//                flag_thread = true;
//            }
//
//        } else {
//            //遍历linkedBlockingQueue队列，从里面取值并且给优先级队列
//            linkedExecute();
//
//            flag = false;
//
//            flag_thread = false;
//        }
//
//    }
}
