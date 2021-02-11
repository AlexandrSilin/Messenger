package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(4444)){
            System.out.println("Server is running");
            Socket socket = serverSocket.accept();
            System.out.println("Someone connected");
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeUTF("From server: Username connected\n");
            outputStream.flush();
            new Thread(() -> {
                try {
                    DataOutputStream serverOutput = new DataOutputStream(socket.getOutputStream());
                    while (true) {
                        try {
                            serverOutput.writeUTF("From server: " + new Scanner(System.in).nextLine() + "\n");
                            serverOutput.flush();
                        } catch (IOException e) {
                            System.out.println("Server message send error");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            while (true){
                String message = inputStream.readUTF();
                if (message.trim().equals("/finish")) {
                    break;
                }
                outputStream.writeUTF("User: " + message + "\n");
                outputStream.flush();
            }
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("disconnect");
            System.exit(0);
        }
    }
}