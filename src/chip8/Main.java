package chip8;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static final int SCALE = 1;

    @Override
    public void start(Stage mainStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("ui.fxml"));
        mainStage.setTitle("CHIP-8");
        mainStage.setResizable(false);
        mainStage.setScene(new Scene(root, 64 * SCALE, 32 * SCALE));
        mainStage.show();

        startEmulation();
    }

    private void startEmulation() {
        setupGraphics();
//        setupInput();

        // Initializes a new Chip8 system
        Chip8System chip8System = new Chip8System();

        try {
            chip8System.loadGame("./pong");
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }

        while (true) {
            chip8System.emulateCycle();

//            if (chip8System.drawFlag)
//                drawGraphics();

            chip8System.setKeys();
        }
    }

    private void setupGraphics() {

    }

    public static void main(String[] args) {
        launch(args);
    }
}
