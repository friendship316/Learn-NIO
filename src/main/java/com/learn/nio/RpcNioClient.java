package com.learn.nio;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import com.learn.common.RequstObject;
import com.learn.common.SerializeUtil;

/**
 * @author: lifs
 * @create: 2018-06-26 23:04
 **/
public class RpcNioClient {

    private volatile static RpcNioClient rpcNioClient;

    // 通道管理器
    private Selector selector;

    // 通道
    private SocketChannel channel;

    private String serverIp = "localhost";

    private int port = 8080;

    private RpcNioClient() {
    }

    public static RpcNioClient getInstance() {
        if (rpcNioClient == null) {
            synchronized (RpcNioClient.class) {
                if (rpcNioClient == null) {
                    rpcNioClient = new RpcNioClient();
                }
            }
        }
        return rpcNioClient;
    }

    public void init() {
        try {
            // 打开一个通道
            channel = SocketChannel.open();
            // 设置为非阻塞通道（异步）
            channel.configureBlocking(false);
            // 获得通道管理器，用于监听通道事件
            selector = Selector.open();
            // 建立连接
            channel.connect(new InetSocketAddress(serverIp, port));
            // 由于是非阻塞的，所以有可能连接并未建立完成，调用finishConnect完成连接
            if (channel.isConnectionPending()) {
                channel.finishConnect();
            }
            System.out.println("客户端初始化完成，建立连接完成");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() {
        try {
            while (true) {
                // 绑定到通道管理器,监听可读事件，因为客户端只需要从服务端获得数据然后读取，所以只需要监听READ事件
                channel.register(selector, SelectionKey.OP_READ);
                // 开始轮询READ事件
                selector.select();
                Iterator ite = this.selector.selectedKeys().iterator();
                while (ite.hasNext()) {
                    SelectionKey key = (SelectionKey) ite.next();
                    // 删除已选的key,以防重复处理
                    ite.remove();
                    readMsgFromServer();
                }
            }
        } catch (IOException e) {
            System.out.println("客户端建立连接失败");
        }
    }

    public boolean sendMsg2Server(Method method, Object[] args) {
        try {
            // 封装请求对象，并序列化
            RequstObject requst = new RequstObject(method.getClass(), method.getName(), method.getParameterTypes(),
                    args);
            byte[] bytes = SerializeUtil.serialize(requst);
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length + 4);
            // 放入消息长度，然后放入消息体
            buffer.putInt(bytes.length);
            buffer.put(bytes);
            // 写出消息
            channel.write(buffer);
        } catch (IOException e) {
            System.out.println("写出消息失败！");
            e.printStackTrace();
        }
        return true;
    }

    public Object readMsgFromServer() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        try {
            // 首先读取头，长度
            channel.read(byteBuffer);
            byteBuffer.flip();
            int length = byteBuffer.getInt();
            // 读取消息体
            byteBuffer = ByteBuffer.allocate(length);
            int read = channel.read(byteBuffer);

            while (read != -1) {
                System.out.println("信息未完全读取");
            }
            byte[] bytes = byteBuffer.array();
            Object obg = SerializeUtil.unSerialize(bytes);
            return obg;
        } catch (IOException e) {
            System.out.println("读取数据异常");
            e.printStackTrace();
            return null;
        }
    }
}
