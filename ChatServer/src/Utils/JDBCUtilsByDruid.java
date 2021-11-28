package Utils;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author Devil
 * @auther Devil(丁杨维)
 * @create 2021-11-15-11:32
 * 基于Druid数据库连接池的工具类
 */
public class JDBCUtilsByDruid {
    private static DataSource dataSource;

    //在静态代码块中完成初始化
    static {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("src\\druid.properties"));
            dataSource = DruidDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 编写获得连接的方法
     *
     * @return Connection
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * 关闭连接 对于connection的关闭 只是断开他与对象之间的引用 将其返回到数据库连接池中 并不是真正的关闭
     *
     * @param resultSet
     * @param connection
     * @param statement
     */
    public static void close(ResultSet resultSet, Connection connection, Statement statement) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (connection != null) {
                connection.close();
            }
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回数据源 即数据库连接池
     *
     * @return DataSource
     */
    public static DataSource getDataSource() {
        return dataSource;
    }
}
