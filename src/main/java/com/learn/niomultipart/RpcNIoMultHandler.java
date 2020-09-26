package com.learn.niomultipart;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.learn.common.RequstMultObject;
import com.learn.common.RpcContainer;
import com.learn.common.RpcResponseFuture;
import com.learn.common.SerializeUtil;

/**
 * @author: lifs
 * @create: 2018-07-01 11:41
 **/
public class RpcNIoMultHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 获得请求id
        Long responseId = RpcContainer.getRequestId();
        // 封装请求对象
        RequstMultObject requstMultObject = new RequstMultObject(method.getDeclaringClass(), method.getName(),
                method.getParameterTypes(), args);
        requstMultObject.setRequestId(responseId);

        // 封装设置rpcResponseFuture，主要用于获取返回值
        RpcResponseFuture rpcResponseFuture = new RpcResponseFuture(responseId);
        RpcContainer.addRequstFuture(rpcResponseFuture);

        // 序列化
        byte[] requstBytes = SerializeUtil.serialize(requstMultObject);
        // 发送请求信息
        RpcNioMultClient rpcNioMultClient = RpcNioMultClient.getInstance();
        rpcNioMultClient.sendMsg2Server(requstBytes);

        // 从ResponseContainer获取返回值
        byte[] responseBytes = rpcResponseFuture.get();
        if (requstBytes != null) {
            RpcContainer.removeResponseAndFuture(responseId);
        }

        // 反序列化获得结果
        Object result = SerializeUtil.unSerialize(responseBytes);
        System.out.println("请求id：" + responseId + " 结果：" + result);
        return result;
    }
}
