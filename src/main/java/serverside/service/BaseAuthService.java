package main.java.serverside.service;

import main.java.serverside.interfaces.AuthService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BaseAuthService implements AuthService {

    private Connection connection;
    private Statement statement = null;

    public BaseAuthService() {
        try {
            connection = Connect.getConnection();
            statement = connection.createStatement();
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
        //insert into users ("login", "password", "nick") values ('Vladimir', '123456', 'Three');
    }

    @Override
    public void start() {
        System.out.println("AuthService start");
    }

    @Override
    public void stop() {
        System.out.println("AuthService stop");
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) throws SQLException {
        ResultSet set = statement.executeQuery("SELECT nick FROM users WHERE login = '" + login +
                        "'AND password = " + "'" + password + "'");
        return set.next() ? set.getString("nick") : null;
    }

    /*private class Entry {
        private String login;
        private String password;
        private String nick;

        public Entry(String login, String password, String nick) {
            this.login = login;
            this.password = password;
            this.nick = nick;
        }
    }*/
}