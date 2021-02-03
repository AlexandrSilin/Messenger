package messenger;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.awt.event.KeyListener;

public class Controller{

    @FXML
    private TextArea mainWindow;

    @FXML
    private TextField messageField;

    @FXML
    private void buttonIsClicked(){
        String message = messageField.getText();
        if (!message.trim().equals("")) {
            mainWindow.appendText("\n" + "userName: " + message);
            messageField.setText("");
        }
    }

    @FXML
    private void handleKeyPressed(KeyEvent event){
        if (event.getCode().equals(KeyCode.ENTER))
            buttonIsClicked();
    }
}
