package serverside.service;

import main.java.serverside.interfaces.AuthService;
import main.java.serverside.service.Connect;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BaseAuthService implements AuthService {

    private Connection connection;
    private Statement statement = null;
    private final Logger logger = LogManager.getLogger(BaseAuthService.class);

    public BaseAuthService() {
        try {
            connection = Connect.getConnection();
            statement = connection.createStatement();
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void start() {
        logger.log(Level.INFO, "AuthService start");
        System.out.println("AuthService start");
    }

    @Override
    public void stop() {
        logger.log(Level.INFO, "AuthService stop");
        System.out.println("AuthService stop");
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) throws SQLException {
        ResultSet set = statement.executeQuery("SELECT nick FROM users WHERE login = '" + login +
                        "'AND password = " + "'" + password + "'");
        return set.next() ? set.getString("nick") : null;
    }
}