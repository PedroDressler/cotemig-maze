package Fabric.UI;

import Fabric.Objects.*;
import Fabric.Objects.Rat.Rat;
import Fabric.World.Block;
import Fabric.World.Maze;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Console implements UI {

    private final String ANSI_RESET = "\u001B[0m";
    private final String ANSI_CHEESE = "\u001B[33m";
    private final String ANSI_WALL = "\u001B[34m";
    private final String ANSI_RAT = "\u001B[37m";
    private Maze maze;
    private final StringBuilder canvas = new StringBuilder();
    private final List<String> messages = new ArrayList<>();

    public Console(Maze maze) {
        this.maze = maze;
    }

    public Console() {}

    public void setMaze(Maze maze) {
        this.maze = maze;
    }

    @Override
    public void draw() {
        for (int i = 0; i < maze.heigth(); i++) {
            for (int j = 0; j < maze.width(); j++) {
                GameObject object = maze.getBlock(i, j).getObjects().getLast();

                switch (object) {
                    case Wall _ -> canvas.append(ANSI_WALL + "█" + ANSI_RESET);
                    case Rat _ -> canvas.append(ANSI_RAT + "█" + ANSI_RESET);
                    case Target _ -> canvas.append(ANSI_CHEESE + "█" + ANSI_RESET);
                    case Path _ -> canvas.append("·");
                    case null, default -> canvas.append(" ");
                }
            }
            canvas.append("\n");
        }
        System.out.print(canvas + "\n\n");

        if (!canvas.isEmpty()) {
            System.out.println("=== MENSAGENS DO SISTEMA  ===");
            for (String message : messages) {
                System.out.println(message);
            }
//            messages.clear();
        }
    }

    @Override
    public void clear() {
        canvas.setLength(0);

        try {
            final String os = System.getProperty("os.name");

            if (os.contains("Windows"))
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            else
                new ProcessBuilder("clear").inheritIO().start().waitFor();

        } catch (final IOException | InterruptedException e) {
            System.out.println("Erro ao tentar limpar o console: " + e.getMessage());
        }
    }

    @Override
    public synchronized void sendNotification(String message) {
        messages.add(message);
    }

    public synchronized List<String> getMessages() {
        return messages;
    }
}
