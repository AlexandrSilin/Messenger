package main.java.clientside.one;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class EchoClient extends JFrame {

    private final Integer SERVER_PORT = 8081;
    private final String SERVER_ADDRESS = "localhost";
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean isAuthorized = false;
    private boolean isTimeout = false;

    private JTextField msgInputField;
    private JTextArea chatArea;

    public EchoClient() {
        try {
            connection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        prepareGUI();
    }

    public void connection() throws IOException {
        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());

        new Thread(() -> {
            try {
                sleep(120000);
                if (!isAuthorized){
                    isTimeout = true;
                    chatArea.append("Auth timeout (120 sec)\n");
                    closeConnection();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                sleep(2000);
                while (!isTimeout) {
                    String messageFromServer = dis.readUTF();
                    if (messageFromServer.startsWith("/authok")) {
                        isAuthorized = true;
                        chatArea.append(messageFromServer + "\n");
                        break;
                    }
                    chatArea.append(messageFromServer + "\n");
                }

                while (isAuthorized) {
                    String messageFromServer = dis.readUTF();
                    chatArea.append(messageFromServer + "\n");
                }
            } catch (IOException | InterruptedException ignored) {

            }
        }).start();
    }

    public void send() {
        if (msgInputField.getText() != null && !msgInputField.getText().trim().isEmpty()) {
            try {
                dos.writeUTF(msgInputField.getText());
                if (msgInputField.getText().equals("/end")) {
                    isAuthorized = false;
                    closeConnection();
                }
                msgInputField.setText("");
            } catch (IOException ignored) {
            }
        }
    }

    private void closeConnection() {
        try {
            dis.close();
            dos.close();
            socket.close();
            chatArea.append("Disconnected\n");
        } catch (IOException ignored) {
        }
    }

    /*public void onAuthClcik() {
        try {
            dos.writeUTF("/auth" + " " + loginField.getText() + " " + passwordField.getText());
            loginField.setText("");
            passwordField.setText("");
        } catch (IOException ignored) {
        }
    }*/


    public void prepareGUI() {

        setBounds(600, 300, 500, 500);
        setTitle("Клиент");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);


        JPanel bottomPanel = new JPanel(new BorderLayout());
        JButton btnSendMsg = new JButton("Отправить");
        bottomPanel.add(btnSendMsg, BorderLayout.EAST);
        msgInputField = new JTextField();
        add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.add(msgInputField, BorderLayout.CENTER);

        btnSendMsg.addActionListener(e -> {
            send();
        });

        msgInputField.addActionListener(e -> {
            send();
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);

            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new EchoClient();
        });
    }
}