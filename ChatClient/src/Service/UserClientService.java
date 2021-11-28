package Service;

import ChatCommon.Message;
import ChatCommon.MessageType;
import ChatCommon.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @auther Devil(丁杨维)
 * @create 2021-11-11-15:56
 * 此类用于用户登陆注册和退出
 */
public class UserClientService {
    private User user;
    private Socket socket;

    //获取传来的User对象
    public void setUserClientService(User user) {
        this.user = user;
    }

    public boolean UserCheck() {
        boolean flag = false;//创建一个返回标志
        int port = 9999;//设置端口
        //获得User对象后 与服务器建立连接 将User对象通过对象输出流输出到服务端验证

        try {
            socket = new Socket(InetAddress.getByName("localhost"), port);//创建一个socket对象
            //创建Socket对象的对象输出流 然后用其输出上面传入的User对象
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            //发送
            oos.writeObject(user);
            //发送完毕 等待服务器返回消息 同样需要使用对象输入流来接收客户端发来的Message对象
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            //获取到服务端发来的Message对象 检查返回的Message对象那个的MessageType来确定用户的操作是否成功

            Message mes = (Message) ois.readObject();

            //检查Message对象判断用户是否登陆成功
            if (mes.getMesType() == MessageType.MESSAGE_LOGIN_SUCCEED) {
                //登陆成功
                System.out.println("\n用户登陆成功\n");
                //为了使得用户可以一直于服务端连接 发送消息 将其加入到线程中
                //ClientConnectServiceThread类 管理用户的socket
                ClientConnectServiceThread clientConnectServiceThread = new ClientConnectServiceThread(socket, oos);
                //启动线程
                clientConnectServiceThread.start();
                flag = true;
            }//销户成功
            else if (mes.getMesType() == MessageType.MESSAGE_DELETE_SUCCEED) {
                //销户成功通知用户
                System.out.println("\n用户销户成功\n");
                //关闭流
                socket.close();
                flag = true;
            }
            //注册成功
            else if (mes.getMesType() == MessageType.MESSAGE_REGISTER_SUCCEED) {
                //销户成功通知用户
                System.out.println("\n用户注册成功\n");
                //关闭流
                socket.close();
                flag = true;
            }
            //用户登陆失败
            else if (mes.getMesType() == MessageType.MESSAGE_LOGIN_FAIL) {
                //用户登陆失败 显示到屏幕上
                System.out.println("\n登陆失败可能是用户未注册或用户已在线\n");
                //关闭socket流
                socket.close();
                //返回标志不变动
            }
            //用户销户失败
            else if (mes.getMesType() == MessageType.MESSAGE_DELETE_FAIL) {
                //用户销户失败 显示到屏幕上
                System.out.println("\n销户失败可能是用户未注册\n");
                //关闭socket流
                socket.close();
                //返回标志不变动
            }
            //用户注册失败
            else if (mes.getMesType() == MessageType.MESSAGE_REGISTER_FAIL) {
                //用户注册失败 显示到屏幕上
                System.out.println("\n注册失败可能是用户已注册\n");
                //关闭socket流
                socket.close();
                //返回标志不变动
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    //获取在线用户列表
    public void onlineFriendList() {
        try {
            //创建一个Message对象将其MessageType设置为MESSAGE_GET_ONLINE_FRIEND
            Message message = new Message();
            message.setSender(user.getID());
            message.setMesType(MessageType.MESSAGE_GET_ONLINE_FRIEND);
            //通过线程类返回该用户的socket的对象输出流
            ObjectOutputStream oos = new ObjectOutputStream(ClientConnectServiceThread.getSocket().getOutputStream());
            //发送message对象
            oos.writeObject(message);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //获取在线群聊天室列表
    public void onlineChatRoomList() {
        try {
            //创建一个Message对象将其MessageType设置为MESSAGE_GET_ONLINE_CHATROOM
            Message message = new Message();
            message.setSender(user.getID());
            message.setMesType(MessageType.MESSAGE_GET_ONLINE_CHATROOM);
            //通过线程类返回该用户的socket的对象输出流
            ObjectOutputStream oos = new ObjectOutputStream(ClientConnectServiceThread.getSocket().getOutputStream());
            //发送message对象
            oos.writeObject(message);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //注册
    public void register() {
        //注册时 要建立一个连接
        int port = 9999;//服务器端口
        try {
            socket = new Socket(InetAddress.getByName("localhost"), port);//创建一个socket
            //获取scoket对象的对象输出流
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            //发送User对象
            oos.writeObject(user);
            //获取对象输入流 以获得服务器返回的结果
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Message message = (Message) ois.readObject();
            if (message.getMesType() == MessageType.MESSAGE_REGISTER_SUCCEED) {
                System.out.println("\n注册成功\n");
            } else if (message.getMesType() == MessageType.MESSAGE_REGISTER_FAIL) {
                System.out.println("\n注册失败,可能是该用户已经注册\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //销户
    public void delete() {
        //销户时 要建立一个连接
        int port = 9999;//服务器端口
        try {
            socket = new Socket(InetAddress.getByName("localhost"), port);//创建一个socket
            //获取scoket对象的对象输出流
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            //发送User对象
            oos.writeObject(user);
            //获取对象输入流 以获得服务器返回的结果
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Message message = (Message) ois.readObject();
            if (message.getMesType() == MessageType.MESSAGE_DELETE_SUCCEED) {
                System.out.println("\n销户成功\n");
            } else if (message.getMesType() == MessageType.MESSAGE_DELETE_FAIL) {
                System.out.println("\n销户失败,可能是该用户还未注册,也可能是密码输入错误\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //退出与客户端的连接 无异常退出
    public void logout() {
        //创建一个Message对象
        Message message = new Message();
        message.setSender(user.getID());
        message.setMesType(MessageType.MESSAGE_CLIENT_EXIT);

        try {
            //通过用户线程类获得用户socket的对象输出流
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //创建群聊 要求传入 发送者 群名
    public void CreateChatRoom(String UID, String chatRoomName) {
        //创建一个Message对象将其MessageType设置为Message_CREATE_CHARTROOM 并设置群名
        Message message = new Message();
        message.setSender(UID);
        message.setMesType(MessageType.Message_CREATE_CHATROOM);
        message.setChatRoomName(chatRoomName);
        try {
            //通过线程类返回该用户的socket的对象输出流
            ObjectOutputStream oos = new ObjectOutputStream(ClientConnectServiceThread.getSocket().getOutputStream());
            //发送message对象
            oos.writeObject(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //加入群聊 要求传入 发送者 群名
    public void JoinChatRoom(String UID, String chatRoomName) {
        //创建一个Message对象将其MessageType设置为Message_CREATE_CHARTROOM 并设置群名
        Message message = new Message();
        message.setSender(UID);
        message.setMesType(MessageType.MESSAGE_CHOOSE_ONLINE_CHATROOM);
        message.setChatRoomName(chatRoomName);

        try {
            //通过线程类返回该用户的socket的对象输出流
            ObjectOutputStream oos = new ObjectOutputStream(ClientConnectServiceThread.getSocket().getOutputStream());
            //发送message对象
            oos.writeObject(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteChatRoom(String chatRoomName, String UID) {
        try {
            Message message = new Message();
            message.setMesType(MessageType.MESSAGE_DELETE_CHATROOM);
            message.setSender(UID);
            message.setChatRoomName(chatRoomName);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
