package Main;

import Fabric.Objects.Rat.CustomPhaser;
import Fabric.Objects.Rat.Rat;
import Fabric.Objects.Target;
import Fabric.Objects.Winner;
import Fabric.UI.Console;
import Fabric.World.Block;
import Fabric.World.Maze;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;

public class Program {
    public static void main(String[] args) {
        Console ui = new Console();

        ui.clear();

        Scanner scanner = new Scanner(System.in);

        System.out.print("Digite a altura do labirinto (blocos).\n> ");

    	final int MAZE_HEIGHT = scanner.nextInt();

        System.out.print("Digite a largura do labirinto (blocos).\n> ");

    	final int MAZE_WIDTH = scanner.nextInt();

        System.out.print("Quantos ratos irão para o gulag?\n> ");

    	final int N_RATS = scanner.nextInt();

    	final long WAIT_TIME = 200;
    	
        Maze maze = new Maze(MAZE_HEIGHT, MAZE_WIDTH);
        ui.setMaze(maze);
        Winner winner = new Winner();
        Random rand = new Random();

        List<int[]> availableFloorTiles = new ArrayList<>();

        maze.buildMaze(availableFloorTiles);
        
        List<Thread> ratThreads = new ArrayList<>();

        CustomPhaser phaser = new CustomPhaser(N_RATS, () -> {
            synchronized(maze.getAllBlocks()) {
                ui.clear();
                ui.draw();
            }
        });

        // Spawn rats
        for (int i = 0; i < N_RATS; i++) {
        	int[] startPos = availableFloorTiles.remove(rand.nextInt(availableFloorTiles.size()));

//            Rat rato = new Rat(maze, winner, ui, barrier, WAIT_TIME, startPos[0], startPos[1]);
            Rat rato = new Rat(maze, winner, ui, phaser, WAIT_TIME, startPos[0], startPos[1]);

        	Thread ratThread = new Thread(rato, "Rato-" + i);
        	ratThreads.add(ratThread);
        }

        ui.clear();
        ui.draw();

        try {
            Thread.sleep(3000);
        }  catch (InterruptedException e) {
            // ignore
        }

        ratThreads.forEach(Thread::start);
        
        for (Thread t : ratThreads) {
        	try {
        		t.join();
        	} catch (InterruptedException e) {
                // ignore
        	}
        }

        System.out.print(
                "\n\nBATTLE ROYALE #1\n\n" +
                "O " + winner.getWinner().getName() + " achou o queijo!\n" +
                "Ele deu " + winner.getWinner().getStepsTaken() + " passo(s) para a vitória."
        );
    }
}
