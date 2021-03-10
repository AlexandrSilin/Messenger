package main.java.serverside.service;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class ClientHandler {

    private MyServer myServer;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Connection connection;
    private Statement statement;
    private File history;
    private FileInputStream fis;

    private String name;
    private long timer;

    public ClientHandler(MyServer myServer, Socket socket) {
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
            }

            new Thread(() -> {
                try {
                    authentication();
                    readMessage();
                } catch (IOException ignored) {
                } finally {
                    closeConnection();
                }

            }).start();

        } catch (IOException e) {
            closeConnection();
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
                    throwables.printStackTrace();
                }
                if (nick != null) {
                    if (!myServer.isNickBusy(nick)) {
                        sendMessage("/authok " + nick);
                        name = nick;
                        myServer.broadcastMessage("Hello " + name);
                        myServer.subscribe(this);
                        history = myServer.getHistory();
                        fis = new FileInputStream(history);
                        StringBuilder message = new StringBuilder();
                        int number;
                        int countMessages = 0;
                        try {
                            while ((number = fis.read()) != -1 && count < 100) {
                                message.append((char) number);
                                count++;
                            }
                        } catch (IOException e) {
                            System.out.println("Can't read history");
                        }
                        sendMessage(message.toString());
                    } else {
                        sendMessage("Nick is busy");
                    }
                }  else {
                    sendMessage("Wrong login and/or password");
                    count++;
                }
            } else {
                sendMessage("Wrong command");
            }
        }
        if (count == 3) {
            sendMessage("Number of attempts exceeded");
        }
    }

    public void readMessage() throws IOException {

        new Thread(() -> {
            timer = System.currentTimeMillis();
            while (true) {
                if (System.currentTimeMillis() - timer > 300000) {
                    sendMessage("Timeout session");
                    break;
                }
            }
            closeConnection();
        }).start();

        while (true) {
            String messageFromClient = dis.readUTF().trim();
            timer = System.currentTimeMillis();
            System.out.println(name + " send message " + messageFromClient);
            if (messageFromClient.startsWith("/")){
                if (messageFromClient.equals("/end")) {
                    return;
                }
                else if (messageFromClient.startsWith("/change")){
                    String [] arr = messageFromClient.split("\\s");
                    if (arr.length != 4){
                        sendMessage("syntax: /change login password newNick");
                    } else {
                        try {
                            statement.executeUpdate("update users set nick = '" + arr[3] + "' where " +
                                    "login = '" + arr[1] + "' and password = '" + arr[2] + "'");
                            sendMessage("Success change! Your new nick is " + arr[3]);
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
                    if (messages.length < 3) {
                        sendMessage("Incorrect format");
                    } else {
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
                    sendMessage("Wrong command");
                }
            } else {
                myServer.broadcastMessage(name + ": " + messageFromClient);
                myServer.addToHistory(name + ": " + messageFromClient);
            }
        }
    }

    public void sendMessage(String message) {
        try {
            dos.writeUTF(message);
        } catch (IOException ignored) {
        }
    }

    private void closeConnection() {
        myServer.unsubscribe(this);
        myServer.broadcastMessage(name + " Leave chat");
        try {
            dis.close();
            dos.close();
            socket.close();
        } catch (IOException ignored) {
        }
    }

    public String getName() {
        return name;
    }
}