package Service;

import ChatCommon.Message;
import ChatCommon.MessageType;
import Utils.JDBCUtilsByDruid;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;

/**
 * @auther Devil(丁杨维)
 * @create 2021-11-11-21:31
 */
public class UserConnectChatRoom {
    public static void UserConnectChatRoom(Message message) throws Exception {
        String chatRoomName = message.getChatRoomName();
        ServerConnectThread serverConnectThread = ManageServerConnectThread.getServerConnectThread(message.getSender());
        //接收客户端发出的消息
        if (message.getMesType() == MessageType.MESSAGE_CHOOSE_ONLINE_CHATROOM) {
            ManageChatRoom.joinChatRoom(chatRoomName, message.getSender(), serverConnectThread);
            //加入群聊后 只会显示聊天室内的消息 (发送者 : 消息的格式 聊天室 标题和在线人数)
            System.out.println("用户: " + message.getSender() + " 加入群聊: " + chatRoomName + "成功 当前人数: " + ManageChatRoom.getChatRoom_Number_Of_People(chatRoomName) + "\n");//后面加人数
            //获得用户的socket的对象输出流

            ObjectOutputStream oos = new ObjectOutputStream(serverConnectThread.getSocket().getOutputStream());
            Message message2 = new Message();
            message2.setMesType(MessageType.MESSAGE_JOIN_CHATROOM_SUCCEED);
            message2.setMes("=====欢迎加入 " + chatRoomName + " 群内在线人数: " + ManageChatRoom.getChatRoom_Number_Of_People(chatRoomName) + "人=====");
            oos.writeObject(message2);

            Message message3 = new Message();
            message3.setSender("服务器");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            message3.setSendTime(dateTimeFormatter.format(LocalDateTime.now()));
            message3.setMesType(MessageType.MESSAGE_MES_TO_ALL);
            message3.setMes("欢迎 " + message.getSender() + " 加入群聊 当前群在线人数: " + ManageChatRoom.getChatRoom_Number_Of_People(chatRoomName));
            //消息插入数据库
            JdbcTemplate jdbcTemplate = new JdbcTemplate(JDBCUtilsByDruid.getDataSource());
            String sql = "insert into servermessage values(?,?,?,null,?)";
            String sql1 = "select id from chatroom where chatRoomName = ?";
            Map<String, Object> stringObjectMap = jdbcTemplate.queryForMap(sql1, chatRoomName);
            jdbcTemplate.update(sql, stringObjectMap.get("id"), message3.getSender(), message3.getMes(), message3.getSendTime());
            Iterator<Map.Entry<String, ServerConnectThread>> iter = ManageChatRoom.getChatRoomMap(chatRoomName).entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, ServerConnectThread> key = iter.next();
                //排除发送者自己 在客户端已经显示了用户要群发的消息了 这里也就理所应到不用在发送了
                if (!key.getValue().UID.equals(message.getSender())) {
                    //获取所有人(除发送者自己的socket的对象流输出流)
                    ObjectOutputStream os = new ObjectOutputStream(ManageChatRoom.getServerConnectThread(chatRoomName, key.getKey()).getSocket().getOutputStream());
                    //发送对象
                    os.writeObject(message3);
                }
            }
        } else if (message.getMesType() == MessageType.Message_CREATE_CHATROOM) {
            ManageChatRoom.CreateChatRoom(chatRoomName, message.getSender(), serverConnectThread);
            //加入群聊后 只会显示聊天室内的消息 (发送者 : 消息的格式 聊天室 标题和在线人数)
            System.out.println("用户: " + message.getSender() + " 创建群聊: " + chatRoomName + " 成功 当前人数: " + ManageChatRoom.getChatRoom_Number_Of_People(chatRoomName) + "\n");//后面加人数
            //获得用户的socket的对象输出流

            ObjectOutputStream oos = new ObjectOutputStream(serverConnectThread.getSocket().getOutputStream());
            Message message2 = new Message();
            message2.setMesType(MessageType.Message_CREATE_CHATROOM_SUCCEED);
            message2.setMes("创建群聊成功\n=====欢迎加入 " + chatRoomName + " 群内在线人数: " + ManageChatRoom.getChatRoom_Number_Of_People(chatRoomName) + "=====");
            oos.writeObject(message2);
        }

        //这里用while 一直读取消息
        while (true) {
            //获取Message对象 后面判断
            ObjectInputStream ois = new ObjectInputStream(serverConnectThread.getSocket().getInputStream());
            Message message1 = (Message) ois.readObject();
            if (message1.getMes().equals("bye")) {
                System.out.println("用户: " + message.getSender() + "退出聊天室: " + chatRoomName + "\n");
                ManageChatRoom.MemberExit(chatRoomName, message.getSender());
                break;
            }
            if (message1.getMes().equals("qcm")) {
                System.out.println("用户: " + message.getSender() + "请求获取当前群聊的聊天记录" + "\n");
                ManageChatRoom.getChatRoomMessage(chatRoomName, message1.getSender());
            }
            //如果MessageType为
            else if (message1.getMesType() == MessageType.MESSAGE_MES_TO_OTHER) {
                //发送给群中的所有人
                JdbcTemplate jdbcTemplate = new JdbcTemplate(JDBCUtilsByDruid.getDataSource());
                String sql = "insert into ChatRoomMessage values(?,?,?,?)";
                String sql1 = "select id from user where UID = ?";
                String sql2 = "select id from chatroom where chatroomname = ?";
                Map<String, Object> stringObjectMap1 = jdbcTemplate.queryForMap(sql2, chatRoomName);
                Map<String, Object> stringObjectMap = jdbcTemplate.queryForMap(sql1, message1.getSender());
                jdbcTemplate.update(sql, stringObjectMap1.get("id"), stringObjectMap.get("id"), message1.getMes(), message1.getSendTime());
                message1.setMesType(MessageType.MESSAGE_MES_TO_ALL);
                Iterator<Map.Entry<String, ServerConnectThread>> it = ManageChatRoom.getChatRoomMap(chatRoomName).entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, ServerConnectThread> key = it.next();
                    //排除发送者自己 在客户端已经显示了用户要群发的消息了 这里也就理所应到不用在发送了
                    if (!key.getValue().UID.equals(message.getSender())) {
                        //获取所有人(除发送者自己的socket的对象流输出流)
                        ObjectOutputStream os = new ObjectOutputStream(ManageChatRoom.getServerConnectThread(chatRoomName, key.getKey()).getSocket().getOutputStream());
                        os.writeObject(message1);
                    }
                }
            } else {
                System.out.println("其他类型消息暂不处理" + "\n");
            }
        }
    }
}

