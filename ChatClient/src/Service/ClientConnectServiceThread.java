package Service;


import ChatCommon.Message;
import ChatCommon.MessageType;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @auther Devil(丁杨维)
 * @create 2021-11-02-21:39
 * 该类用于建立一个用户线程 读取服务器的消息以及发送消息
 */
public class ClientConnectServiceThread extends Thread {
    //该线程需要持有Socket对象 与服务器连接发送与接收消息
    private static Socket socket = null;
    private static Message message = null;

    public ClientConnectServiceThread(Socket socket, ObjectOutputStream oos) {
        ClientConnectServiceThread.socket = socket;

    }

    @Override
    public void run() {
        //线程需要再后台和服务器一直保持通信 因此要使用while循环
        System.out.println("=============客户端线程启动=============");
        while (true) {

            try {
                //获得Message对象以以获得从服务器端获得的消息
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());//获得对象输入流
                message = (Message) ois.readObject();//如果服务器端没有发送Message对象 线程就会一直阻塞 直到服务端发送
                //此处应该还有发送给别的用户的代码需要经过客户端 此处未实现
                //后面需要使用message.
                //如果messageType为MESSAGE_RET_ONLINE_FRIEND 则返回用户ID
                if (message.getMesType() == MessageType.MESSAGE_RET_ONLINE_FRIEND) {
                    //分割用户消息展示 用户名
                    String[] onlineUser = message.getMes().split(" ");
                    int length = onlineUser.length;
                    System.out.println("=============当前在线用户列表 共"+length+"个================");
                    System.out.println();
                    for (String s : onlineUser) {
                        System.out.println("用户: " + s);
                    }
                    System.out.println();
                } else if (message.getMesType() == MessageType.MESSAGE_RET_ONLINE_CHATROOM) {
                    //分割用户消息展示 群聊名称
                    String[] onlineChar = message.getMes().split(" ");
                    int length = onlineChar.length;
                    System.out.println("=============当前聊天室列表 共"+length+"个=============");
                    System.out.println();
                    for (String s : onlineChar) {
                        System.out.println("聊天室: " + s);
                    }
                    System.out.println();
                }
                //用户不在线 私聊返回
                else if (message.getMesType() == MessageType.MESSAGE_NOT_ONLINE) {
                    System.out.println("\n用户不在线,请重新选择\n");
                }
                //加群成功
                else if (message.getMesType() == MessageType.MESSAGE_JOIN_CHATROOM_SUCCEED) {
                    //输出接收到的消息
                    System.out.println(message.getMes());
                    System.out.println("输入'bye'即可退出群聊 输入'qcm'(query ChatRoomMessage)查询聊天记录");
                }
                //建群成功
                else if (message.getMesType() == MessageType.Message_CREATE_CHATROOM_SUCCEED) {
                    System.out.println(message.getMes());
                    System.out.println("输入'bye'即可退出群聊 输入'qcm'(query ChatRoomMessage)查询聊天记录");
                }
                //退出系统
                else if (message.getMesType() == MessageType.MESSAGE_CLIENT_EXIT) {
                    //结束线程
                    break;
                }
                //获得用户发送的私聊message并显示
                else if (message.getMesType() == MessageType.MESSAGE_COMM_MES) {
                    //输出信息
                    System.out.println("\n" + message.getSendTime() + "\n" + message.getSender() + " 向你说: " + message.getMes() + "\n");

                }
                //获得用户群发的消息
                else if (message.getMesType() == MessageType.MESSAGE_MES_TO_ALL) {
                    //输出消息
                    System.out.println(message.getSender() + ": " + message.getMes());
                }
                //获取群聊聊天信息
                else if (message.getMesType() == MessageType.MESSAGE_GET_CHATROOM_MESSAGE) {
                    //输出聊天信息
                    System.out.println("发送者\t消息\t日期时间");
                    System.out.println(message.getMes());
                } else if (message.getMesType() == MessageType.MESSAGE_DELETE_CHATROOM_SUCCEED) {
                    //输出销毁成功
                    System.out.println("\n销毁成功!\n");
                } else if (message.getMesType() == MessageType.MESSAGE_DELETE_CHATROOM_FAIL) {
                    //销毁失败
                    System.out.println("\n销毁失败原因可能是你并不是群主也可能是该群未创建\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //为了可以更方便的获得Socket方法我们提供set方法
    public static Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        ClientConnectServiceThread.socket = socket;
    }

    public static Message getMessage() {
        return message;
    }

}
