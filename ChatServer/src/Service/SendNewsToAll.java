package Service;

import ChatCommon.Message;
import ChatCommon.MessageType;
import Utils.JDBCUtilsByDruid;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

/**
 * @auther Devil(丁杨维)
 * @create 2021-11-06-16:50
 */
public class SendNewsToAll implements Runnable {
    Scanner input = new Scanner(System.in);

    @Override
    public void run() {

        while (true) {
            System.out.println("\n请输入你要发送的新闻[输入Exit退出发送]");
            //获取要发送的信息
            String content = input.nextLine();
            //如果是Exit则退出发送新闻线程
            if (content.equals("Exit")) {
                break;
            }
            //创建Message对象
            Message message = new Message();
            //设置消息类型 MESSAGE_MES_TO_ALL 表示发送给所有用户
            message.setMesType(MessageType.MESSAGE_MES_TO_ALL);
            //发送者 服务器
            message.setSender("服务器");
            //装填消息
            message.setMes(content);
            //设置发送消息时间
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            message.setSendTime(dateTimeFormatter.format(LocalDateTime.now()));
            //使用JdbcTemplate类执行dml语句
            JdbcTemplate jdbcTemplate = new JdbcTemplate(JDBCUtilsByDruid.getDataSource());
            String sql = "insert into ServerMessage values (null,?,?,?,?)";
            jdbcTemplate.update(sql, "服务器", message.getMes(),"所有人", message.getSendTime());
            //遍历用户线程集合
            Iterator<Map.Entry<String, ServerConnectThread>> iter = ManageServerConnectThread.getMap().iterator();
            while (iter.hasNext()) {
                try {
                    //获取用户线程的socket对象的对象输出流
                    ObjectOutputStream oos = new ObjectOutputStream(iter.next().getValue().getSocket().getOutputStream());
                    //发送信息
                    oos.writeObject(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
