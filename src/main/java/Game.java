import g4p_controls.*;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.*;

public class Game extends PApplet {
    boolean spacePressed, upPressed, downPressed, rightPressed, leftPressed, wPressed, sPressed, aPressed, dPressed;

    CellState[][] terrain;
    boolean[][] initialTerrain;

    float resolution = 300, verticalStretch = 1f;

    List<Entity> entities;

    GCustomSlider power, angle;

    Player player;
    int playerSpeed = 3;

    boolean rainingSand = false;

    public void settings() {
        fullScreen();
    }

    public void setup() {
        initialTerrain = new boolean[height][width];
        terrain = new CellState[height][width];

        Terrain.classic(height, width, initialTerrain, terrain, this);

        entities = new ArrayList<>();

        power = new GCustomSlider(this, 10, height - 40, 200, 20);
        angle = new GCustomSlider(this, 10, height - 25, 200, 20);

        power.setNbrTicks(10);
        angle.setNbrTicks(10);

        power.setLimits(10, 100);
        angle.setLimits(200, -20);

        power.setShowDecor(false, true, true, true);
        angle.setShowDecor(false, true, true, true);
        background(100, 100, 240);

        player = new Player(100, 100, this);

        entities.add(player);
    }

    public void draw() {
        keyEvents();

        for (int i = 0; i < 2; i++) {
            fallSand();
        }

        background(100, 100, 240);
        stroke(85, 45, 5);
        strokeWeight(2);
        loadPixels();
        for (int i = 0; i < width * height; i++) {
            CellState state = terrain[i / width][i - (i / width) * width];
            switch (state) {
                case SAND -> pixels[i] = Terrain.SAND_COLOR;
                case BEDROCK -> pixels[i] = Terrain.BEDROCK_COLOR;
                case GRASS -> pixels[i] = Terrain.GRASS_COLOR;
                case EXCAVATED -> pixels[i] = Terrain.EXCAVATED_COLOR;
                case DIRT -> pixels[i] = Terrain.DIRT_COLOR;
                case AIR -> pixels[i] = Terrain.AIR_COLOR;
            }
        }
        updatePixels();

        for (Entity entity : entities) {
            entity.update();
            entity.show();
        }

        List<Entity> complete = new ArrayList<>();
        List<Explosion> newExplosions = new ArrayList<>();

        for (Entity entity : entities) {
            if (entity.completed()) {
                complete.add(entity);
                if (entity instanceof Explosion) {
                    destroyTerrain((Explosion) entity);
                }
            } else if (entity instanceof Projectile && ((Projectile) entity).checkImpact(terrain, Terrain.collisions)) {
                newExplosions.add(new Explosion(((Projectile) entity).position, ((Projectile) entity).range, this));
                complete.add(entity);
            }
        }

        entities.addAll(newExplosions);
        entities.removeAll(complete);

        drawArc();
    }

    private void fallSand() {
        for (int i = 0; i < width; i++) {
            if (rainingSand && terrain[0][i] == CellState.AIR && new Random().nextFloat() > .7f) {
                terrain[0][i] = CellState.SAND;
            }
            if (terrain[height - 1][i] == CellState.SAND) {
                terrain[height - 1][i] = CellState.AIR;
            }
        }

        for (int i = height - 1; i > -1; i--) {
            for (int j = 0; j < width; j++) {
                if (terrain[i][j] == CellState.SAND) {
                    if (Terrain.backgroundMaterials.contains(terrain[i + 1][j])) {
                        clearCell(j, i);
                        terrain[i + 1][j] = CellState.SAND;
                    }
                }
            }
        }

        for (int i = height - 2; i > -1; i--) {
            for (int j = 0; j < width; j++) {
                if (terrain[i][j] == CellState.SAND) {
                    boolean leftAir = false, rightAir = false;
                    boolean leftWall = j == 0;
                    boolean rightWall = j == width - 1;
                    if (j > 0) {
                        leftAir = Terrain.backgroundMaterials.contains(terrain[i + 1][j - 1]);
                    }
                    if (j < width - 1) {
                        rightAir = Terrain.backgroundMaterials.contains(terrain[i + 1][j + 1]);
                    }
                    if ((rightAir || rightWall) && (leftAir || leftWall)) {
                        clearCell(j, i);
                        if (new Random().nextBoolean()) {
                            if (rightAir) {
                                terrain[i + 1][j + 1] = CellState.SAND;
                            }
                        } else if (leftAir) {
                            terrain[i + 1][j - 1] = CellState.SAND;
                        }
                    } else if (rightAir) {
                        clearCell(j, i);
                        terrain[i + 1][j + 1] = CellState.SAND;
                    } else if (leftAir) {
                        clearCell(j, i);
                        terrain[i + 1][j - 1] = CellState.SAND;
                    } else if (rightWall) {
                        clearCell(j, i);
                    } else if (leftWall) {
                        clearCell(j, i);
                    }
                }
            }
        }
    }

