package messenger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public  class Connection {

    private final String SERVER_ADDRESS = "localhost";
    private final Integer SERVER_PORT = 4444;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Socket client;
    private static Connection instance;

    private Connection(){
        try {
            client = new Socket(SERVER_ADDRESS, SERVER_PORT);
            inputStream = new DataInputStream(client.getInputStream()) ;
            outputStream = new DataOutputStream(client.getOutputStream());
        } catch (IOException e) {
            System.out.println("Can't connect to server");
            System.exit(1);
        }
    }

    public static Connection getInstance(){
        return instance == null ? instance = new Connection() : instance;
    }

    public DataInputStream getInputStream() {
        return inputStream;
    }

    public DataOutputStream getOutputStream() {
        return outputStream;
    }
}
