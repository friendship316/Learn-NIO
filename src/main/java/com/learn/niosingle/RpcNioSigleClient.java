package com.learn.niosingle;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
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
 * @create: 2018-06-30 11:52
 **/
public class RpcNioSigleClient implements InvocationHandler {

    private Selector selector;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        initClient("localhost", 8080);
        return listen(method, args);
    }

    /**
     * 获得一个Socket通道，并对该通道做一些初始化的工作
     *
     * @param ip
     *            连接的服务器的ip
     * @param port
     *            连接的服务器的端口号
     * @throws IOException
     */
    public void initClient(String ip, int port) throws IOException {
        // 获得一个Socket通道
        SocketChannel channel = SocketChannel.open();
        // 设置通道为非阻塞
        channel.configureBlocking(false);
        // 获得一个通道管理器
        this.selector = Selector.open();

        // 客户端连接服务器,其实方法执行并没有实现连接
        // 需要在listen（）方法中调channel.finishConnect();才能完成连接
        channel.connect(new InetSocketAddress(ip, port));
        // 将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_CONNECT事件。
        channel.register(selector, SelectionKey.OP_CONNECT);
    }

    /**
     * 采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理
     *
     * @throws IOException
     */
    public Object listen(Method method, Object[] args) throws IOException {
        // 轮询访问selector
        while (true) {
            selector.select();
            // 获得selector中选中的项的迭代器
            Iterator ite = this.selector.selectedKeys().iterator();
            while (ite.hasNext()) {
                SelectionKey key = (SelectionKey) ite.next();
                // 删除已选的key,以防重复处理
                ite.remove();
                // 连接事件发生
                if (key.isConnectable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    // 如果正在连接，则完成连接
                    if (channel.isConnectionPending()) {
                        channel.finishConnect();
                    }
                    // 设置成非阻塞
                    channel.configureBlocking(false);

                    // 在和服务端连接成功之后，为了可以接收到服务端的信息，需要给通道设置读的权限。
                    channel.register(this.selector, SelectionKey.OP_READ);

                    // 在这里可以给服务端发送信息
                    sendMsg2Server(method, args, channel);

                    // 获得了可读的事件
                } else if (key.isReadable()) {
                    return readMsgFromServer((SocketChannel) key.channel());
                }

            }

        }
    }

    public boolean sendMsg2Server(Method method, Object[] args, SocketChannel channel) {
        try {
            // 封装请求对象，并序列化
            RequstObject requst = new RequstObject(method.getDeclaringClass(), method.getName(),
                    method.getParameterTypes(), args);
            byte[] bytes = SerializeUtil.serialize(requst);
            ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length + 4);
            // 放入消息长度，然后放入消息体
            buffer.putInt(bytes.length);
            buffer.put(bytes);
            // 写完之后buffer设置为可读状态
            buffer.flip();
            // 写出消息
            channel.write(buffer);
            System.out.println("客户端写出消息成功！");
        } catch (IOException e) {
            System.out.println("客户端写出消息失败！");
            e.printStackTrace();
        }
        return true;
    }

    public Object readMsgFromServer(SocketChannel channel) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        try {
            // 首先读取头，长度
            channel.read(byteBuffer);
            byteBuffer.flip();
            int length = byteBuffer.getInt();
            // 读取消息体
            byteBuffer = ByteBuffer.allocate(length);
            int read = channel.read(byteBuffer);

            while (read == -1) {
                System.out.println("信息未完全读取");
            }
            byte[] bytes = byteBuffer.array();
            // 任务完成，关闭连接
            channel.close();
            Object obg = SerializeUtil.unSerialize(bytes);
            return obg;
        } catch (IOException e) {
            System.out.println("读取数据异常");
            e.printStackTrace();
            return null;
        }
    }
}
