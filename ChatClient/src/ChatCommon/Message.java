package ChatCommon;

import java.io.Serializable;

/**
 * @author Devil
 * @create 2021-11-02-19:17
 * 对象需要经过io流传输所以要使用对象流 需要序列化对象
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 2L;

    private String sender;//发送者
    private String receiver;//接送者
    private String mes;//消息内容
    private String sendTime; //发送时间
    private MessageType mesType;//登陆类型 成功 或 失败
    private String ChatRoomName; //群聊天室的名称 用于选择群聊天室


    public String getChatRoomName() {
        return ChatRoomName;
    }

    public void setChatRoomName(String chatRoomName) {
        ChatRoomName = chatRoomName;
    }

    public Message() {//无参构造方法

    }

    public Message(String sender, String receiver, String mes, String sendTime) {
        this.sender = sender;
        this.receiver = receiver;
        this.mes = mes;
        this.sendTime = sendTime;
    }


    public MessageType getMesType() {
        return mesType;
    }

    public void setMesType(MessageType mesType) {
        this.mesType = mesType;
    }


    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }
}
