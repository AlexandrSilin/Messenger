package main.java.serverside.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Connect {
    private static final String DB_URL = "jdbc:postgresql://127.0.0.1:5433/postgres";
    private static final String USER = "postgres";
    private static final String PASS = "postgres";
    private static final String DRIVER = "org.postgresql.Driver";

    private static Connection instance;

    private Connect() {

    }

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName(DRIVER);
        return instance == null ? instance = DriverManager.getConnection(DB_URL, USER, PASS) : instance;
    }
}
