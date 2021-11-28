package Service;

import ChatCommon.Message;
import ChatCommon.MessageType;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @auther Devil(丁杨维)
 * @create 2021-11-12-8:51
 * 此类用于管理发送用户消息
 */
public class UserMessageService {
    //处理群聊中的发送的消息
    public static void SendMessageToAll(String UID, String mes) {
        Message message = new Message();
        message.setMesType(MessageType.MESSAGE_MES_TO_OTHER);
        message.setSender(UID);
        message.setMes(mes);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        message.setSendTime(dateTimeFormatter.format(LocalDateTime.now()));
        try {
            //获取用户的socket的对象输出流
            ObjectOutputStream oos = new ObjectOutputStream(ClientConnectServiceThread.getSocket().getOutputStream());

            oos.writeObject(message);
            //发送消息
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //处理私聊消息
    public static void SendMessageToOne(String sender, String receiver, String message) {
        Message message1 = new Message();
        message1.setMesType(MessageType.MESSAGE_COMM_MES);
        message1.setMes(message);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        message1.setSendTime(dateTimeFormatter.format(LocalDateTime.now()));
        message1.setSender(sender);
        message1.setReceiver(receiver);

        try {
            ObjectOutputStream oos = new ObjectOutputStream(ClientConnectServiceThread.getSocket().getOutputStream());
            System.out.println("\n" + message1.getSendTime() + "\n" + "你向" + message1.getReceiver() + "发送消息: " + message + "\n");
            oos.writeObject(message1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
