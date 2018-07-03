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
    private final int WIDTH = 64;
    private final int HEIGHT = 32;
    private final int SCALE = 6;
    private final WritableImage SCREEN = new WritableImage(WIDTH, HEIGHT);

    private final int BLACK = 0xFF000000;
    private final int WHITE = 0xFFFFFFFF;
    private final PixelFormat<ByteBuffer> PIXELFORMAT = PixelFormat.createByteIndexedInstance(new int[] {BLACK, WHITE});

    @Override
    public void start(Stage mainStage) {
        startEmulation(mainStage);
    }

    private void startEmulation(Stage mainStage) {
        setupGraphics(mainStage);
//        setupInput();

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

                chip8System.setKeys();
            }

        }.start();
    }

    private void setupGraphics(Stage mainStage) {
        ImageView imageView = new ImageView(SCREEN);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(WIDTH * SCALE);
        imageView.setFitHeight(HEIGHT * SCALE);
        StackPane root = new StackPane(imageView);
        mainStage.setScene(new Scene(root, 400, 400));
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
