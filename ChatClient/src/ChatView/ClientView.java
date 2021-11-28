package ChatView;

import ChatCommon.MessageType;
import ChatCommon.User;
import ChatCommon.UserType;
import Service.ClientConnectServiceThread;
import Service.UserClientService;
import Service.UserMessageService;

import java.util.Scanner;

/**
 * @auther Devil(丁杨维)
 * @create 2021-11-11-12:48
 */
public class ClientView {
    private boolean loop = true;//控制循环
    private String key;//获取选项的key
    Scanner input = new Scanner(System.in);//输入流

    UserClientService userClientService = new UserClientService();

    public void view() {
        while (loop) {
            ChatMenu.Menu1();//显示一级菜单
            key = input.next();//获取输入的key值
            switch (key) {
                case "1":
                    //"登 陆 账 户"
                    System.out.println("=============登 陆 账 户=============");
                    System.out.print("请输入你的账户ID: ");
                    String UID = input.next();
                    System.out.print("请输入你的账户密码: ");
                    String PWD = input.next();
                    User user = new User(UID, PWD);
                    user.setUserType(UserType.USER_TYPE_LOGIN);
                    userClientService.setUserClientService(user);
                    if (userClientService.UserCheck()) {
                        //用户登陆成功 进入二级菜单
                        while (loop) {
                            //线程延时 防止菜单显示出错
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            ChatMenu.Menu2(user.getID());
                            key = input.next();
                            switch (key) {
                                case "1":
                                    //"显示在线用户列表";
                                    userClientService.onlineFriendList();
                                    break;
                                case "2":
                                    boolean flag = true;
                                    while (flag) {
                                        //线程延时 防止菜单显示出错
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        ChatMenu.Menu3(user.getID());
                                        key = input.next();
                                        switch (key) {
                                            case "1":
                                                //"获取当前在线群聊链表";
                                                userClientService.onlineChatRoomList();
                                                break;
                                            case "2":
                                                System.out.print("请输入你要创建的群聊名称: ");
                                                String chatRoomName = input.next();
                                                userClientService.CreateChatRoom(user.getID(), chatRoomName);
                                                input.nextLine();//吞空格
                                                try {
                                                    Thread.sleep(100);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                                if (ClientConnectServiceThread.getMessage().getMesType() == MessageType.Message_CREATE_CHATROOM_SUCCEED) {
                                                    while (true) {
                                                        String message = input.nextLine();//读取控制台输入的消息
                                                        UserMessageService.SendMessageToAll(user.getID(), message);
                                                        if (message.equals("bye")) {//如果发送的消息为bye则退出群聊
                                                            System.out.println("\n退出群聊成功!\n");
                                                            break;
                                                        }
                                                    }
                                                } else {
                                                    System.out.println("\n该群已创建\n");
                                                }
                                                break;
                                            case "3":
                                                System.out.print("请输入你要加入的群聊名称: ");
                                                String ChatRoomName = input.next();
                                                userClientService.JoinChatRoom(user.getID(), ChatRoomName);
                                                input.nextLine();//吞空格
                                                try {
                                                    Thread.sleep(100);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                                if (ClientConnectServiceThread.getMessage().getMesType() == MessageType.MESSAGE_JOIN_CHATROOM_SUCCEED) {
                                                    while (true) {
                                                        String message = input.nextLine();//读取控制台输入的消息
                                                        UserMessageService.SendMessageToAll(user.getID(), message);
                                                        if (message.equals("bye")) {//如果发送的消息为bye则退出群聊
                                                            System.out.println("\n退出群聊\n");
                                                            break;
                                                        }
                                                    }
                                                } else {
                                                    System.out.println("\n该群还未创建\n");
                                                }
                                                break;
                                            case "4":
                                                //销毁群聊
                                                System.out.print("输入你要要销毁的群聊名称:");
                                                String name = input.next();
                                                userClientService.deleteChatRoom(name, user.getID());
                                                break;
                                            case "9":
                                                flag = false;
                                                System.out.println("\n退出群聊界面成功!\n");
                                                break;
                                        }
                                    }
                                    break;
                                case "3":
                                    //"私 聊 消 息";
                                    System.out.print("请输入你要发送的对象: ");
                                    input.nextLine();
                                    String ob = input.nextLine();
                                    System.out.print("请输入你要发送的信息: ");
                                    String message = input.nextLine();
                                    UserMessageService.SendMessageToOne(user.getID(), ob, message);
                                    break;
                                case "20":
                                    userClientService.logout();
                                    System.out.println("退出成功!");
                                    loop = false;
                                    break;
                                default:
                                    break;
                            }
                            if (key.equals("4")) {
                                userClientService.logout();
                                System.out.println("\n返回成功\n");
                                break;
                            }
                        }
                    }
                    break;
                case "2":
                    //"注 册 账 户";
                    System.out.println("=============注 册 账 户=============");
                    System.out.print("请输入你要注册的账户ID: ");
                    String UID1 = input.next();
                    System.out.print("请输入你要注册的账户密码: ");
                    String PWD1 = input.next();
                    User user1 = new User(UID1, PWD1);
                    user1.setUserType(UserType.USER_TYPE_REGISTER);
                    userClientService.setUserClientService(user1);
                    userClientService.register();
                    break;
                case "3":
                    //"注 销 账 户";
                    System.out.println("=============注 销 账 户=============");
                    System.out.print("请输入你要销户的账户ID: ");
                    String UID2 = input.next();
                    System.out.print("请输入你要销户的账户密码: ");
                    String PWD2 = input.next();
                    User user2 = new User(UID2, PWD2);
                    user2.setUserType(UserType.USER_TYPE_DELETE);
                    userClientService.setUserClientService(user2);
                    userClientService.delete();
                    break;
                case "20":
                    System.out.println("退出成功!");
                    loop = false;
                    break;
                default:
                    break;
            }
        }
    }
}
