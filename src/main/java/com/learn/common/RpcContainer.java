package com.learn.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: lifs
 * @create: 2018-07-01 10:58
 **/
public class RpcContainer {

    private static ConcurrentHashMap<Long, byte[]> responseContainer = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Long, RpcResponseFuture> requestFuture = new ConcurrentHashMap<>();
    private volatile static AtomicLong requstId = new AtomicLong(0);

    public static Long getRequestId() {
        return requstId.getAndIncrement();
    }

    public static void addResponse(Long requestId, byte[] responseBytes) {
        responseContainer.put(requestId, responseBytes);
        RpcResponseFuture responseFuture = requestFuture.get(requestId);
        responseFuture.rpcIsDone();
    }

    public static byte[] getResponse(Long requestId) {
        return responseContainer.get(requestId);
    }

    public static void addRequstFuture(RpcResponseFuture rpcResponseFuture) {
        requestFuture.put(rpcResponseFuture.getRequstId(), rpcResponseFuture);
    }

    public static RpcResponseFuture getRpcRequstFutue(Long requestId) {
        return requestFuture.get(requestId);
    }

    public static void removeResponseAndFuture(Long requestId) {
        responseContainer.remove(requestId);
        requestFuture.remove(requestId);
    }
}
