package ChatView;

/**
 * @auther Devil(丁杨维)
 * @create 2021-11-11-12:46
 */
public class ChatMenu {
    //一级菜单
    public static void Menu1() {
        System.out.println("=============用户一级菜单============");
        System.out.println("\t\t 1.用 户 登 陆");
        System.out.println("\t\t 2.用 户 注 册");
        System.out.println("\t\t 3.用 户 注 销");
        System.out.println("\t\t 20.退 出 系 统");
        System.out.print("请输入你要进行的操作: ");
    }

    //二级菜单
    public static void Menu2(String UID) {
        System.out.println("=============二级菜单(用户:" + UID + "欢迎你)============");
        System.out.println("\t\t 1.显示在线用户列表");
        System.out.println("\t\t 2.群 聊 消 息");
        System.out.println("\t\t 3.私 聊 消 息");
        System.out.println("\t\t 4.返回一级菜单");
        System.out.println("\t\t 20.退 出 系 统");
        System.out.print("请输入你的选择: ");
    }

    //三级菜单
    public static void Menu3(String UID) {
        System.out.println("=============三级菜单(用户:" + UID + "欢迎你)============");
        System.out.println("\t\t 1.显示在线群聊列表");
        System.out.println("\t\t 2.创 建 群 聊(直接进入你所创建的群)");
        System.out.println("\t\t 3.加 入 群 聊");
        System.out.println("\t\t 4.销 毁 群 聊");
        System.out.println("\t\t 9.退 出 群 聊");
        System.out.print("请输入你的选择: ");
    }
}
