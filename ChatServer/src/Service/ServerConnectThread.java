package Service;

import ChatCommon.Message;
import ChatCommon.MessageType;
import Utils.JDBCUtilsByDruid;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;

/**
 * @auther Devil(丁杨维)
 * @create 2021-11-02-23:33
 * 创建socket线程
 */
public class ServerConnectThread extends Thread {
    String UID;//用户id 用于分辨用户线程 方便管理
    private Socket socket;

    private MessageType messageType;

    public ServerConnectThread(String UID, Socket socket) {
        this.UID = UID;
        this.socket = socket;
    }

    static {
    }

    public Socket getSocket() {
        return socket;
    }


    @Override
    public void run() {
        while (true) {

            try {
                //获得socket的对象输入流
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                //因为我们定制的协议 因此传递的是Message对象
                Message message = (Message) ois.readObject();
                //判断Message对象里的messageType
                //判断请求是否为MESSAGE_GET_ONLINE_FRIEND
                Connection connection = null;
                PreparedStatement preparedStatement = null;
                if (message.getMesType() == MessageType.MESSAGE_GET_ONLINE_FRIEND) {
                    //通过服务端线程管理类 获取用户当前用户列表
                    System.out.println("用户: " + message.getSender() + " 请求返回当前在线用户列表\n");
                    String onlineUserList = ManageServerConnectThread.getOnlineUserList();
                    //新建Message对象 用于返回客户端
                    Message message1 = new Message();
                    message1.setMesType(MessageType.MESSAGE_RET_ONLINE_FRIEND);
                    message1.setReceiver(message.getSender());
                    message1.setMes(onlineUserList);

                    //获取对象输出流发送Message对象
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(message1);
                }
                //加入群聊
                else if (message.getMesType() == MessageType.MESSAGE_CHOOSE_ONLINE_CHATROOM) {
                    //加入群聊成功 进入群聊
                    System.out.println("用户: " + message.getSender() + " 请求加入群聊: " + message.getChatRoomName() + "\n");
                    if (ManageChatRoom.ChatRoomCheck(message.getChatRoomName())) {
                        UserConnectChatRoom.UserConnectChatRoom(message);
                        System.out.println("用户: " + message.getSender() + " 退出群聊: " + message.getChatRoomName() + "\n");
                    } else {
                        Message message1 = new Message();
                        message1.setMesType(MessageType.MESSAGE_JOIN_CHATROOM_FAIL);
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        oos.writeObject(message1);
                        System.out.println("用户: " + message.getSender() + " 指定的群还未创建" + "\n");
                    }
                }
                //获取在线聊天室列表
                else if (message.getMesType() == MessageType.MESSAGE_GET_ONLINE_CHATROOM) {
                    //通过服务端线程管理类 获取用户当前用户列表
                    System.out.println("用户: " + message.getSender() + " 请求返回当前在线聊天室列表" + "\n");
                    String onlineChatRoomList = ManageChatRoom.getChatRoomList();
                    //新建Message对象 用于返回客户端
                    Message message1 = new Message();
                    message1.setMesType(MessageType.MESSAGE_RET_ONLINE_CHATROOM);
                    message1.setReceiver(message.getSender());
                    message1.setMes(onlineChatRoomList);

                    //获取对象输出流发送Message对象
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(message1);
                }
                //如果MessageType为MESSAGE_CLIENT_EXIT 代表客户端要退出连接
                else if (message.getMesType() == MessageType.MESSAGE_CLIENT_EXIT) {
                    System.out.println("用户: " + message.getSender() + " 退出" + "\n");
                    //发送结束标志 通知客户端结束用户线程
                    Message message1 = new Message();
                    message1.setMesType(MessageType.MESSAGE_CLIENT_EXIT);
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(message1);
                    socket.shutdownOutput();
                    //将这个客户端线程从集合中彻底移除
                    ManageServerConnectThread.removeServerConnectThread(message.getSender());
                    //关闭连接
                    socket.close();
                    //退出循环关闭线程
                    break;
                }
                //创建群聊
                else if (message.getMesType() == MessageType.Message_CREATE_CHATROOM) {
                    System.out.println("用户: " + message.getSender() + " 请求创建群聊: " + message.getChatRoomName() + "\n");
                    if (!ManageChatRoom.ChatRoomCheck(message.getChatRoomName())) {
                        UserConnectChatRoom.UserConnectChatRoom(message);
                        System.out.println("用户: " + message.getSender() + " 创建群聊: " + message.getChatRoomName() + "成功\n");
                    } else {
                        Message message1 = new Message();
                        message1.setMesType(MessageType.MESSAGE_CREATE_CHATROOM_FAIL);
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        oos.writeObject(message1);
                        System.out.println("用户: " + message.getSender() + " 指定的群已经创建了" + "\n");
                    }
                }
                //信息中转站 用户要发送的东西会从发送者客户端发送到这里再转到接收者客户端(这里处理的是私聊消息)
                else if (message.getMesType() == MessageType.MESSAGE_COMM_MES) {//消息信息类型为MESSAGE_COMM_MES 即普通消息
                    if (ManageServerConnectThread.getServerConnectThread(message.getReceiver()) != null) {
                        //使用JdbcTemplate执行sql语句
                        JdbcTemplate jdbcTemplate = new JdbcTemplate(JDBCUtilsByDruid.getDataSource());
                        String sql = "insert into userMessage values (?,?,?,?)";
                        String sql1 = "select id from user where UID = ?";
                        Map<String, Object> stringObjectMap = jdbcTemplate.queryForMap(sql1, message.getSender());
                        Map<String, Object> stringObjectMap1 = jdbcTemplate.queryForMap(sql1, message.getReceiver());
                        jdbcTemplate.update(sql, stringObjectMap.get("id"), stringObjectMap1.get("id"), message.getMes(), message.getSendTime());
                        System.out.println(message.getSendTime() + " 用户: " + message.getSender() + " 请求向" + message.getReceiver() + "发送消息..." + "\n");
                        //直接获取接收者线程的socket对象的输出流
                        ObjectOutputStream oos = new ObjectOutputStream(ManageServerConnectThread.getServerConnectThread(message.getReceiver()).getSocket().getOutputStream());
                        //发送
                        oos.writeObject(message);
                    } else {
                        Message message1 = new Message();
                        message1.setMesType(MessageType.MESSAGE_NOT_ONLINE);
                        System.out.println("用户: " + message.getSender() + " 请求的用户不在线" + "\n");
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        oos.writeObject(message1);
                    }
                } else if (message.getMesType() == MessageType.MESSAGE_DELETE_CHATROOM) {
                    ManageChatRoom.deleteChatroom(message.getChatRoomName(), message.getSender());
                    System.out.println("用户: " + message.getSender() + " 请求删除群聊: " + message.getChatRoomName() + "\n");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
