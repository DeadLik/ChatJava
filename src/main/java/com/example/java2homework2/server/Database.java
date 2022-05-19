package com.example.java2homework2.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class Database {
    private static final Logger log = LogManager.getLogger(Database.class);

    private static Connection connection;
    private static Statement statement;

    private static PreparedStatement getUserNicknameStatement;

    public static boolean connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:mainDB.db");
            log.info("Подключение к базе данных выполнено");
            statement = connection.createStatement();
            createTable();

//            insert("user1", "111", "bulka");
//            insert("user2", "222", "volk");
//            insert("user3", "333", "oreh");

            prepareAllStatement();
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void disconnect() {
        try {
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTable() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("" +
                " CREATE TABLE IF NOT EXISTS users (" +
                "     id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "     login TEXT, " +
                "     password TEXT, " +
                "     nick TEXT " +
                ")")) {
            statement.executeUpdate();
        }
    }

    public static void insert(String login, String password, String nick) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO users (login, password, nick) VALUES (?, ?, ?)")) {
            statement.setString(1, login);
            statement.setString(2, password);
            statement.setString(3, nick);
            statement.executeUpdate();
        }
    }


    public static void prepareAllStatement() throws SQLException {
        getUserNicknameStatement = connection.prepareStatement("SELECT nick FROM users WHERE login = ? AND password = ?;");
    }

    public static String getUserNickname(String login, String password) {
        String nickname = null;
        try {
            getUserNicknameStatement.setString(1, login);
            getUserNicknameStatement.setString(2, password);
            ResultSet rs = getUserNicknameStatement.executeQuery();
            if (rs.next()) {
                nickname = rs.getString(1);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nickname;
    }
}
