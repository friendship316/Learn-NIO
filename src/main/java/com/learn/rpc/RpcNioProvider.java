package com.learn.rpc;

import java.io.IOException;

import com.learn.common.BeanContainer;
import com.learn.niomultipart.RpcNioMultServer;
import com.learn.niosingle.RpcNioSigleServer;
import com.learn.service.HelloService;
import com.learn.service.HelloServiceImpl;

/**
 * @author: lifs
 * @create: 2018-06-28 23:24
 **/
public class RpcNioProvider {
    public static void main(String[] args) throws IOException {
        // 将服务放进bean容器
        HelloService helloService = new HelloServiceImpl();
        BeanContainer.addBean(HelloService.class, helloService);
        // 启动NIO服务端
        startMultRpcNioServer();
        // startSigleRpcNioServer();
    }

    /**
     * 单线程服务端示例
     * 
     * @param
     * @return void
     * @throws BizException
     * @createTime：2018/7/1
     * @author: shakeli
     */
    public static void startSigleRpcNioServer() {
        Runnable r = () -> {
            try {
                RpcNioSigleServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    public static void startMultRpcNioServer() {
        Runnable r = () -> {
            try {
                RpcNioMultServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Thread t = new Thread(r);
        t.start();
    }
}
