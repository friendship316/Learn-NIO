package com.learn.nio;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import com.learn.common.BeanContainer;
import com.learn.common.RequstObject;
import com.learn.common.SerializeUtil;

/**
 * @author: lifs
 * @create: 2018-06-28 07:38
 **/
public class RpcNioServer {

    private static final RpcNioServer rpcNioServer = new RpcNioServer();

    // 通道管理器
    private Selector selector;

    // 通道
    private ServerSocketChannel serverChannel;

    // 客户端通道
    private SocketChannel channel;

    public static void start() {
        rpcNioServer.initServer(8080);
        rpcNioServer.listen();
    }

    private void initServer(int port) {
        try {
            serverChannel = ServerSocketChannel.open();
            // 设置通道为非阻塞
            serverChannel.configureBlocking(false);
            // 将该通道对应的ServerSocket绑定到port端口
            serverChannel.socket().bind(new InetSocketAddress(port));
            // 打开通道管理器 监听accept事件
            this.selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务端启动完成！");
        } catch (IOException e) {
            System.out.println("服务端启动出错！");
            e.printStackTrace();
        }
    }

    public void listen() {
        try {
            while (true) {
                // 当连接的事件到达时，方法返回；否则,该方法会一直阻塞
                selector.select();
                System.out.println("accept事件到达");
                // 获得selector中选中的项的迭代器，选中的项为注册的事件
                Iterator ite = this.selector.selectedKeys().iterator();
                while (ite.hasNext()) {
                    SelectionKey key = (SelectionKey) ite.next();
                    // 删除已选的key,以防重复处理
                    ite.remove();
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        // 获得和客户端连接的通道
                        channel = server.accept();
                        channel.configureBlocking(false);
                        channel.register(this.selector, SelectionKey.OP_READ);

                    } else if (key.isReadable()) {
                        RequstObject requst = (RequstObject) readMsgFromClient(channel);
                        requestHandle(requst, channel);
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("监听accpt事件出错");
            e.printStackTrace();
        }
    }

    public Object readMsgFromClient(SocketChannel channel) {
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

    public void requestHandle(RequstObject requstObject, SocketChannel channel) {
        Object obj = BeanContainer.getBean(requstObject.getCalzz());
        String methodName = requstObject.getMethodName();
        Class<?>[] parameterTypes = requstObject.getParamTypes();
        Object[] arguments = requstObject.getArgs();
        try {
            Method method = obj.getClass().getMethod(methodName, parameterTypes);
            Object result = method.invoke(obj, arguments);
            byte[] bytes = SerializeUtil.serialize(result);
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length + 4);
            buffer.putInt(bytes.length);
            buffer.put(bytes);
            channel.write(buffer);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IOException e) {
            e.printStackTrace();
        }
    }

}
