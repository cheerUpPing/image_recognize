package com.elon.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {

    public static Properties properties = null;

    static {
        try {
            InputStream inputStream = DBUtil.class.getResourceAsStream("/db.properties");
            properties = new Properties();
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String db_url = DBUtil.properties.getProperty("db_url");
    public static String username = DBUtil.properties.getProperty("username");
    public static String userpass = DBUtil.properties.getProperty("userpass");
    public static String drive_name = DBUtil.properties.getProperty("drive_name");

    /**
     * 获取数据库连接
     *
     * @param db_url
     * @param username
     * @param password
     * @param classAllName
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static Connection getConnection(String db_url, String username, String password, String classAllName) throws ClassNotFoundException, SQLException {
        Class.forName(classAllName);
        return DriverManager.getConnection(db_url, username, password);
    }


    public static void main(String[] args) {
        String db_url = DBUtil.properties.getProperty("db_url");
        String username = DBUtil.properties.getProperty("username");
        String userpass = DBUtil.properties.getProperty("userpass");
        System.out.println(db_url + " " + username + " " + userpass);
    }

}
