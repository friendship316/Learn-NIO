package com.learn.common;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.learn.niomultipart.RpcNioMultServerTask;

/**
 * @author: lifs
 * @create: 2018-07-01 21:39
 **/
public class ThreadPoolUtil {

    private static ThreadPoolExecutor executor;

    public static void init() {
        if (executor == null) {
            synchronized (ThreadPoolUtil.class) {
                executor = new ThreadPoolExecutor(10, 20, 200, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());
            }
        }
    }

    public static void addTask(RpcNioMultServerTask task) {
        if (executor == null) {
            init();
        }
        executor.execute(task);
    }
}
