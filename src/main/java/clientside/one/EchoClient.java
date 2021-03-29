package main.java.clientside.one;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

public class EchoClient extends JFrame {

    private final Integer SERVER_PORT = 8081;
    private final String SERVER_ADDRESS = "localhost";

    private Socket socket;

    private DataInputStream dis;
    private DataOutputStream dos;
    private FileOutputStream fos;
    private RandomAccessFile accessFile;

    private File history;

    private ExecutorService executor;

    private boolean isAuthorized = false;
    private boolean isTimeout = false;

    private JTextField msgInputField;
    private JTextArea chatArea;

    public EchoClient() {
        executor = Executors.newCachedThreadPool();
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

        Thread timeout = new Thread(() -> {
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
        });

        Thread write = new Thread(() -> {
            try {
                sleep(2000);
                while (!isTimeout) {
                    String messageFromServer = dis.readUTF();
                    if (messageFromServer.startsWith("/authok")) {
                        isAuthorized = true;
                        chatArea.append(messageFromServer + "\n");
                        history = new File("history.txt");
                        accessFile = new RandomAccessFile(history, "r");
                        try {
                            fos = new FileOutputStream(history, true);
                            StringBuilder message = new StringBuilder();
                            int number;
                            int countMessages = 0;
                            int start = (int) history.length() - 1;
                            accessFile.seek(start);
                            try {
                                while (start > 0 && countMessages < 100) {
                                    number = accessFile.read();
                                    start--;
                                    accessFile.seek(start);
                                    message.insert(0, (char)number);
                                    if ((char) number == '\n'){
                                        countMessages++;
                                    }
                                }
                            } catch (IOException e) {
                                System.out.println("Can't read history");
                            }
                            chatArea.append(message.toString());
                        } catch (FileNotFoundException e) {
                            System.out.println("File not found");
                        }
                        break;
                    }
                    chatArea.append(messageFromServer + "\n");
                }

                while (isAuthorized) {
                    String messageFromServer = dis.readUTF() + '\n';
                    fos.write(messageFromServer.getBytes(StandardCharsets.UTF_8));
                    chatArea.append(messageFromServer);

                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        executor.execute(timeout);
        executor.execute(write);
    }

    public void send() {
        if (msgInputField.getText() != null && !msgInputField.getText().trim().isEmpty()) {
            try {
                String message = msgInputField.getText();
                dos.writeUTF(message);
                if (msgInputField.getText().equals("/end")) {
                    isAuthorized = false;
                    fos.close();
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
            executor.shutdownNow();
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
        SwingUtilities.invokeLater(EchoClient::new);
    }
}