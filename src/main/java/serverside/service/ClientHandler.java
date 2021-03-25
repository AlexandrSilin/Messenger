package serverside.service;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import main.java.serverside.service.Connect;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ClientHandler {

    private MyServer myServer;
    private Socket socket;

    private DataInputStream dis;
    private DataOutputStream dos;

    private Connection connection;
    private Statement statement;

    private String name;
    private long timer;

    private ExecutorService executor;
    private final Logger logger = LogManager.getLogger(BaseAuthService.class);

    public ClientHandler(MyServer myServer, Socket socket) {
        executor = Executors.newCachedThreadPool();
        try {
            this.myServer = myServer;
            this.socket = socket;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.name = null;

            try {
                connection = Connect.getConnection();
                statement = connection.createStatement();
            } catch (SQLException | ClassNotFoundException throwables) {
                throwables.printStackTrace();
                logger.log(Level.ERROR, "Problem with connection");
            }

            Thread clientAuth = new Thread(() -> {
                try {
                    authentication();
                    readMessage();
                } catch (IOException exception) {
                    logger.log(Level.ERROR, "IO error");
                } finally {
                    closeConnection();
                }
            });

            executor.execute(clientAuth);
        } catch (IOException e) {
            closeConnection();
            logger.log(Level.ERROR, "Problem with ClientHandler");
            throw new RuntimeException("Problem with ClientHandler");
        }
    }

    public void authentication() throws IOException {
        sendMessage("type /auth [login] [password] for authentication");
        int count = 0;
        while (count < 3 && name == null) {
            String str = dis.readUTF().trim();
            if (str.startsWith("/auth")) { //  /auth login password
                String [] arr = str.split("\\s");
                String nick = null;
                try {
                    nick = myServer
                            .getAuthService()
                            .getNickByLoginAndPassword(arr[1], arr[2]);
                } catch (SQLException throwables) {
                    logger.log(Level.ERROR, "SQL error");
                    throwables.printStackTrace();
                }
                if (nick != null) {
                    if (!myServer.isNickBusy(nick)) {
                        sendMessage("/authok " + nick);
                        name = nick;
                        myServer.broadcastMessage("Hello " + name);
                        myServer.subscribe(this);
                        logger.log(Level.INFO, "user " + name + " is authorized");
                    } else {
                        logger.log(Level.WARN, nick + " nick is busy");
                        sendMessage("Nick is busy");
                    }
                }  else {
                    sendMessage("Wrong login and/or password");
                    logger.log(Level.WARN, "Wrong login and/or password");
                    count++;
                }
            } else {
                logger.log(Level.WARN, "Wrong command");
                sendMessage("Wrong command");
            }
        }
        if (count == 3) {
            logger.log(Level.WARN, "Number of attempts exceeded");
            sendMessage("Number of attempts exceeded");
        }
    }

    public void readMessage() throws IOException {

        Thread clientMessage = new Thread(() -> {
            timer = System.currentTimeMillis();
            while (true) {
                if (System.currentTimeMillis() - timer > 300000) {
                    logger.log(Level.WARN, "Timeout session");
                    sendMessage("Timeout session");
                    break;
                }
            }
            closeConnection();
        });

        executor.execute(clientMessage);
        while (true) {
            String messageFromClient = dis.readUTF().trim();
            timer = System.currentTimeMillis();
            if (messageFromClient.startsWith("/")){
                if (messageFromClient.equals("/end")) {
                    logger.log(Level.INFO, name + " disconnect");
                    executor.shutdownNow();
                    return;
                }
                else if (messageFromClient.startsWith("/change")){
                    logger.log(Level.INFO, name + " tries to change nick");
                    String [] arr = messageFromClient.split("\\s");
                    if (arr.length != 4){
                        sendMessage("syntax: /change login password newNick");
                    } else {
                        try {
                            statement.executeUpdate("update users set nick = '" + arr[3] + "' where " +
                                    "login = '" + arr[1] + "' and password = '" + arr[2] + "'");
                            sendMessage("Success change! Your new nick is " + arr[3]);
                            logger.log(Level.INFO, name + " changed nick to " + arr[3]);
                            name = arr[3];
                        } catch (SQLException throwables) {
                            sendMessage("Can't change nick");
                            throwables.printStackTrace();
                        }
                    }
                }
                else if (messageFromClient.startsWith("/w")) {
                    List<ClientHandler> clients = myServer.getClients();
                    String[] messages = messageFromClient.split("\\s", 2);
                    if (messages[2].trim().isEmpty()) {
                        logger.log(Level.WARN, name + " input incorrect format for personal message");
                        sendMessage("Incorrect format");
                    } else {
                        logger.log(Level.INFO, name + " tries to send: " + messages[2] + " for " + messages[1]);
                        for (ClientHandler client : clients) {
                            if (client.getName().equals(messages[1])) {
                                client.sendMessage(messages[2]);
                                break;
                            }
                            sendMessage("User offline");
                        }
                    }
                }
                else {
                    logger.log(Level.WARN, name + " input wrong command");
                    sendMessage("Wrong command");
                }
            } else {
                myServer.broadcastMessage(name + ": " + messageFromClient);
            }
        }
    }

    public void sendMessage(String message) {
        try {
            dos.writeUTF(message);
        } catch (IOException e) {
            logger.log(Level.ERROR, "IO error");
        }
    }

    private void closeConnection() {
        myServer.unsubscribe(this);
        myServer.broadcastMessage(name + " Leave chat");
        try {
            dis.close();
            dos.close();
            socket.close();
            logger.log(Level.INFO, name + " disconnect");
        } catch (IOException e) {
            logger.log(Level.ERROR, "IO error");
        }
    }

    public String getName() {
        return name;
    }
}