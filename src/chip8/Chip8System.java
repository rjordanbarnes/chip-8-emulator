package chip8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Chip8System {
    private int opcode;

    private int[] memory = new int[4096];
    private int[] registers = new int[16];
    private int[] stack = new int[16];

    private int indexRegister = 0;
    private int programCounter = 0x200;
    private int stackPointer = 0;

    private int delay_timer = 60;
    private int sound_timer = 60;

    private int[] key = new int[16];

    public Chip8System() {
    }

    public void loadGame(String game) throws IOException {
        Path path = Paths.get(game);

        byte[] fileContents = Files.readAllBytes(path);

        // Load game into memory starting at 0x200
        for (int i = 0; i < fileContents.length; i++) {
            memory[i + 0x200] = fileContents[i];
        }
    }

    public void setKeys() {

    }

    public void emulateCycle() {
        int X;
        int Y;

        // Fetch Opcode
        int opcode1 = memory[programCounter] << 8;
        int opcode2 = memory[programCounter + 1] & 0x00FF;
        opcode = opcode1 | opcode2;

        // Decode Opcode
        switch (opcode & 0xF000) {

            case 0x0000:
                switch (opcode & 0x000F) {
                    case 0x0000: // 0x00E0: Clears screen
                        ;
                        break;

                    case 0x000E: // 0x00EE: Returns from subroutine
                        ;
                        break;

                    default:
                        System.out.println("Unknown opcode: " + String.format("0x%04X", opcode));
                }
                break;

            case 0x2000: // 0x2NNN: Calls subroutine at NNN.
                stack[stackPointer] = programCounter;
                stackPointer++;
                programCounter = opcode & 0x0FFF;
                break;

            case 0x6000: // 0x6XNN: Sets VX to NN.
                X = (opcode & 0x0F00) >> 8;
                registers[X] = opcode & 0x00FF;
                programCounter += 2;
                break;

            case 0x8000:
                switch (opcode & 0x000F) {
                    case 0x0004: // 0x8XY4: Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't.
                        X = (opcode & 0x0F00) >> 8;
                        Y = (opcode & 0x00F0) >> 4;

                        if (registers[Y] > (0xFF - registers[X]))
                            registers[0xF] = 1; // carry
                        else
                            registers[0xF] = 0;

                        registers[X] += registers[Y];
                        programCounter += 2;
                        break;

                    default:
                        System.out.println("Unknown opcode: " + String.format("0x%04X", opcode));
                }
                break;

            case 0xA000: // 0xANNN: Sets I to the address NNN.
                indexRegister = opcode & 0x0FFF;
                programCounter += 2;
                break;

            case 0xF000:
                switch (opcode & 0x00FF) {
                    case 0x0033: // 0xFX33: Stores the binary-coded decimal representation of VX
                        X = (opcode & 0x0F00) >> 8;
                        memory[indexRegister] = registers[X] / 100;
                        memory[indexRegister + 1] = (registers[X] / 10) % 10;
                        memory[indexRegister + 2] = (registers[X] % 100) % 10;
                        programCounter += 2;
                        break;

                    default:
                        System.out.println("Unknown opcode: " + String.format("0x%04X", opcode));
                }
                break;

            default:
                System.out.println("Unknown opcode: " + String.format("0x%04X", opcode));
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
