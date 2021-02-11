package messenger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Client extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Client.class.getResource("sample.fxml"));
        loader.load();
        primaryStage.setTitle("Messenger");
        Scene scene = new Scene(loader.getRoot(), 420, 540);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
        new Thread(() -> {
            while (true) {
                Controller controller = loader.getController();
                Connection connection = controller.getConnection();
                try {
                    String message;
                    if (!(message = connection.getInputStream().readUTF()).trim().isEmpty()) {
                        controller.receive(message);
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
