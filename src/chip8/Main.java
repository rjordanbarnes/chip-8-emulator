package chip8;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Main extends Application {
    private final int SCREEN_WIDTH = 512;
    private final int SCREEN_HEIGHT = 256;
    private final int CYCLES_PER_FRAME = 7;
    private final int FRAMES_PER_SECOND = 60;
    private final int CYCLES_PER_SECOND = CYCLES_PER_FRAME * FRAMES_PER_SECOND;

    private final WritableImage SCREEN = new WritableImage(SCREEN_WIDTH, SCREEN_HEIGHT);
    private Scene mainScene;
    private boolean[] keys = new boolean[16];

    // Defines the colors used for the screen pixel states (0 or 1)
    private final int BLACK = 0xFF000000;
    private final int WHITE = 0xFFFFFFFF;
    private final PixelFormat<ByteBuffer> PIXELFORMAT = PixelFormat.createByteIndexedInstance(new int[] {BLACK, WHITE});

    @Override
    public void start(Stage mainStage) {
        startEmulation(mainStage);
    }

    private void startEmulation(Stage mainStage) {
        // Initializes a new Chip8 system
        Chip8System chip8System = new Chip8System(SCREEN_WIDTH, SCREEN_HEIGHT);

        // Creates screen and enables key press listeners
        setupGraphics(mainStage);
        setupInput(chip8System);

        try {
            chip8System.loadGame("./GAMES/pong");
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }

        // Game Loop, emulation speed depends on set Cycles Per Second
        Timeline timeline = new Timeline (new KeyFrame(Duration.seconds(1.0 / CYCLES_PER_SECOND), event -> {
            chip8System.emulateCycle();

            if (chip8System.getDrawFlag())
                drawGraphics(chip8System);

            // Tells the chip8 system which keys are currently pressed.
            chip8System.setKeys(keys);
        }));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    // Watches for button presses and releases
    private void setupInput(Chip8System chip8System) {
        mainScene.setOnKeyPressed(event -> {
            String codeString = event.getCode().toString();
            byte keyPad = convertKeyPressToKeyPad(codeString);
            chip8System.setLastKeyPressed(keyPad);
            toggleKey(keyPad, true);
        });

        mainScene.setOnKeyReleased(event -> {
            String codeString = event.getCode().toString();
            byte keyPad = convertKeyPressToKeyPad(codeString);
            toggleKey(keyPad, false);
        });
    }

    // Converts the key that the user pressed on their keyboard to the corresponding CHIP8 keypad input
    private byte convertKeyPressToKeyPad(String codeString) {
        switch (codeString) {
            case "X": return 0;
            case "DIGIT1": return 1;
            case "DIGIT2": return 2;
            case "DIGIT3": return 3;
            case "Q": return 4;
            case "W": return 5;
            case "E": return 6;
            case "A": return 7;
            case "S": return 8;
            case "D": return 9;
            case "Z": return 10;
            case "C": return 11;
            case "DIGIT4": return 12;
            case "R": return 13;
            case "F": return 14;
            case "V": return 15;
        }

        return -1;
    }

    // Presses or unpresses the specified key
    private void toggleKey(byte keyPad, Boolean isPressed) {
        keys[keyPad] = isPressed;
    }

    // Sets up the javafx graphics
    private void setupGraphics(Stage mainStage) {
        ImageView imageView = new ImageView(SCREEN);
        StackPane root = new StackPane(imageView);
        mainScene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        mainStage.setScene(mainScene);
        mainStage.setTitle("CHIP-8");
        mainStage.setResizable(false);
        mainStage.show();
    }

    // Draws the screen using the gfx bytestream in the given chip8system
    private void drawGraphics(Chip8System chip8System) {
        SCREEN.getPixelWriter().setPixels(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, PIXELFORMAT, chip8System.getGFX(), 0, SCREEN_WIDTH);
        chip8System.setDrawFlag(false);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
