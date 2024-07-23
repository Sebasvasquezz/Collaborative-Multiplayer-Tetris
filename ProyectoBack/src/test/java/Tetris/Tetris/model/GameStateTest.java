package Tetris.Tetris.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GameStateTest {

    private GameState gameState;

    @BeforeEach
    public void setUp() {
        gameState = GameState.getInstance();
        gameState.reset();
    }

    @Test
    public void testSingletonInstance() {
        GameState anotherInstance = GameState.getInstance();
        assertSame(gameState, anotherInstance, "Both instances should be the same");
    }

    @Test
    public void testCreateInitialStage() {
        Cell[][] stage = gameState.stage;
        assertNotNull(stage);
        assertEquals(36, stage.length);
        assertEquals(24, stage[0].length);

        for (int i = 0; i < 36; i++) {
            for (int j = 0; j < 24; j++) {
                assertEquals("0", stage[i][j].getValue());
                assertEquals("clear", stage[i][j].getStatus());
                assertEquals("0, 0, 0", stage[i][j].getColor());
            }
        }
    }

    @Test
    public void testGetRandomColor() {
        String color = gameState.getRandomColor();
        assertNotNull(color);
        assertFalse(color.isEmpty());
    }

    @Test
    public void testUpdateFromClient() {
        String sessionId = "player1";
        gameState.addPlayerTetromino(sessionId, new String[][]{{"T"}});
        gameState.addPlayerColor(sessionId, "255, 0, 0");

        Map<String, Object> data = new HashMap<>();
        data.put("sessionId", sessionId);
        data.put("posX", 0);
        data.put("posY", 0);

        gameState.updateFromClient(data);

        assertEquals("T", gameState.stage[0][0].getValue());
        assertEquals("clear", gameState.stage[0][0].getStatus());
        assertEquals("255, 0, 0", gameState.stage[0][0].getColor());
    }

    @Test
    public void testUpdateFromClientCollided() {
        String sessionId = "player1";
        gameState.addPlayerTetromino(sessionId, new String[][]{{"T"}});
        gameState.addPlayerColor(sessionId, "255, 0, 0");

        Map<String, Object> data = new HashMap<>();
        data.put("sessionId", sessionId);
        data.put("posX", 0);
        data.put("posY", 0);

        gameState.updateFromClientCollided(data);

        assertEquals("T", gameState.stage[0][0].getValue());
        assertEquals("merged", gameState.stage[0][0].getStatus());
        assertEquals("255, 0, 0", gameState.stage[0][0].getColor());
    }

    @Test
    public void testClearTetrominoPosition() {
        String sessionId = "player1";
        gameState.addPlayerTetromino(sessionId, new String[][]{{"T"}});
        gameState.addPlayerColor(sessionId, "255, 0, 0");

        Map<String, Object> data = new HashMap<>();
        data.put("sessionId", sessionId);
        data.put("posX", 0);
        data.put("posY", 0);

        gameState.updateFromClient(data);
        gameState.clearTetrominoPosition(sessionId);

        assertEquals("0", gameState.stage[0][0].getValue());
        assertEquals("clear", gameState.stage[0][0].getStatus());
        assertEquals("0, 0, 0", gameState.stage[0][0].getColor());
    }

    @Test
    public void testRotateTetromino() {
        String sessionId = "player1";
        gameState.addPlayerTetromino(sessionId, new String[][]{{"T", "0"}, {"T", "T"}});
        gameState.addPlayerColor(sessionId, "255, 0, 0");

        Map<String, Object> data = new HashMap<>();
        data.put("sessionId", sessionId);
        data.put("posX", 0);
        data.put("posY", 0);

        gameState.rotateTetromino(data);

        assertEquals("T", gameState.stage[0][0].getValue());
        assertEquals("clear", gameState.stage[0][0].getStatus());
        assertEquals("255, 0, 0", gameState.stage[0][0].getColor());
    }

    @Test
    public void testRemoveLines() {
        for (int i = 0; i < 24; i++) {
            gameState.stage[0][i] = new Cell("1", "merged", "255, 255, 255");
        }

        gameState.removeLines(1);

        for (int i = 0; i < 24; i++) {
            assertEquals("0", gameState.stage[0][i].getValue());
            assertEquals("clear", gameState.stage[0][i].getStatus());
            assertEquals("0, 0, 0", gameState.stage[0][i].getColor());
        }
    }

    @Test
    public void testGetNextXPosition() {
        int initialPosition = gameState.getNextXPosition();
        int nextPosition = gameState.getNextXPosition();
        assertNotEquals(initialPosition, nextPosition);
    }

    @Test
    public void testPersistTetrominoPosition() {
        String sessionId = "player1";
        String[][] tetromino = {{"T"}};
        String color = "255, 0, 0";

        gameState.persistTetrominoPosition(sessionId, 0, 0, tetromino, color);

        assertEquals("T", gameState.stage[0][0].getValue());
        assertEquals("clear", gameState.stage[0][0].getStatus());
        assertEquals("255, 0, 0", gameState.stage[0][0].getColor());
    }
}
