package ChatCommon;

/**
 * 消息类型 用于处理不同的需求
 *
 * @auther Devil(丁杨维)
 * @create 2021-11-02-19:27
 */
public enum MessageType {
//==================================用户管理类型=================================

    //表示登陆成功
    MESSAGE_LOGIN_SUCCEED,
    //表示登陆失败
    MESSAGE_LOGIN_FAIL,
    //客户端请求退出
    MESSAGE_CLIENT_EXIT,
    //用户注册成功
    MESSAGE_REGISTER_SUCCEED,
    //用户注册失败
    MESSAGE_REGISTER_FAIL,
    //用户销户成功
    MESSAGE_DELETE_SUCCEED,
    //用户销户成功
    MESSAGE_DELETE_FAIL,

//================================用户聊天类型===================================

    //表示普通(私聊)信息
    MESSAGE_COMM_MES,
    //表示群聊中别人发的消息
    MESSAGE_MES_TO_ALL,
    //表示你在群聊中的发的消息
    MESSAGE_MES_TO_OTHER,
    //私聊对象未在线
    MESSAGE_NOT_ONLINE,

//================================用户请求列表类型===================================

    //请求返回在线群聊列表
    MESSAGE_GET_ONLINE_CHATROOM,
    //返回在线群聊列表
    MESSAGE_RET_ONLINE_CHATROOM,
    //要求返回用户在线列表
    MESSAGE_GET_ONLINE_FRIEND,
    //返回用户在线列表
    MESSAGE_RET_ONLINE_FRIEND,

//==================================用户群聊操作类型=================================

    //群聊创建成功
    Message_CREATE_CHATROOM_SUCCEED,
    //选择群聊天室
    MESSAGE_CHOOSE_ONLINE_CHATROOM,
    //加入群聊天室成功
    MESSAGE_JOIN_CHATROOM_SUCCEED,
    //加入群聊失败
    MESSAGE_JOIN_CHATROOM_FAIL,
    //创建群聊天室
    Message_CREATE_CHATROOM,
    //创建群聊失败
    MESSAGE_CREATE_CHATROOM_FAIL,
    //销毁群聊(creator)
    MESSAGE_DELETE_CHATROOM,
    //销毁群聊成功
    MESSAGE_DELETE_CHATROOM_SUCCEED,
    //销毁群聊失败
    MESSAGE_DELETE_CHATROOM_FAIL,
    //获取群聊消息
    MESSAGE_GET_CHATROOM_MESSAGE,
    //退出群聊
    MESSAGE_EXIT_CHATROOM,

}
