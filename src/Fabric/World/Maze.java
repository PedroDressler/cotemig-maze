package Fabric.World;

import Fabric.Objects.Floor;
import Fabric.Objects.GameObject;
import Fabric.Objects.Target;
import Fabric.Objects.Wall;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import Fabric.Types.MazeDirection;

public class Maze {

    private Block[][] blocks;

    public Maze(int heigth, int width) {
        if (heigth < 3 || width < 3) {
            throw new IllegalArgumentException("Heigth or Width less than 3");
        }

        heigth = heigth % 2 == 0 ? heigth + 1 : heigth;
        width = width % 2 == 0 ? width + 1 : width;

        blocks = new Block[heigth][width];
    }

    public void buildTestMaze() {
        System.out.println("Building test maze...");
        System.out.println("Maze size: " +  heigth() + "x" + width());
        for (int i = 0; i < heigth(); i++) {
            for (int j = 0; j < width(); j++) {
                if (i == 0 || j == 0 || i == heigth() - 1 || j == width() - 1) {
                    blocks[i][j] = new Block(new Wall());
                } else {
                    blocks[i][j] = new Block(new Floor());
                }
            }
        }

        // Depois que terminar a geração do labirinto


        // Definição da posição do alvo
        blocks[blocks.length - 2][blocks[0].length - 2] = new Block(new Target());
    }

    public void buildMaze(List<int[]> availableFloorTiles) {
        /* TODO: implementar algoritmo de geração do labirinto
         * Precisa spawnar o caçador no canto esquerdo superior do labirinto
         * Precisa spawnar o alvo no canto direito inferior do labirinto
         */
        for (int i = 0; i < blocks.length; i++) {
            for (int j = 0; j < blocks[0].length; j++) {
                blocks[i][j] = new Block(new Wall());
            }
        }

        carvePassages(1, 1, new Random(), availableFloorTiles);

        int targetX = heigth() - 2;
        int targetY = width() - 2;

        if (targetX % 2 == 0) targetX--;
        if (targetY % 2 == 0) targetY--;

        blocks[targetX][targetY] = new Block(new Target());

        availableFloorTiles.remove(new int[]{targetX, targetY});
    }

    private void carvePassages(int x, int y, Random rand, List<int[]> availableFloorTiles) {
        blocks[x][y] = new Block(new Floor());

        MazeDirection[] directions = MazeDirection.values();
        Collections.shuffle(Arrays.asList(directions), rand);

        for (MazeDirection dir : directions) {
            int newX = x + dir.x();
            int newY = y + dir.y();

            if (newX > 0 && newX < blocks.length - 1 && newY > 0 && newY < blocks[0].length - 1) {

                if (blocks[newX][newY].isWall()) {

                    blocks[x + dir.pathX()][y + dir.pathY()] = new Block(new Floor());

                    availableFloorTiles.add(new int[] {x + dir.pathX(), y + dir.pathY()});

                    carvePassages(newX, newY, rand, availableFloorTiles);
                }
            }
        }
    }

    public int heigth() {
        return blocks.length;
    }

    public int width() {
        return blocks[0].length;
    }

    public Block getBlock(int x, int y) {
        return blocks[x][y];
    }

    public Block[][] getAllBlocks() {
        return blocks;
    }

    public List<GameObject> getObjects(int x, int y) {
        return blocks[x][y].getObjects();
    }

    public GameObject getTopObject(int x, int y) {
        if (blocks[x][y] == null || blocks[x][y].getObjects().isEmpty()) {
            return null;
        }

        return blocks[x][y].getObjects().getFirst();
    }

    public GameObject getBottomObject(int x, int y) {
        if (blocks[x][y] == null || blocks[x][y].getObjects().isEmpty()) {
            return null;
        }

        return blocks[x][y].getObjects().getLast();
    }

    public void setBlock(int x, int y, GameObject object) {
        blocks[x][y] = new Block(object);
    }

    public void add(int x, int y, GameObject object) {
        blocks[x][y].add(object);
    }

    public void remove(int x, int y, GameObject object) {
        blocks[x][y].remove(object);
    }

}
