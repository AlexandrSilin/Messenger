package main.java.serverside.service;

import main.java.serverside.interfaces.AuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {
    private final int PORT = 8081;
    private final int HISTORY_CAPACITY = 1000;

    private List<ClientHandler> clients;
    private List<String> history;

    private AuthService authService;

    public AuthService getAuthService() {
        return this.authService;
    }

    public MyServer() {
        try (ServerSocket server = new ServerSocket(PORT)){

            authService = new BaseAuthService();
            authService.start();

            clients = new ArrayList<>();
            history = new ArrayList<>();

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

    public void addToHistory(String message){
        if (history.size() > HISTORY_CAPACITY - 1){
            history.remove(0);
        }
        history.add(message);
    }

    public List<String> getHistory(){
        return history;
    }

    public List<ClientHandler> getClients(){
        return clients;
    }
}