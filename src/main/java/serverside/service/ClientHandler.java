package main.java.serverside.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class ClientHandler {
    
    private MyServer myServer;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    
    private String name;
    
    
    public ClientHandler(MyServer myServer, Socket socket) {
        try {
            
            this.myServer = myServer;
            this.socket = socket;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            
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
        int count = 0;
        while (count < 3) {
            String str = dis.readUTF();
            if (str.startsWith("/auth")) { //  /auth login password
                String [] arr = str.split("\\s");
                String nick = myServer
                        .getAuthService()
                        .getNickByLoginAndPassword(arr[1], arr[2]);
                if (nick != null) {
                    if (!myServer.isNickBusy(nick)) {
                        sendMessage("/authok " + nick);
                        name = nick;
                        myServer.broadcastMessage("Hello " + name);
                        myServer.subscribe(this);
                        List<String> history = myServer.getHistory();
                        if (history.size() > 50) {
                            for (int i = 1; i <= 50; i++){
                                sendMessage(history.get(history.size() - i));
                            }
                        } else {
                            for (int i = history.size() - 1; i > 0; i--){
                                sendMessage(history.get(i));
                            }
                        }
                        return;
                    } else {
                        sendMessage("Nick is busy");
                    }
                } else {
                    sendMessage("Wrong login and/or password");
                    count++;
                }
            }
        }
        sendMessage("Number of attempts exceeded");
    }

    public void readMessage() throws IOException {
        while (true) {
            String messageFromClient = dis.readUTF();
            System.out.println(name + " send message " + messageFromClient);
            if (messageFromClient.equals("/end")) {
                return;
            }
            if (messageFromClient.startsWith("/w")){
                List <ClientHandler> clients = myServer.getClients();
                String[] messages = messageFromClient.split("\\s");
                if (messages.length < 3){
                    sendMessage("Incorrect format");
                } else {
                    for (ClientHandler client : clients) {
                        if (client.getName().equals(messages[1])) {
                            StringBuilder out = new StringBuilder();
                            out.append("Private message from ").append(name).append(": ");
                            for (int i = 2; i < messages.length; i++) {
                                out.append(messages[i]).append(" ");
                            }
                            client.sendMessage(out.toString().trim());
                            break;
                        }
                        sendMessage("User offline");
                    }
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
