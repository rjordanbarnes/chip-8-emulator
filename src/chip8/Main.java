package chip8;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class Main extends Application {
    private final int SCREEN_WIDTH = 64;
    private final int SCREEN_HEIGHT = 32;
    private final int SCALE = 16;
    private final int CYCLES_PER_FRAME = 7;
    private final int FRAMES_PER_SECOND = 60;
    private final int CYCLES_PER_SECOND = CYCLES_PER_FRAME * FRAMES_PER_SECOND;

    private Scene mainScene;
    private GraphicsContext gc;
    private final Color drawColor = Color.WHITE;
    private final Color backgroundColor = Color.BLACK;

    private boolean[] keys = new boolean[16];

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
        StackPane root = new StackPane();
        mainScene = new Scene(root, SCREEN_WIDTH * SCALE, SCREEN_HEIGHT * SCALE);
        Canvas canvas = new Canvas(SCREEN_WIDTH * SCALE, SCREEN_HEIGHT * SCALE);
        gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);
        mainStage.setScene(mainScene);
        mainStage.setTitle("CHIP-8");
        mainStage.setResizable(false);
        mainStage.show();
    }

    // Scales and draws the screen using the pixels in the given chip8system
    private void drawGraphics(Chip8System chip8System) {

        for (int x = 0; x < SCREEN_WIDTH; x++) {
            for (int y = 0; y < SCREEN_HEIGHT; y++) {
                if (chip8System.getPixel(x, y) == 1) {
                    gc.setFill(drawColor);
                } else {
                    gc.setFill(backgroundColor);
                }

                gc.fillRect(x * SCALE, y * SCALE, SCALE, SCALE);
            }
        }

        chip8System.setDrawFlag(false);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
