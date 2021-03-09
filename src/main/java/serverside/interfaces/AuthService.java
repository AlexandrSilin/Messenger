package main.java.serverside.interfaces;

import java.sql.SQLException;

public interface AuthService {
    void start();
    void stop();
    String getNickByLoginAndPassword(String login, String password) throws SQLException;
}