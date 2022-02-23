import processing.core.PApplet;

import java.util.EnumSet;

public class Terrain {
    final static int SAND_COLOR = color(190, 170, 105);
    final static int BEDROCK_COLOR = color(40, 40, 40);
    final static int GRASS_COLOR = color(35, 105, 25);
    final static int EXCAVATED_COLOR = color(35, 40, 50);
    final static int DIRT_COLOR = color(85, 45, 5);
    final static int AIR_COLOR = color(100, 100, 240);

    final static EnumSet<CellState> collisions = EnumSet.of(
            CellState.GRASS,
            CellState.DIRT,
            CellState.SAND,
            CellState.BEDROCK
    );

    final static EnumSet<CellState> destructible = EnumSet.of(
            CellState.DIRT,
            CellState.GRASS,
            CellState.SAND
    );

    final static EnumSet<CellState> backgroundMaterials = EnumSet.of(
            CellState.AIR,
            CellState.EXCAVATED
    );

    public static void classic(int height, int width, boolean[][] initialTerrain, CellState[][] terrain, Game game) {
        int bedrockThickness = 10;

        for (int j = 0; j < width; j++) {
            for (int i = 0; i < height - bedrockThickness; i++) {
                terrain[i][j] = CellState.AIR;
            }
            for (int i = height - bedrockThickness; i < height; i++) {
                terrain[i][j] = CellState.BEDROCK;
            }
        }

        float[] heightValues = new float[width];
        for (int i = 0; i < width; i++) {
            heightValues[i] = game.noise(i / game.resolution) * height * game.verticalStretch;
        }

        float offsetFactor = (height * .75f) - PApplet.max(heightValues);
        for (int i = 0; i < width; i++) {
            heightValues[i] += offsetFactor;
        }

        for (int i = 0; i < width; i++) {
            for (int j = height - bedrockThickness; j >= heightValues[i] - 4;j--) {
                if (game.noise(i / 40f, j / 30f) > .6) {
                    terrain[j][i] = CellState.SAND;
                } else {
                    terrain[j][i] = CellState.DIRT;
                }
                if (game.noise(i / 50f + 10000f, j / 50f + 10000f) > .7) {
                    terrain[j][i] = CellState.EXCAVATED;
                }
                initialTerrain[j][i] = true;
            }
            for (int j = (int) (heightValues[i] - 4); j < heightValues[i]; j++) {
                terrain[j][i] = CellState.GRASS;
            }
        }
    }

    public static void floatingIslands(int height, int width, boolean[][] initialTerrain, CellState[][] terrain, Game game) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (game.noise(i / 60f, j / 60f) < .4) {
                    terrain[i][j] = CellState.DIRT;
                    initialTerrain[i][j] = true;
                } else {
                    terrain[i][j] = CellState.AIR;
                }
            }
        }
    }

    private static int color(int r, int g, int b) {
        return -16777216 | r << 16 | g << 8 | b;
    }
}
