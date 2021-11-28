package ChatCommon;

import java.io.Serializable;

/**
 * @auther Devil(丁杨维)
 * @create 2021-11-02-19:30
 */
public class User implements Serializable {
    private static final long serialVersionUID = 2L;
    private String ID = null;//用户id(账号)
    private String PWD = null;//密码
    private UserType userType = null;//操作用户类型(注册,销户,登陆)

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }


    public User(String ID, String PWD) {
        this.ID = ID;
        this.PWD = PWD;
    }


    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getPWD() {
        return PWD;
    }

    public void setPWD(String PWD) {
        this.PWD = PWD;
    }

}
