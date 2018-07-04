package chip8;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Main extends Application {
    private final int WIDTH = 512;
    private final int HEIGHT = 256;
    private final WritableImage SCREEN = new WritableImage(WIDTH, HEIGHT);
    private Scene mainScene;
    private boolean[] keys = new boolean[16];

    private final int BLACK = 0xFF000000;
    private final int WHITE = 0xFFFFFFFF;
    private final PixelFormat<ByteBuffer> PIXELFORMAT = PixelFormat.createByteIndexedInstance(new int[] {BLACK, WHITE});

    @Override
    public void start(Stage mainStage) {
        startEmulation(mainStage);
    }

    private void startEmulation(Stage mainStage) {
        setupGraphics(mainStage);
        setupInput();

        // Initializes a new Chip8 system
        Chip8System chip8System = new Chip8System();

        try {
            chip8System.loadGame("./GAMES/pong");
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }

        // Game Loop pulses at 60 Hz (60 FPS)
        new AnimationTimer() {

            public void handle(long now) {

                chip8System.emulateCycle();

                if (chip8System.getDrawFlag())
                    drawGraphics(chip8System);

                chip8System.setKeys(keys);
            }

        }.start();
    }

    private void setupInput() {
        mainScene.setOnKeyPressed(event -> {
            String codeString = event.getCode().toString();
            toggleKey(codeString, true);
        });

        mainScene.setOnKeyReleased(event -> {
            String codeString = event.getCode().toString();
            toggleKey(codeString, false);
        });
    }

    private void toggleKey(String codeString, Boolean isPressed) {
        switch (codeString) {
            case "X": keys[0] = isPressed; break;
            case "DIGIT1": keys[1] = isPressed; break;
            case "DIGIT2": keys[2] = isPressed; break;
            case "DIGIT3": keys[3] = isPressed; break;
            case "Q": keys[4] = isPressed; break;
            case "W": keys[5] = isPressed; break;
            case "E": keys[6] = isPressed; break;
            case "A": keys[7] = isPressed; break;
            case "S": keys[8] = isPressed; break;
            case "D": keys[9] = isPressed; break;
            case "Z": keys[10] = isPressed; break;
            case "C": keys[11] = isPressed; break;
            case "DIGIT4": keys[12] = isPressed; break;
            case "R": keys[13] = isPressed; break;
            case "F": keys[14] = isPressed; break;
            case "V": keys[15] = isPressed; break;
            default: break;
        }
    }

    private void setupGraphics(Stage mainStage) {
        ImageView imageView = new ImageView(SCREEN);
        StackPane root = new StackPane(imageView);
        mainScene = new Scene(root, WIDTH, HEIGHT);
        mainStage.setScene(mainScene);
        mainStage.setTitle("CHIP-8");
        mainStage.setResizable(false);
        mainStage.show();
    }

    private void drawGraphics(Chip8System chip8System) {
        SCREEN.getPixelWriter().setPixels(0, 0, WIDTH, HEIGHT, PIXELFORMAT, chip8System.getGFX(), 0, WIDTH);
        chip8System.setDrawFlag(false);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
