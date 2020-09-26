package com.learn.niosingle;

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
 * @create: 2018-06-30 14:10
 **/
public class RpcNioSigleServer {
    // 通道管理器
    private Selector selector;

    public static void start() throws IOException {
        RpcNioSigleServer server = new RpcNioSigleServer();
        server.initServer(8080);
        server.listen();
    }

    /**
     * 获得一个ServerSocket通道，并对该通道做一些初始化的工作
     *
     * @param port
     *            绑定的端口号
     * @throws IOException
     */
    public void initServer(int port) throws IOException {
        // 获得一个ServerSocket通道
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        // 设置通道为非阻塞
        serverChannel.configureBlocking(false);
        // 将该通道对应的ServerSocket绑定到port端口
        serverChannel.socket().bind(new InetSocketAddress(port));
        // 获得一个通道管理器
        this.selector = Selector.open();
        // 将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_ACCEPT事件,注册该事件后，
        // 当该事件到达时，selector.select()会返回，如果该事件没到达selector.select()会一直阻塞。
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * 采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理
     *
     * @throws IOException
     */
    public void listen() throws IOException {
        System.out.println("服务端启动成功！");
        // 轮询访问selector
        while (true) {
            // 当注册的事件到达时，方法返回；否则,该方法会一直阻塞
            selector.select();
            // 获得selector中选中的项的迭代器，选中的项为注册的事件
            Iterator ite = this.selector.selectedKeys().iterator();
            while (ite.hasNext()) {
                SelectionKey key = (SelectionKey) ite.next();
                // 删除已选的key,以防重复处理
                ite.remove();
                // 客户端请求连接事件
                if (key.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    // 获得和客户端连接的通道
                    SocketChannel channel = server.accept();
                    // 设置成非阻塞
                    channel.configureBlocking(false);

                    // 在和客户端连接成功之后，为了可以接收到客户端的信息，需要给通道设置读的权限。
                    channel.register(this.selector, SelectionKey.OP_READ);

                    // 获得了可读的事件
                } else if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    if (channel.isOpen()) {
                        RequstObject requstObject = readMsgFromClient(channel);
                        if (requstObject != null) {
                            requestHandle(requstObject, (SocketChannel) key.channel());
                        }
                    }
                }

            }

        }
    }

    public RequstObject readMsgFromClient(SocketChannel channel) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        try {
            // 首先读取头，长度
            int headCount = channel.read(byteBuffer);
            if (headCount < 0) {
                return null;
            }
            byteBuffer.flip();
            int length = byteBuffer.getInt();
            // 读取消息体
            byteBuffer = ByteBuffer.allocate(length);
            int bodyCount = channel.read(byteBuffer);
            if (bodyCount < 0) {
                return null;
            }
            byte[] bytes = byteBuffer.array();
            Object obg = SerializeUtil.unSerialize(bytes);
            return (RequstObject) obg;
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
            buffer.flip();
            channel.write(buffer);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IOException e) {
            e.printStackTrace();
        }
    }

}
