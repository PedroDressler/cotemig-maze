package Fabric.Objects.Rat;

import Fabric.Objects.*;
import Fabric.Types.RatDirection;
import Fabric.UI.UI;
import Fabric.World.Block;
import Fabric.World.Maze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

public class Rat extends GameObject implements Runnable {

    private final Maze maze;
    private final Winner winner;
    private final Stack<History> history = new Stack<>();
    private final Phaser phaser;
    private final UI ui;

    private final long waitTime;
    private final int startX;
    private final int startY;
    private String name;
    private AtomicInteger stepsTaken = new AtomicInteger(0);
//    private final CyclicBarrier barrier;

    public Rat(Maze maze, Winner winner, UI ui, Phaser phaser, long waitTime, int startX, int startY) {
        super();
        this.maze = maze;
        this.winner = winner;
        this.ui = ui;
        this.waitTime = waitTime;
        this.startX = startX;
        this.startY = startY;
        this.phaser = phaser;

        synchronized(maze.getAllBlocks()) {
            if (maze.getBlock(startX, startY) == null) {
                maze.setBlock(startX, startY, new Floor());
            }
            maze.add(startX, startY, this);
        }
    }
    
    @Override
    public void run() {
        this.name = Thread.currentThread().getName();
        ui.sendNotification("O " + this.getName() + " foi invocado.");
        if (backtrack(this.startX, this.startY)) {
            synchronized (winner) {
                winner.setWinner(this);
            }
            phaser.forceTermination();
        } else {
            if (!winner.isDecided()) {
                ui.sendNotification("O " + this.getName() + " se perdeu no meio do caminho.");
            } else {
                ui.sendNotification("O " + this.getName() + " foi abduzido.");
            }
        }
        phaser.arriveAndDeregister();
    }

    private boolean backtrack(final int x, final int y) {


        for (RatDirection ratDirection : randomizeMoves()) {
            if (winner.isDecided()) return false;
        	
            int stepX = x + ratDirection.x();
            int stepY = y + ratDirection.y();
            
            GameObject nextStepObject;
            
            synchronized(maze.getAllBlocks()) {
            	nextStepObject = maze.getBottomObject(stepX, stepY);
            }
            
            if (nextStepObject instanceof Target) {
            	stepUpRat(stepX, stepY, ratDirection);
            	render();
            	return true;
            }
            
            if (isSafe(stepX, stepY) && !isExplored(stepX, stepY)) {
                if (winner.isDecided()) return false;

                stepUpRat(stepX, stepY, ratDirection);
                if (!render()) {
                    return false;
                }

                if (backtrack(stepX, stepY)) return true;

                stepBackRat(x, y);
                if (!render()) {
                    return false;
                }
            }
        }
        return false;
    }

    private boolean isSafe(final int x, final int y) {
        boolean isOnBounds = x >= 1 && x < maze.heigth() - 1 && y >= 1 && y < maze.width() - 1;
        if (!isOnBounds) return false;
        
        boolean isBlocked;

        synchronized(maze.getAllBlocks()) {
        	GameObject topObject = maze.getBottomObject(x, y);
        	
        	isBlocked = (topObject instanceof Wall) || (topObject instanceof Rat) ||
        			(topObject instanceof Path && ((Path)topObject).getRat() != this);
        }

        return !isBlocked;
    }

    private boolean isExplored(final int x, final int y) {
        List<GameObject> objectsCopy;
        
        synchronized(maze.getAllBlocks()) {
        	Block block = maze.getBlock(x, y);
        	
        	if (block == null) return false;
        	
        	objectsCopy = block.getObjects();
        }
        
        for (GameObject object : objectsCopy) {
        	if (object instanceof Path) {
        		if (((Path) object).getRat() == this) return true;
        	}
        }
        return false;
    }

    private RatDirection[] randomizeMoves() {
        List<RatDirection> ratDirections = Arrays.asList(RatDirection.values());
        RatDirection[] availableRatDirections = new RatDirection[ratDirections.size()];
        Collections.shuffle(ratDirections);

        for (int i = 0; i < ratDirections.size(); i++) {
            availableRatDirections[i] = ratDirections.get(i);
        }

        return availableRatDirections;
    }

    private void stepUpRat(final int x, final int y, RatDirection ratDirection) {
        int stepX = x - ratDirection.x();
        int stepY = y - ratDirection.y();

        Path path = new Path(this);
        history.push(new History(x, y, path));

        synchronized (maze.getAllBlocks()) {
            maze.remove(stepX, stepY, this);
            maze.add(stepX, stepY, path);

            if (maze.getBlock(x, y) == null) {
                maze.setBlock(x, y, new Floor());
            }

            maze.add(x, y, this);
        }

        stepsTaken.incrementAndGet();

//        ui.sendNotification("Rat " + Thread.currentThread().getName() + " stepped from (" + x + ", " + y + ") to (" + stepX + ", " + stepY + ").");
    }

    private void stepBackRat(final int oldX, final int oldY) {
        History back = history.pop();

        synchronized (maze.getAllBlocks()) {
            maze.remove(back.x(), back.y(), this);
            maze.remove(oldX, oldY, back.path());
            maze.add(oldX, oldY, this);
        }

        stepsTaken.incrementAndGet();

//        ui.sendNotification("Rat " + Thread.currentThread().getName() + " stepped from (" + back.x() + ", " + back.y() + ") to (" + oldX + ", " + oldY + ").");
    }

    private boolean render() {
        // barrier logic
//        try {
//            barrier.await();
//        } catch (InterruptedException e) {
//            // ignore
//        } catch (BrokenBarrierException e) {
//            throw new RuntimeException(e);
//        }

        int currentPhase = phaser.arriveAndAwaitAdvance();

        if (currentPhase < 0) {
            return false;
        }

        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            // ignore
        }

        return true;
    }

    public int getStepsTaken() {
        return this.stepsTaken.get();
    }

    public String getName() {
        return this.name;
    }


}

