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

public class Rat extends GameObject implements Runnable {

    private final Maze maze;
    private final Winner winner;
    private final long waitTime;
    private final UI ui;
    private final Stack<History> history = new Stack<>();
    
    private final int startX;
    private final int startY;
    private final CyclicBarrier barrier;
    
    public Rat(Maze maze, Winner winner, long waitTime, UI ui, int startX, int startY, CyclicBarrier barrier) {
        super();
        this.maze = maze;
        this.winner = winner;
        this.waitTime = waitTime;
        this.ui = ui;
        this.startX = startX;
        this.startY = startY;
        this.barrier = barrier;

        synchronized(maze.getAllBlocks()) {
            if (maze.getBlock(startX, startY) == null) {
                maze.setBlock(startX, startY, new Floor());
            }
            maze.add(startX, startY, this);
        }
    }
    
    @Override
    public void run() {
        if (winner.getWinner() != null) return;

        if (backtrack(this.startX, this.startY)) {
            triggerEndGame();
        }
    }

    private boolean backtrack(final int x, final int y) {
    	if (winner.getWinner() != null) return false;

        for (RatDirection ratDirection : randomizeMoves()) {
        	if (winner.getWinner() != null) return false;
        	
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
                stepUpRat(stepX, stepY, ratDirection);
                render();

                if (backtrack(stepX, stepY)) return true;
                
                if (winner.getWinner() != null) return false;
                stepBackRat(x, y);
                render();
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
        	
        	objectsCopy = new ArrayList<>(block.getObjects());
        }
        
        for (GameObject object : objectsCopy) {
        	if (object instanceof Path) {
        		if (((Path) object).getRat() == this) return true;
        	}
        }
        return false;
    }

    private void triggerEndGame() {
        synchronized (winner) {
        	winner.setWinner(this);
        }
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

    private void freeze() {
        if (waitTime <= 0) return;

        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        	Thread.currentThread().interrupt();
        }
    }

    private void stepUpRat(final int x, final int y, RatDirection ratDirection) {
        int stepX = x - ratDirection.x();
        int stepY = y - ratDirection.y();

        Path path = new Path(this);
        history.push(new History(x, y, path));

        maze.remove(stepX, stepY, this);
        maze.add(stepX, stepY, path);
        
        if (maze.getBlock(x, y) == null) {
            maze.setBlock(x, y, new Floor());
        }

        maze.add(x, y, this);
    }

    private void stepBackRat(final int oldX, final int oldY) {
        History back = history.pop();

        maze.remove(back.x(), back.y(), this);
        maze.remove(oldX, oldY, back.path());
        maze.add(oldX, oldY, this);
    }

    private void render() {
        try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace(); //handle it appropriately
        } catch (BrokenBarrierException e) {
            throw new RuntimeException(e);
        }

//        synchronized(maze.getAllBlocks()) {
//    		ui.clear();
//            ui.draw();
//    	}

        freeze();
    }

    public int getStepsTaken() {
        return this.history.size();
    }

}

