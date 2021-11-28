package Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @auther Devil(丁杨维)
 * @create 2021-11-02-23:33
 * 管理线程类 用于管理服务端与客户端连接的的线程 以及创建群聊
 */
public class ManageServerConnectThread {
    //创建一个HashMap用于存储管理线程 key:UserID value:ServerConnectThread
    private static HashMap<String, ServerConnectThread> map = new HashMap<>();

    //添加线程
    public static void addServerConnectThread(String UID, ServerConnectThread thread) {
        map.put(UID, thread);
    }

    //取出线程
    public static ServerConnectThread getServerConnectThread(String UID) {
        return map.get(UID);
    }

    //获取当前在线用户列表
    public static String getOnlineUserList() {
        String OnlineUserList = "";//用于存储用户名
        //遍历HashMap
        Iterator<Map.Entry<String, ServerConnectThread>> iter = map.entrySet().iterator();//迭代器遍历
        while (iter.hasNext()) {
            Map.Entry<String, ServerConnectThread> key = iter.next();
            OnlineUserList += key.getValue().UID + " ";//将用户名添加在String后
        }
        //最后返回列表
        return OnlineUserList;
    }

    //移除指定用户线程
    public static void removeServerConnectThread(String UID) {
        map.remove(UID);
    }

    //获取整个集合
    public static Set getMap() {
        return map.entrySet();
    }
}
