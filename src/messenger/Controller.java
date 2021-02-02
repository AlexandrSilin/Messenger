package messenger;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class Controller {
    @FXML
    private TextArea mainWindow;

    @FXML
    private TextField messageField;

    @FXML
    public void buttonIsClicked(){
        String message = messageField.getText();
        if (!message.equals("")) {
            mainWindow.appendText("\n" + "userName: " + message);
            messageField.setText("");
        }
    }
}
