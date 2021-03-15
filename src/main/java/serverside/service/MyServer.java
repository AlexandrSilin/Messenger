package main.java.serverside.service;

import main.java.serverside.interfaces.AuthService;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MyServer {
    private final int PORT = 8081;

    private List<ClientHandler> clients;
    private File history;
    private FileOutputStream fos;

    private AuthService authService;

    public AuthService getAuthService() {
        return this.authService;
    }

    public MyServer() {
        history = new File("history.txt");
        try {
            fos = new FileOutputStream(history, true);
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        }

        try (ServerSocket server = new ServerSocket(PORT)){
            authService = new BaseAuthService();
            authService.start();

            clients = new ArrayList<>();
            while (true) {
                System.out.println("Сервер ожидает подключения");
                Socket socket = server.accept();
                System.out.println(socket.getInetAddress().getCanonicalHostName());
                System.out.println("Клиент подклчился");
                new ClientHandler(this, socket);
            }

        } catch (IOException e){
            System.out.println("Сервер не пережил землятрясение");
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }

    public synchronized void broadcastMessage(String message) {
        try {
            addToHistory(message);
        } catch (IOException e) {
            System.out.println("Can't write history");
        }
        for (ClientHandler c : clients) {
            c.sendMessage(message);
        }
    }

    public synchronized void subscribe(ClientHandler client) {
        clients.add(client);
    }

    public synchronized void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler c : clients) {
            if (c.getName().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public void addToHistory(String message) throws IOException {
        message += '\n';
        fos.write(message.getBytes(StandardCharsets.UTF_8));
    }

    public File getHistory(){
        return history;
    }

    public List<ClientHandler> getClients(){
        return clients;
    }
}