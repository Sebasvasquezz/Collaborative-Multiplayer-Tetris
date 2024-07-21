package Tetris.Tetris.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameState {
    public Cell[][] stage;
    private int currentXPosition;
    private int dropTime;
    private int score;
    private int level;
    private int rows;
    private boolean gameOver;

    // Mapa concurrente para manejar los tetrominos actuales de los jugadores
    private ConcurrentHashMap<String, String[][]> playerTetrominos;
    private ConcurrentHashMap<String, String> playerColors;

    // Lista de colores disponibles
    private List<String> availableColors;
    private Random random;

    public GameState() {
        this.stage = createInitialStage();
        this.dropTime = 1000;
        this.score = 0;
        this.level = 0;
        this.rows = 0;
        this.gameOver = false;
        this.playerTetrominos = new ConcurrentHashMap<>();
        this.playerColors = new ConcurrentHashMap<>();
        this.random = new Random();
        this.availableColors = createColorList();
    }

    private List<String> createColorList() {
        List<String> colors = new ArrayList<>();
        colors.add("255, 0, 0");    // Rojo
        colors.add("0, 255, 0");    // Verde
        colors.add("0, 0, 255");    // Azul
        colors.add("255, 255, 0");  // Amarillo
        colors.add("0, 255, 255");  // Cian
        colors.add("255, 0, 255");  // Magenta
        colors.add("192, 192, 192");// Gris claro
        colors.add("128, 0, 128");  // Púrpura
        colors.add("255, 165, 0");  // Naranja
        colors.add("0, 128, 128");  // Verde azulado
        return colors;
    }

    private Cell[][] createInitialStage() {
        Cell[][] stage = new Cell[36][24];
        for (int i = 0; i < 36; i++) {
            for (int j = 0; j < 24; j++) {
                stage[i][j] = new Cell("0", "clear", "0, 0, 0"); // Inicializa todas las celdas a (0, 'clear', '0, 0, 0')
            }
        }
        return stage;
    }

    public void updateFromClient(Map<String, Object> data) {
        String sessionId = (String) data.get("sessionId");
        String[][] tetromino = getPlayerTetromino(sessionId);
        String color = playerColors.get(sessionId);

        int posX = getIntFromData(data, "posX");
        int posY = getIntFromData(data, "posY");

        // Limpia la posición anterior del tetromino
        clearTetrominoPosition(sessionId);

        // Actualiza el tablero de juego con la nueva posición del tetromino
        for (int y = 0; y < tetromino.length; y++) {
            for (int x = 0; x < tetromino[y].length; x++) {
                if (!tetromino[y][x].equals("0")) {
                    stage[posY + y][posX + x] = new Cell(tetromino[y][x], "clear", color);
                }
            }
        }
    }

    public void updateFromClientCollided(Map<String, Object> data) {
        String sessionId = (String) data.get("sessionId");
        String[][] tetromino = getPlayerTetromino(sessionId);
        String color = playerColors.get(sessionId);

        int posX = getIntFromData(data, "posX");
        int posY = getIntFromData(data, "posY");

        // Actualiza el tablero de juego con la nueva posición del tetromino
        for (int y = 0; y < tetromino.length; y++) {
            for (int x = 0; x < tetromino[y].length; x++) {
                if (!tetromino[y][x].equals("0")) {
                    stage[posY + y][posX + x] = new Cell(tetromino[y][x], "merged", color);
                }
            }
        }
    }

    private void clearTetrominoPosition(String sessionId) {
        String color = getPlayerColor(sessionId);
        for (int y = 0; y < stage.length; y++) {
            for (int x = 0; x < stage[y].length; x++) {
                if (stage[y][x].getColor().equals(color) && stage[y][x].getStatus().equals("clear")) {
                    stage[y][x] = new Cell("0", "clear", "0, 0, 0");
                }
            }
        }
    }

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
    
    
    public int getIntFromData(Map<String, Object> data, String key) {
        if (data.containsKey(key)) {
            if (data.get(key) instanceof Integer) {
                return (Integer) data.get(key);
            } else if (data.get(key) instanceof Double) {
                return ((Double) data.get(key)).intValue();
            }
        }
        return 0; // Valor predeterminado si no está presente
    }

    // Método para resetear el estado del juego
    public void reset() {
        for (int y = 0; y <36; y++) {
            for (int x = 0; x < 24; x++) {
                stage[y][x] = new Cell("0", "clear", "0, 0, 0");
            }
        }
        playerTetrominos.clear(); // Limpia el mapa de tetrominos de los jugadores
        playerColors.clear(); // Limpia el mapa de colores de los jugadores
        availableColors = createColorList(); // Restablece la lista de colores
    }

    public void rotateTetromino(Map<String, Object> data) {
        String sessionId = (String) data.get("sessionId");
        String[][] tetromino = getPlayerTetromino(sessionId);
        String color = playerColors.get(sessionId);
    
        // Rotar el tetromino
        tetromino = rotateMatrix(tetromino);
    
        int posX = getIntFromData(data, "posX");
        int posY = getIntFromData(data, "posY");
    
        // Limpia la posición anterior del tetromino
        clearTetrominoPosition(sessionId);
    
        // Actualiza el tablero de juego con la nueva posición del tetromino
        for (int y = 0; y < tetromino.length; y++) {
            for (int x = 0; x < tetromino[y].length; x++) {
                if (!tetromino[y][x].equals("0")) {
                    stage[posY + y][posX + x] = new Cell(tetromino[y][x], "clear", color);
                }
            }
        }
    
        // Actualizar el tetromino del jugador en el estado del juego
        addPlayerTetromino(sessionId, tetromino);
    }
    
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

    public void removeLines(int linesCleared) {
        // Encuentra las líneas completas y elimínalas
        for (int y = 0; y < stage.length; y++) {
            boolean lineComplete = true;
            for (int x = 0; x < stage[y].length; x++) {
                if (stage[y][x].getValue().equals("0")) {
                    lineComplete = false;
                    break;
                }
            }
            if (lineComplete) {
                // Mueve todas las líneas por encima hacia abajo
                for (int row = y; row > 0; row--) {
                    stage[row] = stage[row - 1];
                }
                // Crea una nueva línea vacía en la parte superior
                stage[0] = new Cell[stage[0].length];
                for (int x = 0; x < stage[0].length; x++) {
                    stage[0][x] = new Cell("0", "clear", "0, 0, 0");
                }
            }
        }
    }

    public int getNextXPosition() {
        int position = currentXPosition;
        currentXPosition = (currentXPosition + 3) % 24;
        return position;
    }

    public String[][] getPlayerTetromino(String sessionId) {
        return playerTetrominos.get(sessionId);
    }

    public String getPlayerColor(String sessionId) {
        return playerColors.get(sessionId);
    }

    public void addPlayerTetromino(String sessionId, String[][] tetromino) {
        playerTetrominos.put(sessionId, tetromino);
    }
    public void addPlayerColor(String sessionId, String color) {
        playerColors.put(sessionId, color);
    }

    public String getRandomColor() {
        if (availableColors.isEmpty()) {
            throw new IllegalStateException("No more colors available");
        }
        int index = random.nextInt(availableColors.size());
        return availableColors.remove(index);
    }

    public void setStage(Cell[][] stage) {
        this.stage = stage;
    }
}
