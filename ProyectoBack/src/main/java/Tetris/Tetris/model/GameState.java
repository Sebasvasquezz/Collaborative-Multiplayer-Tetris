package Tetris.Tetris.model;

import java.util.Map;

public class GameState {
    public Cell[][] stage;
    private int dropTime;
    private int score;

    public void setStage(Cell[][] stage) {
        this.stage = stage;
    }

    private int level;
    private int rows;
    private boolean gameOver;
    private int currentXPosition;

    public GameState() {
        this.stage = createInitialStage();
        this.dropTime = 1000;
        this.score = 0;
        this.level = 0;
        this.rows = 0;
        this.gameOver = false;
    }

    private Cell[][] createInitialStage() {
        Cell[][] stage = new Cell[50][60];
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 60; j++) {
                stage[i][j] = new Cell("0", "clear"); // Inicializa todas las celdas a (0, 'clear')
            }
        }
        return stage;
    }

    public void updateFromClient(Map<String, Object> data) {
        // Obtiene el tetromino como un array bidimensional de String
        String[][] tetromino = (String[][]) data.get("tetromino");
    
        // Verifica y convierte posX a Double si es Integer
        int posX;
        if (data.get("posX") instanceof Integer) {
            posX = ((Integer) data.get("posX")).intValue();
        } else {
            posX = ((Double) data.get("posX")).intValue();
        }
    
        // Verifica y convierte posY a Double si es Integer, si no está presente lo asigna a 0
        int posY;
        if (data.containsKey("posY")) {
            if (data.get("posY") instanceof Integer) {
                posY = ((Integer) data.get("posY")).intValue();
            } else {
                posY = ((Double) data.get("posY")).intValue();
            }
        } else {
            posY = 0;
        }
    
        // Actualiza el tablero de juego
        for (int y = 0; y < tetromino.length; y++) {
            for (int x = 0; x < tetromino[y].length; x++) {
                if (!tetromino[y][x].equals("0")) {
                    stage[posY + y][posX + x] = new Cell(tetromino[y][x], "clear");
                }
            }
        }
    }
    
    
    

    // Método para resetear el estado del juego
    public void reset() {
        for (int y = 0; y < 50; y++) {
            for (int x = 0; x < 60; x++) {
                stage[y][x] = new Cell("0", "clear");
            }
        }
    }
    
    public int getNextXPosition() {
        int position = currentXPosition;
        currentXPosition = (currentXPosition + 4) % 60;
        return position;
    }
}
