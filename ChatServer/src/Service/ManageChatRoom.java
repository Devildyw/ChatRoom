package Service;

import ChatCommon.Message;
import ChatCommon.MessageType;
import Utils.JDBCUtilsByDruid;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @auther Devil(丁杨维)
 * @create 2021-11-11-20:11
 */
public class ManageChatRoom {
    static JdbcTemplate jdbcTemplate = null;

    //创建一个HashMap 管理群聊 原理就是将用户线程加入到一个HashMap中 key为群名称 value为装着用户线程的HashMap 也就是群聊;
    private static ConcurrentHashMap<String, ConcurrentHashMap<String, ServerConnectThread>> ChatRooms = new ConcurrentHashMap<>();
    //创建一个HashMap 即一个群


    static {
        jdbcTemplate = new JdbcTemplate(JDBCUtilsByDruid.getDataSource());
        String sql = "select ChatRoomName from chatroom";
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sql);
        for (int i = 0; i < mapList.size(); i++) {
            ConcurrentHashMap<String, ServerConnectThread> ChatRoom = new ConcurrentHashMap<>();
            String name = (String) mapList.get(i).get("ChatRoomName");
            ChatRooms.put(name, ChatRoom);
        }
    }

    //建群
    public static void CreateChatRoom(String name, String UID, ServerConnectThread serverConnectThread) {
        ConcurrentHashMap<String, ServerConnectThread> ChatRoom = new ConcurrentHashMap<>();
        ChatRoom.put(UID,serverConnectThread);
        ChatRooms.put(name, ChatRoom);
        jdbcTemplate = new JdbcTemplate(JDBCUtilsByDruid.getDataSource());
        String sql = "select * from chatroom where ChatRoomName = ?";
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sql, name);
        if (mapList.size() == 0) {
            Map<String, Object> stringObjectMap = jdbcTemplate.queryForMap("select id from user where UID = ?", UID);
            String sql1 = "insert into chatroom values (null,?,?)";
            jdbcTemplate.update(sql1, name, stringObjectMap.get("id"));
        }
    }

    //获取在线聊天室列表
    public static String getChatRoomList() {
        String ChatRoomList = "";
        Iterator<Map.Entry<String, ConcurrentHashMap<String, ServerConnectThread>>> iter = ChatRooms.entrySet().iterator();
        while (iter.hasNext()) {
            ChatRoomList += iter.next().getKey() + " ";
        }
        return ChatRoomList;
    }

    //加入聊天室
    public static void joinChatRoom(String name, String UID, ServerConnectThread serverConnectThread) {
        ChatRooms.get(name).put(UID,serverConnectThread);
    }

    //获得聊天室用户的线程
    public static ServerConnectThread getServerConnectThread(String name, String UID) {
        return ChatRooms.get(name).get(UID);
    }

    //获得群聊的整个集合 用于群聊发送消息
    public static ConcurrentHashMap getChatRoomMap(String name) {
        return ChatRooms.get(name);
    }

    //返回群聊在线人数
    public static int getChatRoom_Number_Of_People(String name) {
        return ChatRooms.get(name).size();

    }

    //退出聊天室
    public static void MemberExit(String name, String UID) {
        if (ChatRooms.containsKey(name)) {
            ChatRooms.get(name).remove(UID);
            Message message = new Message();
            message.setMesType(MessageType.MESSAGE_MES_TO_ALL);
            message.setSender("服务器");
            message.setMes("用户: " + UID + " 退出了聊天室当前聊天室人数: " + ManageChatRoom.getChatRoom_Number_Of_People(name));
            OutputChatroomMessage(name, message);
        }
    }

    public static void OutputChatroomMessage(String name, Message message) {
        Iterator<Map.Entry<String, ServerConnectThread>> iter = ManageChatRoom.getChatRoomMap(name).entrySet().iterator();
        while (iter.hasNext()) {
            try {
                Map.Entry<String, ServerConnectThread> key = iter.next();
                ObjectOutputStream oos = new ObjectOutputStream(getServerConnectThread(name,key.getKey()).getSocket().getOutputStream());
                oos.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //检查是否有指定名称对应的群聊
    public static boolean ChatRoomCheck(String ChatRoomName) {
        if (ChatRooms.containsKey(ChatRoomName)) {
            return true;
        }
        return false;
    }

    //GetMessage
    public static void getChatRoomMessage(String ChatRoomName, String UID) {
        String sql = "select UID,message,date from chatroommessage,user,chatroom   where chatroommessage.SenderId = user.id and chatroom.id = chatroommessage.ChatRoomID and ChatRoomName = ?";
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sql, ChatRoomName);
        if (mapList.size()>0) {
            String mes = "";
            for (Map<String, Object> stringObjectMap : mapList) {
                mes += stringObjectMap.get("UID") + "\t" + stringObjectMap.get("message") + "\t" + stringObjectMap.get("date") + "\n";
            }

            try {
                Message message = new Message();
                message.setMes(mes);
                message.setMesType(MessageType.MESSAGE_GET_CHATROOM_MESSAGE);
                ObjectOutputStream oos = new ObjectOutputStream(ManageChatRoom.getServerConnectThread(ChatRoomName, UID).getSocket().getOutputStream());
                oos.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Message message = new Message();
            message.setMes("还没有聊天记录啊(ToT)/~~~\n");
            message.setMesType(MessageType.MESSAGE_GET_CHATROOM_MESSAGE);
            ObjectOutputStream oos = null;
            try {
                oos = new ObjectOutputStream(ManageChatRoom.getServerConnectThread(ChatRoomName, UID).getSocket().getOutputStream());
                oos.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

    /**
     * DELETE CHATROOM(only by Creator)
     *
     * @param chatRoomName
     * @param UID
     */
    public static void deleteChatroom(String chatRoomName, String UID) {
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList("select UID from chatroom join user on chatroom.creator = user.id where ChatRoomName = ?", chatRoomName);
        if (mapList.size() > 0) {
            String creator = (String) mapList.get(0).get("UID");
            if (creator.equals(UID)) {
                Message message = new Message();
                message.setMesType(MessageType.MESSAGE_MES_TO_ALL);
                message.setSender("服务器");
                message.setMes("用户: " + UID + " 解散了群聊 请自行退出");
                OutputChatroomMessage(chatRoomName, message);
                Message message1 = new Message();
                message1.setMesType(MessageType.MESSAGE_DELETE_CHATROOM_SUCCEED);
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(ManageServerConnectThread.getServerConnectThread(UID).getSocket().getOutputStream());
                    oos.writeObject(message1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ChatRooms.remove(chatRoomName);
                jdbcTemplate.update("delete chatroom from chatroom where ChatRoomName = ?", chatRoomName);
            } else {
                Message message = new Message();
                message.setMesType(MessageType.MESSAGE_DELETE_CHATROOM_FAIL);
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(ManageServerConnectThread.getServerConnectThread(UID).getSocket().getOutputStream());
                    oos.writeObject(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Message message = new Message();
            message.setMesType(MessageType.MESSAGE_DELETE_CHATROOM_FAIL);
            try {
                ObjectOutputStream oos = new ObjectOutputStream(ManageServerConnectThread.getServerConnectThread(UID).getSocket().getOutputStream());
                oos.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
