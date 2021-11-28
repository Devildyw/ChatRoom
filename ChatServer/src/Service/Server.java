package Service;

import ChatCommon.Message;
import ChatCommon.MessageType;
import ChatCommon.User;
import ChatCommon.UserType;
import Utils.JDBCUtilsByDruid;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;

/**
 * @auther Devil(丁杨维)
 * @create 2021-11-02-23:15
 * <p>
 * 服务器端
 */
public class Server {
    //创建ServerSocket对象
    private ServerSocket serverSocket;
    static JdbcTemplate jdbcTemplate;

    static {
        jdbcTemplate = new JdbcTemplate(JDBCUtilsByDruid.getDataSource());
        jdbcTemplate.queryForList("select * from server");
    }

    /**
     * 用于用户注册
     *
     * @param user
     */
    public static void UserRegister(User user) {
        //使用JdbcTemplate类执行sql语句
        String sql = "insert into user values (null,?,?)";
        jdbcTemplate.update(sql, user.getID(), user.getPWD());
    }

    /**
     * 销户
     *
     * @param user
     */
    public static void UserDelete(User user) {
        //使用JdbcTemplate类执行sql语句
        String sql = "delete from user where UID = ?";
        jdbcTemplate.update(sql, user.getID());
    }

    /**
     * 检查用户登陆
     *
     * @param user
     * @return
     */
    public boolean userCheck(User user) {
        //通过JdbcTemplate类获得字段
        String sql = "select * from user where UID = ?";
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sql, user.getID());
        if (mapList.size() == 0) {//如果获取成功且u为空则表示用户存储集合中没有改用户 也就是说账户ID输入错误
            return false;
        } else {
            String id = (String) mapList.get(0).get("UID");
            String password = (String) mapList.get(0).get("password");
            if (id == null) {
                return false;
            }
            if (!password.equals(user.getPWD())) {
                return false;
            }
            //如果上述条件都满足即用户账号,密码都满足验证 则返回true
            return true;
        }

    }

    /**
     * 用于处理传入User对象和 UserType对象的用户检查
     *
     * @param user
     * @param userType
     * @return
     */
    public boolean userCheck(User user, UserType userType) {
        String sql = "select * from user where UID = ?";
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sql, user.getID());

        if (mapList.size() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public Server() {
        new Thread(new SendNewsToAll()).start();
        //注意:端口可以写在配置文件中
        try {
            System.out.println("服务器已启动");
            serverSocket = new ServerSocket(9999);

            //循环接收连接 持续监听 因为多用用户连接 保证用户正常使用
            // 要一直保持开启除非出现意外或人为关闭
            while (true) {
                Socket socket = serverSocket.accept();//阻塞监听 直到连接到为止

                //得到socket的对象输入流
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                User user = (User) ois.readObject();//读取到客户发送来的User对象
                //创建socket的对象输出流 方便后面将Message对象发送到客户端通知它结果
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

                //验证
                Message message = new Message();//因if-else语句中都可能使用Message对象因此定义在if-else语句外

                //返回客户端消息登陆成功
                if (userCheck(user) && (user.getUserType() == UserType.USER_TYPE_LOGIN && ManageServerConnectThread.getServerConnectThread(user.getID()) == null)) {//验证用户的账户ID和密码
                    //验证成功 登陆成功
                    //创建Message对象装载验证结果
                    message.setMesType(MessageType.MESSAGE_LOGIN_SUCCEED);//返回登陆成功
                    System.out.println("用户: " + user.getID() + " 登陆成功" + "\n");
                    //将Message对象返回到客户端
                    oos.writeObject(message);

                    //加入线程 创建ServerConnectThread类 将其加入线程
                    ServerConnectThread serverConnectThread = new ServerConnectThread(user.getID(), socket);//创建线程
                    serverConnectThread.start();//启动线程

                    //创建ManageServerConnectThread类 将其加入集合中管理
                    ManageServerConnectThread.addServerConnectThread(user.getID(), serverConnectThread);
                }

                //返回客户端消息注册成功
                else if (userCheck(user, user.getUserType()) && user.getUserType() == UserType.USER_TYPE_REGISTER) {
                    //验证成功 注册成功
                    //创建Message对象装载验证结果
                    message.setMesType(MessageType.MESSAGE_REGISTER_SUCCEED);//返回注册成功

                    //将Message对象返回到客户端
                    oos.writeObject(message);
                    System.out.println("用户: " + user.getID() + " 注册成功" + "\n");

                    //加入用户集合
                    Server.UserRegister(user);
                    //注册成功关闭socket
                    socket.close();
                }

                //返回客户端消息销户成功
                else if (userCheck(user) && (user.getUserType() == UserType.USER_TYPE_DELETE)) {
                    //验证成功 注册成功
                    //创建Message对象装载验证结果
                    message.setMesType(MessageType.MESSAGE_DELETE_SUCCEED);//返回销户成功

                    //将Message对象返回到客户端
                    oos.writeObject(message);
                    System.out.println("用户: " + user.getID() + " 注销成功" + "\n");

                    //将用户移除用户集合
                    Server.UserDelete(user);
                    //销户成功关闭socket
                    socket.close();
                }

                //返回客户端消息登陆失败
                else if (!userCheck(user) && (user.getUserType() == UserType.USER_TYPE_LOGIN)) {
                    //创建Message对象装载验证结果
                    message.setMesType(MessageType.MESSAGE_LOGIN_FAIL);//返回登陆失败
                    //将Message对象返回到客户端
                    oos.writeObject(message);
                    System.out.println("用户: " + user.getID() + " 登陆失败原因是用户未注册" + "\n");
                    //登陆失败关闭socket
                    socket.close();
                }

                //返回客户端消息登陆失败
                else if (userCheck(user) && (user.getUserType() == UserType.USER_TYPE_LOGIN && ManageServerConnectThread.getServerConnectThread(user.getID()) != null)) {
                    //创建Message对象装载验证结果
                    message.setMesType(MessageType.MESSAGE_LOGIN_FAIL);//返回登陆失败
                    //将Message对象返回到客户端
                    oos.writeObject(message);
                    System.out.println("用户: " + user.getID() + " 登陆失败原因是用户已在线" + "\n");
                    //登陆失败关闭socket
                    socket.close();
                }

                //返回客户端消息注册失败
                else if (!userCheck(user, user.getUserType()) && user.getUserType() == UserType.USER_TYPE_REGISTER) {
                    //创建Message对象装载验证结果
                    message.setMesType(MessageType.MESSAGE_REGISTER_FAIL);//返回注册失败
                    //将Message对象返回到客户端
                    oos.writeObject(message);
                    System.out.println("用户: " + user.getID() + " 注册失败 原因是用户已注册" + "\n");
                    //注册失败关闭socket
                    socket.close();
                }

                //返回客户端消息销户失败
                else if (!userCheck(user) && (user.getUserType() == UserType.USER_TYPE_DELETE)) {
                    //创建Message对象装载验证结果
                    message.setMesType(MessageType.MESSAGE_DELETE_FAIL);//返回销户失败
                    //将Message对象返回到客户端
                    oos.writeObject(message);
                    System.out.println("用户: " + user.getID() + " 销户失败 原因是用户未注册" + "\n");
                    //销户失败关闭socket
                    socket.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //如果循环监听被人为或其他原因打断 说明服务器不在监听 需要关闭服务器
            try {
                serverSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
