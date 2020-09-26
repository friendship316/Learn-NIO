package com.learn.proxy;

import java.lang.reflect.Proxy;

import com.learn.niomultipart.RpcNIoMultHandler;
import com.learn.niosingle.RpcNioSigleClient;

/**
 * @author: lifs
 * @create: 2018-06-28 23:35
 **/
public class RpcProxyFactory {

    /**
     * 单线程示例代理对象
     * 
     * @param interfaceClass
     * @return T
     * @throws BizException
     * @createTime：2018/7/1
     * @author: shakeli
     */
    public static <T> T getSingleService(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[] { interfaceClass },
                new RpcNioSigleClient());
    }

    /**
     * 多线程环境代理对象
     * 
     * @param interfaceClass
     * @return T
     * @throws BizException
     * @createTime：2018/7/1
     * @author: shakeli
     */
    public static <T> T getMultService(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[] { interfaceClass },
                new RpcNIoMultHandler());
    }
}
