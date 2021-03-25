package serverside.service;

import main.java.serverside.interfaces.AuthService;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {
    private final int PORT = 8081;
    private final Logger logger = LogManager.getLogger(MyServer.class);
    private List<ClientHandler> clients;

    private AuthService authService;

    public AuthService getAuthService() {
        return this.authService;
    }

    public MyServer() {
        try (ServerSocket server = new ServerSocket(PORT)){
            authService = new BaseAuthService();
            authService.start();

            clients = new ArrayList<>();
            while (true) {
                logger.log(Level.INFO, "server run");
                Socket socket = server.accept();
                logger.log(Level.INFO, "attempt to connect");
                new ClientHandler(this, socket);
            }

        } catch (IOException e){
            System.out.println("Server dead");
            logger.log(Level.FATAL, "Server dead");
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler c : clients) {
            c.sendMessage(message);
            logger.log(Level.INFO, "send broadcast message: " + message);
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

    public List<ClientHandler> getClients(){
        return clients;
    }
}