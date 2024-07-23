package Tetris.Tetris.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Singleton class representing the state of the Tetris game.
 */
public class GameState {
    private static GameState instance;
    public Cell[][] stage;
    private int currentXPosition;
    private ConcurrentHashMap<String, String[][]> playerTetrominos;
    private ConcurrentHashMap<String, String> playerColors;
    private List<String> availableColors;
    private Random random;

    /**
     * Private constructor to initialize the game state.
     */
    private GameState() {
        this.stage = createInitialStage();
        this.playerTetrominos = new ConcurrentHashMap<>();
        this.playerColors = new ConcurrentHashMap<>();
        this.random = new Random();
        this.availableColors = createColorList();
    }

    /**
     * Returns the single instance of the GameState.
     *
     * @return the single instance of the GameState
     */
    public static synchronized GameState getInstance() {
        if (instance == null) {
            instance = new GameState();
        }
        return instance;
    }
    
    /**
     * Creates a list of available colors.
     *
     * @return a list of available colors
     */
    private List<String> createColorList() {
        List<String> colors = new ArrayList<>();
        colors.add("255, 0, 0");    // Red
        colors.add("0, 255, 0");    // Green
        colors.add("0, 0, 255");    // Blue
        colors.add("255, 255, 0");  // Yellow
        colors.add("0, 255, 255");  // Cyan
        colors.add("255, 0, 255");  // Magenta
        colors.add("192, 192, 192");// Light Gray
        colors.add("128, 0, 128");  // Purple
        colors.add("255, 165, 0");  // Orange
        colors.add("0, 128, 128");  // Teal
        return colors;
    }

    /**
     * Creates the initial stage of the game.
     *
     * @return the initial stage of the game
     */
    private Cell[][] createInitialStage() {
        Cell[][] stage = new Cell[36][24];
        for (int i = 0; i < 36; i++) {
            for (int j = 0; j < 24; j++) {
                stage[i][j] = new Cell("0", "clear", "0, 0, 0"); 
            }
        }
        return stage;
    }

    /**
     * Updates the game state from the client data.
     *
     * @param data the data from the client
     */
    public void updateFromClient(Map<String, Object> data) {
        String sessionId = (String) data.get("sessionId");
        String[][] tetromino = getPlayerTetromino(sessionId);
        String color = playerColors.get(sessionId);

        int posX = getIntFromData(data, "posX");
        int posY = getIntFromData(data, "posY");

        clearTetrominoPosition(sessionId);

        for (int y = 0; y < tetromino.length; y++) {
            for (int x = 0; x < tetromino[y].length; x++) {
                if (!tetromino[y][x].equals("0")) {
                    stage[posY + y][posX + x] = new Cell(tetromino[y][x], "clear", color);
                }
            }
        }
    }

    /**
     * Updates the game state for a collided tetromino from the client data.
     *
     * @param data the data from the client
     */
    public void updateFromClientCollided(Map<String, Object> data) {
        String sessionId = (String) data.get("sessionId");
        String[][] tetromino = getPlayerTetromino(sessionId);
        String color = playerColors.get(sessionId);

        int posX = getIntFromData(data, "posX");
        int posY = getIntFromData(data, "posY");

        for (int y = 0; y < tetromino.length; y++) {
            for (int x = 0; x < tetromino[y].length; x++) {
                if (!tetromino[y][x].equals("0")) {
                    stage[posY + y][posX + x] = new Cell(tetromino[y][x], "merged", color);
                }
            }
        }
    }


    /**
     * Clears the current position of a player's tetromino.
     *
     * @param sessionId the session ID of the player
     */
    public void clearTetrominoPosition(String sessionId) {
        String color = getPlayerColor(sessionId);
        for (int y = 0; y < stage.length; y++) {
            for (int x = 0; x < stage[y].length; x++) {
                if (stage[y][x].getColor().equals(color) && stage[y][x].getStatus().equals("clear")) {
                    stage[y][x] = new Cell("0", "clear", "0, 0, 0");
                }
            }
        }
    }

    /**
     * Persists the position of a player's tetromino on the stage.
     *
     * @param sessionId the session ID of the player
     * @param posX the x-coordinate of the tetromino
     * @param posY the y-coordinate of the tetromino
     * @param tetromino the tetromino shape
     * @param color the color of the tetromino
     */
    public void persistTetrominoPosition(String sessionId, int posX, int posY, String[][] tetromino, String color) {
        for (int y = 0; y < tetromino.length; y++) {
            for (int x = 0; x < tetromino[y].length; x++) {
                if (!tetromino[y][x].equals("0")) {
                    int boardX = posX + x;
                    int boardY = posY + y;
    
                    if (boardX >= 0 && boardX < stage[0].length && boardY >= 0 && boardY < stage.length) {
                        stage[boardY][boardX] = new Cell(tetromino[y][x], "clear", color);
                    } else {
                        System.out.println("Attempted to access out of bounds position: (" + boardX + ", " + boardY + ")");
                    }
                }
            }
        }
    }
    