    private void clearCell(int x, int y) {
        terrain[y][x] = initialTerrain[y][x] ? CellState.EXCAVATED : CellState.AIR;
    }

    private void drawArc() {
        noFill();
        stroke(100, 20, 20, 100);
        PVector position = player.position.copy();
        PVector velocity = new PVector(power.getValueF() / 10, 0).rotate(radians(-angle.getValueF()));

        beginShape();
        curveVertex(position.x, position.y);
        for (int i = 0; i < 100; i++) {
            position.add(velocity);
            velocity.sub(0, -.05f);
            curveVertex(position.x, position.y);
            if (position.x < width && position.x > -1 && position.y < height && position.y > -1) {
                if (Terrain.collisions.contains(terrain[(int) position.y][(int) position.x])) {
                    break;
                }
            }
        }
        endShape();
    }

    private void destroyTerrain(Explosion explosion) {
        for (float i = explosion.position.y - explosion.range; i < explosion.position.y + explosion.range; i++) {
            if (i >= height || i < 0) {
                continue;
            }
            for (float j = explosion.position.x - explosion.range; j < explosion.position.x + explosion.range; j++) {
                if (j >= width || j < 0) {
                    continue;
                }
                if (Terrain.destructible.contains(terrain[(int) i][(int) j]) && PVector.dist(explosion.position, new PVector(j, i)) <= explosion.range) {
                    clearCell((int) j, (int) i);
                }
            }
        }
    }

    private void keyEvents() {
        if (downPressed) {
            power.setValue(power.getValueF() - 1);
        }
        if (upPressed) {
            power.setValue(power.getValueF() + 1);
        }
        if (leftPressed) {
            angle.setValue(angle.getValueF() + 1);
        }
        if (rightPressed) {
            angle.setValue(angle.getValueF() - 1);
        }
        if (spacePressed) {
            entities.add(new Projectile(player.position.copy(), new PVector(power.getValueF() / 10, 0).rotate(radians(-angle.getValueF())), this));
        }
        if (wPressed) {
            player.position.add(0, -playerSpeed);
        }
        if (sPressed) {
            player.position.add(0, playerSpeed);
        }
        if (aPressed) {
            player.position.add(-playerSpeed, 0);
        }
        if (dPressed) {
            player.position.add(playerSpeed, 0);
        }
        if (mousePressed) {
            try {
                if (Terrain.backgroundMaterials.contains(terrain[mouseY][mouseX])) {
                    for (int i = 0; i < 40; i++) {
                        for (int j = -20; j < 20; j++) {
                            terrain[mouseY + i][mouseX + j] = CellState.SAND;
                        }
                    }
                }
            } catch (ArrayIndexOutOfBoundsException ignored) {

            }
        }
    }

    public void keyPressed() {
        if (key == CODED) {
            switch (keyCode) {
                case DOWN -> downPressed = true;
                case UP -> upPressed = true;
                case LEFT -> leftPressed = true;
                case RIGHT -> rightPressed = true;
            }
        } else {
            switch (key) {
                case ' ' -> spacePressed = true;
                case 'w' -> wPressed = true;
                case 'a' -> aPressed = true;
                case 's' -> sPressed = true;
                case 'd' -> dPressed = true;
            }
        }
    }

    public void keyReleased() {
        if (key == CODED) {
            switch (keyCode) {
                case DOWN -> downPressed = false;
                case UP -> upPressed = false;
                case LEFT -> leftPressed = false;
                case RIGHT -> rightPressed = false;
            }
        }  else {
            switch (key) {
                case ' ' -> spacePressed = false;
                case 'w' -> wPressed = false;
                case 'a' -> aPressed = false;
                case 's' -> sPressed = false;
                case 'd' -> dPressed = false;
            }
        }
    }

    public static void main(String[] args) {
        String[] pArgs = new String[]{"Game"};

        PApplet.runSketch(pArgs, new Game());
    }
}