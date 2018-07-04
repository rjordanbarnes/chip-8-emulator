package chip8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class Chip8System {
    private final int WIDTH = 512;
    private final int HEIGHT = 256;

    private char opcode;
    private Boolean drawFlag = false;

    private byte[] memory = new byte[4096];
    private byte[] registers = new byte[16];
    private char[] stack = new char[16];
    private byte[] gfx = new byte[WIDTH * HEIGHT];

    private char indexRegister = 0;
    private char programCounter = 0x200;
    private char stackPointer = 0; // Points to the next empty index of the stack.

    private byte delay_timer = 60;
    private byte sound_timer = 60;

    private boolean[] keys;

    private final int[] CHIP8_FONTSET =
            {
                    0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
                    0x20, 0x60, 0x20, 0x20, 0x70, // 1
                    0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
                    0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
                    0x90, 0x90, 0xF0, 0x10, 0x10, // 4
                    0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
                    0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
                    0xF0, 0x10, 0x20, 0x40, 0x40, // 7
                    0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
                    0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
                    0xF0, 0x90, 0xF0, 0x90, 0x90, // A
                    0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
                    0xF0, 0x80, 0x80, 0x80, 0xF0, // C
                    0xE0, 0x90, 0x90, 0x90, 0xE0, // D
                    0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
                    0xF0, 0x80, 0xF0, 0x80, 0x80  // F
            };

    public Chip8System() {
        // Load font set
        for (int i = 0; i < CHIP8_FONTSET.length; i++) {
            memory[i] = (byte) CHIP8_FONTSET[i];
        }
    }

    public void loadGame(String game) throws IOException {
        Path path = Paths.get(game);

        byte[] fileContents = Files.readAllBytes(path);

        // Load game into memory starting at 0x200
        for (int i = 0; i < fileContents.length; i++) {
            memory[i + 0x200] = fileContents[i];
        }
    }

    public Boolean getDrawFlag() {
        return drawFlag;
    }

    public void setDrawFlag(Boolean drawFlag) {
        this.drawFlag = drawFlag;
    }

    public byte[] getGFX() {
        return gfx;
    }

    public void setKeys(boolean[] keys) {
        this.keys = keys;
    }


    public int getPixel(int x, int y) {
        return gfx[x + WIDTH * y];
    }

    // Draws an 8 x 8 box at coordinate (x, y)
    public void drawBox(int x, int y) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                drawPixel(x + i, y + j);
            }
        }
    }

    // Draws a single pixel at coordinate (x, y)
    public void drawPixel(int x, int y) {
        gfx[x + WIDTH * y] ^= 1;
    }

    public void emulateCycle() {
        // Fetch Opcode
        opcode = (char)((memory[programCounter] << 8) | (memory[programCounter + 1] & 0x00FF));

        char X = (char) ((opcode & 0x0F00) >>> 8);
        char Y = (char) ((opcode & 0x00F0) >>> 4);

        // Decode Opcode
        switch (opcode & 0xF000) {

            case 0x0000:
                switch (opcode & 0x000F) {
                    case 0x0000: // 0x00E0: Clears screen
                        for (int i = 0; i < gfx.length; i++) {
                            gfx[i] = 0;
                        }

                        programCounter += 2;
                        drawFlag = true;
                        System.out.println(String.format("0x%04x", (int) opcode) + ": clears screen");
                        break;

                    case 0x000E: // 0x00EE: Returns from subroutine
                        stackPointer--;
                        programCounter = stack[stackPointer];
                        programCounter += 2;
                        System.out.println(String.format("0x%04x", (int) opcode) + ": returns from subroutine");
                        break;

                    default:
                        System.out.println("Unknown opcode: " + String.format("0x%04x", (int) opcode));
                }
                break;

            case 0x1000: // 0x1NNN: Jumps to address NNN.
                programCounter = (char) (opcode & 0x0FFF);
                System.out.println(String.format("0x%04x", (int) opcode) + ": jumps to address " + String.format("0x%04x", opcode & 0x0FFF));
                break;

            case 0x2000: // 0x2NNN: Calls subroutine at NNN.
                stack[stackPointer] = programCounter;
                stackPointer++;

                programCounter = (char) (opcode & 0x0FFF);
                System.out.println(String.format("0x%04x", (int) opcode) + ": calls subroutine at " + String.format("0x%04x", opcode & 0x0FFF));
                break;

            case 0x3000: // 0x3XNN: Skips the next instruction if VX equals NN.
                if (registers[X] == (opcode & 0x00FF))
                    programCounter += 4;
                else
                    programCounter += 2;

                System.out.println(String.format("0x%04x", (int) opcode) + ": skips the next instruction if " + String.format("0x%04x", registers[X]) + " == " + String.format("0x%04x", opcode & 0x00FF));
                break;

            case 0x4000: // 0x4XNN: Skips the next instruction if VX doesn't equal NN.
                if (registers[X] != (opcode & 0x00FF))
                    programCounter += 4;
                else
                    programCounter += 2;

                System.out.println(String.format("0x%04x", (int) opcode) + ": skips the next instruction if " + String.format("0x%04x", registers[X]) + " != " + String.format("0x%04x", opcode & 0x00FF));
                break;

            case 0x6000: // 0x6XNN: Sets VX to NN.
                registers[X] = (byte) (opcode & 0x00FF);

                programCounter += 2;
                System.out.println(String.format("0x%04x", (int) opcode) + ": sets register " + String.format("0x%04x", (short)X) + " to " + String.format("0x%04x", opcode & 0x00FF));
                break;

            case 0x7000: // 0x7XNN: Adds NN to VX.
                registers[X] += (opcode & 0x00FF);

                programCounter += 2;
                System.out.println(String.format("0x%04x", (int) opcode) + ": adds " + String.format("0x%04x", opcode & 0x00FF) + " to register " + String.format("0x%04x", (short)X));
                break;

            case 0x8000:
                switch (opcode & 0x000F) {
                    case 0x0000: // 0x8XY0: Sets VX to the value of VY.
                        registers[X] = registers[Y];

                        programCounter += 2;
                        System.out.println(String.format("0x%04x", (int) opcode) + ": sets register " + String.format("0x%04x", (short)X) + " to value of register " + String.format("0x%04x", (short)Y));
                        break;

                    case 0x0002: // 0x8XY2: Sets VX to VX and VY.
                        registers[X] &= registers[Y];
                        programCounter += 2;
                        System.out.println(String.format("0x%04x", (int) opcode) + ": sets register " + String.format("0x%04x", (short)X) + " to value of register " + String.format("0x%04x", (short)X) + " & " + " register " + String.format("0x%04x", (short)Y));
                        break;

                    case 0x0004: // 0x8XY4: Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't.
                        if (registers[Y] + registers[X] > 0xFF)
                            registers[0xF] = 1; // carry
                        else
                            registers[0xF] = 0;

                        registers[X] += registers[Y];
                        registers[X] &= 0xFF;
                        programCounter += 2;
                        System.out.println(String.format("0x%04x", (int) opcode) + ": adds value of register " + String.format("0x%04x", (short) Y) + " to register " + String.format("0x%04x", (short)X));
                        break;

                    case 0x0005: // 0x8XY5: VY is subtracted from VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
                        if (registers[X] < registers[Y])
                            registers[0xF] = 0; // borrow
                        else
                            registers[0xF] = 1;

                        registers[X] -= registers[Y];
                        registers[X] &= 0xFF;
                        programCounter += 2;
                        System.out.println(String.format("0x%04x", (int) opcode) + ": subtracts value of register " + String.format("0x%04x", (short)Y) + " from register " + String.format("0x%04x", (short)X));
                        break;

                    default:
                        System.out.println("Unknown opcode: " + String.format("0x%04x", (int) opcode));
                }
                break;

            case 0xA000: // 0xANNN: Sets I to the address NNN.
                indexRegister = (char) (opcode & 0x0FFF);

                programCounter += 2;
                System.out.println(String.format("0x%04x", (int) opcode) + ": sets instruction pointer to " + String.format("0x%04x", opcode & 0x0FFF));
                break;

            case 0xC000: // 0xCXNN: Sets VX to the result of a bitwise and operation on a random number (Typically: 0 to 255) and NN.
                Random r = new Random();

                registers[X] = (byte) (r.nextInt(256) & (opcode & 0x00FF));
                programCounter += 2;
                break;

            case 0xD000: // 0xDXYN: Draws a sprite at coordinate (VX, VY) that has a width of 8 pixels and a height of N pixels.
                int xCoord = registers[X] * 8;
                int yCoord = registers[Y] * 8;
                int verticalBoxes = opcode & 0x000F;

                registers[0xF] = 0; // Clear carry

                // For each vertical box needed, draw four boxes horizontally if sprite requires
                for (int i = 0; i < verticalBoxes; i++) {
                    int scaledYCoord = yCoord + 8 * i;

                    for (int j = 0; j < 4; j++) {
                        int scaledXCoord = xCoord + 8 * j;

                        if ((memory[indexRegister + i] & (0x80 >>> j)) != 0) { // If sprite says to draw this box, draw it
                            if (getPixel(scaledXCoord, scaledYCoord) == 1) // Collision
                                registers[0xF] = 1;

                            drawBox(scaledXCoord, scaledYCoord);
                        }
                    }
                }

                drawFlag = true;
                programCounter += 2;

                System.out.println(String.format("0x%04x", (int) opcode) + ": draws a sprite at coordinate (" + registers[X] + ", " + registers[Y] + ") that has a width of 8 pixels and a height of " + (short) verticalBoxes + " pixels");
                break;

            case 0xE000:
                switch(opcode & 0x00FF) {
                    case 0x009E: // 0xEX9E: Skips the next instruction if the key stored in VX is pressed.
                        if (keys[registers[X]])
                            programCounter += 4;
                        else
                            programCounter += 2;

                        break;

                    case 0x00A1: // 0xEXA1: Skips the next instruction if the key stored in VX isn't pressed.
                        if (!keys[registers[X]])
                            programCounter += 4;
                        else
                            programCounter += 2;

                        break;

                    default:
                        System.out.println("Unknown opcode: " + String.format("0x%04x", (int) opcode));
                }
                break;

            case 0xF000:
                switch (opcode & 0x00FF) {
                    case 0x0007: // 0xFX07: Sets VX to the value of the delay timer.
                        registers[X] = (byte) (delay_timer & 0xFF);

                        programCounter += 2;
                        break;

                    case 0x0015: // 0xFX15: Sets the delay timer to VX.
                        delay_timer = registers[X];

                        programCounter += 2;
                        break;

                    case 0x0018: // 0xFX18: Sets the sound timer to VX.
                        sound_timer = registers[X];

                        programCounter += 2;
                        break;

                    case 0x0029: // 0xFX29: Sets I to the location of the sprite for the character in VX.
                        indexRegister = (char) (registers[X] * 5); // Sprites 5 bytes long

                        programCounter += 2;
                        break;

                    case 0x0033: // 0xFX33: Stores the binary-coded decimal representation of VX
                        memory[indexRegister] = (byte) (registers[X] / 100);
                        memory[indexRegister + 1] = (byte) ((registers[X] / 10) % 10);
                        memory[indexRegister + 2] = (byte) ((registers[X] % 100) % 10);

                        programCounter += 2;
                        break;

                    case 0x0065: // 0xFX65: Fills V0 to VX (including VX) with values from memory starting at address I.
                        for (int i = 0; i <= X; i++) {
                            registers[i] = (byte) (memory[indexRegister + i] & 0xFF);
                        }

                        indexRegister = (char) ((indexRegister + X + 1) & 0xFFFF);
                        programCounter+= 2;
                        break;

                    default:
                        System.out.println("Unknown opcode: " + String.format("0x%04x", (int) opcode));
                }
                break;

            default:
                System.out.println("Unknown opcode: " + String.format("0x%04x", (int) opcode));
        }

        // Update timers
        if (delay_timer > 0)
            delay_timer--;

        if (sound_timer > 0) {
            if (sound_timer == 1)
                System.out.println("BEEP!");

            sound_timer--;
        }
    }
}