    /**
     * Retrieves an integer value from the client data.
     *
     * @param data the client data
     * @param key the key to retrieve the value for
     * @return the integer value
     */
    public int getIntFromData(Map<String, Object> data, String key) {
        if (data.containsKey(key)) {
            if (data.get(key) instanceof Integer) {
                return (Integer) data.get(key);
            } else if (data.get(key) instanceof Double) {
                return ((Double) data.get(key)).intValue();
            }
        }
        return 0; 
    }

    /**
     * Resets the game state to the initial state.
     */
    public void reset() {
        for (int y = 0; y <36; y++) {
            for (int x = 0; x < 24; x++) {
                stage[y][x] = new Cell("0", "clear", "0, 0, 0");
            }
        }
        playerTetrominos.clear(); 
        playerColors.clear(); 
        availableColors = createColorList(); 
    }

    /**
     * Rotates a player's tetromino.
     *
     * @param data the client data
     */
    public void rotateTetromino(Map<String, Object> data) {
        String sessionId = (String) data.get("sessionId");
        String[][] tetromino = getPlayerTetromino(sessionId);
        String color = playerColors.get(sessionId);
        tetromino = rotateMatrix(tetromino);
        int posX = getIntFromData(data, "posX");
        int posY = getIntFromData(data, "posY");
        clearTetrominoPosition(sessionId);
        for (int y = 0; y < tetromino.length; y++) {
            for (int x = 0; x < tetromino[y].length; x++) {
                if (!tetromino[y][x].equals("0")) {
                    stage[posY + y][posX + x] = new Cell(tetromino[y][x], "clear", color);
                }
            }
        }
        addPlayerTetromino(sessionId, tetromino);
    }
    
    /**
     * Rotates a matrix by 90 degrees.
     *
     * @param matrix the matrix to rotate
     * @return the rotated matrix
     */
    private String[][] rotateMatrix(String[][] matrix) {
        int n = matrix.length;
        String[][] rotatedMatrix = new String[n][n];
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                rotatedMatrix[x][n - y - 1] = matrix[y][x];
            }
        }
        return rotatedMatrix;
    }

    /**
     * Removes completed lines from the game stage.
     *
     * @param linesCleared the number of lines cleared
     */
    public void removeLines(int linesCleared) {
        for (int y = 0; y < stage.length; y++) {
            boolean lineComplete = true;
            for (int x = 0; x < stage[y].length; x++) {
                if (stage[y][x].getValue().equals("0")) {
                    lineComplete = false;
                    break;
                }
            }
            if (lineComplete) {
                for (int row = y; row > 0; row--) {
                    stage[row] = stage[row - 1];
                }
                stage[0] = new Cell[stage[0].length];
                for (int x = 0; x < stage[0].length; x++) {
                    stage[0][x] = new Cell("0", "clear", "0, 0, 0");
                }
            }
        }
    }

    /**
     * Retrieves the next x position for placing a tetromino.
     *
     * @return the next x position
     */
    public int getNextXPosition() {
        int position = currentXPosition;
        currentXPosition = (currentXPosition + 3) % 24;
        return position;
    }

    /**
     * Retrieves a player's tetromino.
     *
     * @param sessionId the session ID of the player
     * @return the player's tetromino
     */    
    public String[][] getPlayerTetromino(String sessionId) {
        return playerTetrominos.get(sessionId);
    }

    /**
     * Retrieves a player's color.
     *
     * @param sessionId the session ID of the player
     * @return the player's color
     */
    public String getPlayerColor(String sessionId) {
        return playerColors.get(sessionId);
    }

    /**
     * Adds a tetromino for a player.
     *
     * @param sessionId the session ID of the player
     * @param tetromino the tetromino to add
     */
    public void addPlayerTetromino(String sessionId, String[][] tetromino) {
        playerTetrominos.put(sessionId, tetromino);
    }

    /**
     * Adds a color for a player.
     *
     * @param sessionId the session ID of the player
     * @param color the color to add
     */
    public void addPlayerColor(String sessionId, String color) {
        playerColors.put(sessionId, color);
    }


    /**
     * Retrieves a random color from the list of available colors.
     *
     * @return a random color
     */
    public String getRandomColor() {
        if (availableColors.isEmpty()) {
            throw new IllegalStateException("No more colors available");
        }
        int index = random.nextInt(availableColors.size());
        return availableColors.remove(index);
    }

    /**
     * Sets the game stage.
     *
     * @param stage the game stage to set
     */
    public void setStage(Cell[][] stage) {
        this.stage = stage;
    }
}
