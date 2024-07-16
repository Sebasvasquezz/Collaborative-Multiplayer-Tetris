package Tetris.Tetris.model;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class GameState {
    public String[][] gameBoard;
    public List<String> playerIds;
    public Map<String, String[][]> playerTetrominos; // Almacena los tetrominos de cada jugador
    private int currentXPosition;

    public GameState() {
        // Inicializa el tablero de juego a 50x60
        this.gameBoard = new String[50][60];
        for (int y = 0; y < 50; y++) {
            for (int x = 0; x < 60; x++) {
                gameBoard[y][x] = "0";
            }
        }
        this.playerIds = new ArrayList<>();
        this.playerTetrominos = new HashMap<>();
    }

    public void updateFromClient(Map<String, Object> data) {
        // Actualiza la posición del tetromino en el tablero de juego
        String[] rows = ((String) data.get("tetromino")).split(";");
        int posX = ((Double) data.get("posX")).intValue();
        int posY = ((Double) data.get("posY")).intValue();
        String playerId = (String) data.get("playerId");

        // Agrega el ID del jugador a la lista si no está presente
        if (!playerIds.contains(playerId)) {
            playerIds.add(playerId);
        }

        // Actualiza el tablero de juego
        for (int y = 0; y < rows.length; y++) {
            String[] cols = rows[y].split(",");
            for (int x = 0; x < cols.length; x++) {
                if (!cols[x].equals("0")) {
                    gameBoard[posY + y][posX + x] = cols[x];
                }
            }
        }
    }

    // Método para resetear el estado del juego
    public void reset() {
        for (int y = 0; y < 50; y++) {
            for (int x = 0; x < 60; x++) {
                gameBoard[y][x] = "0";
            }
        }
        playerIds.clear();
        playerTetrominos.clear();
    }

    // Método para asignar un tetromino a un jugador
    public void assignTetromino(String playerId, String[][] tetromino) {
        playerTetrominos.put(playerId, tetromino);
    }
    
    public int getNextXPosition() {
        int position = currentXPosition;
        currentXPosition = (currentXPosition + 4) % 60;
        return position;
    }
}
