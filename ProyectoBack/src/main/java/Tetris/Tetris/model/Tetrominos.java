package Tetris.Tetris.model;

import java.util.Map;
import java.util.HashMap;

/**
 * Represents a collection of Tetromino shapes used in the Tetris game.
 * Each Tetromino shape is defined by a 2D array of strings.
 */
public class Tetrominos {
    
    /**
     * A static map that holds the Tetromino shapes.
     * The key is the name of the Tetromino shape, and the value is a 2D array representing the shape.
     */
    public static final Map<String, String[][]> TETROMINOS = new HashMap<>();

    static {
        TETROMINOS.put("I", new String[][]{
            {"0", "I", "0", "0"},
            {"0", "I", "0", "0"},
            {"0", "I", "0", "0"},
            {"0", "I", "0", "0"}
        });
        TETROMINOS.put("J", new String[][]{
            {"0", "J", "0"},
            {"0", "J", "0"},
            {"J", "J", "0"}
        });
        TETROMINOS.put("L", new String[][]{
            {"0", "L", "0"},
            {"0", "L", "0"},
            {"0", "L", "L"}
        });
        TETROMINOS.put("O", new String[][]{
            {"O", "O"},
            {"O", "O"}
        });
        TETROMINOS.put("S", new String[][]{
            {"0", "S", "S"},
            {"S", "S", "0"},
            {"0", "0", "0"}
        });
        TETROMINOS.put("T", new String[][]{
            {"0", "0", "0"},
            {"T", "T", "T"},
            {"0", "T", "0"}
        });
        TETROMINOS.put("Z", new String[][]{
            {"Z", "Z", "0"},
            {"0", "Z", "Z"},
            {"0", "0", "0"}
        });
    }
}
