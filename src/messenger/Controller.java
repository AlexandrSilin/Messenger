package messenger;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.IOException;

public class Controller {
    @FXML
    private TextArea mainWindow;

    @FXML
    private TextField messageField;

    private final Connection connection = Connection.getInstance();

    public Connection getConnection() {
        return connection;
    }

    @FXML
    private void buttonIsClicked(){
        try {
            String message = messageField.getText();
            if (!message.trim().isEmpty()) {
                connection.getOutputStream().writeUTF(message);
                connection.getOutputStream().flush();
                messageField.setText("");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receive(String message){
        mainWindow.appendText(message);
    }

    @FXML
    private void handleKeyPressed(KeyEvent event){
        if (event.getCode().equals(KeyCode.ENTER))
            buttonIsClicked();
    }
}
