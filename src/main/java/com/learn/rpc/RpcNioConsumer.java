package com.learn.rpc;

import com.learn.proxy.RpcProxyFactory;
import com.learn.service.HelloService;

/**
 * @author: lifs
 * @create: 2018-06-28 23:34
 **/
public class RpcNioConsumer {
    public static void main(String[] args) {
        // singleRpcNio();
        multipartRpcNio();
    }

    /**
     * 单线程NIO调用
     * 
     * @param
     * @return void
     * @throws BizException
     * @createTime：2018/7/1
     * @author: shakeli
     */
    public static void singleRpcNio() {
        // 远程调用
        HelloService proxy = RpcProxyFactory.getSingleService(HelloService.class);
        String result = proxy.sayHello("world");
        System.out.println(result);
    }

    /**
     * 多线程IO调用示例
     * 
     * @param
     * @return void
     * @throws BizException
     * @createTime：2018/7/1
     * @author: shakeli
     */
    public static void multipartRpcNio() {
        HelloService proxy = RpcProxyFactory.getMultService(HelloService.class);
        for (int i = 0; i < 100; i++) {
            final int j = i;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    String result = proxy.sayHello("world!");
                }
            };
            Thread t = new Thread(runnable);
            t.start();
        }
    }
}
