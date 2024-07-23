package Tetris.Tetris.controller;

import Tetris.Tetris.model.GameState;
import Tetris.Tetris.model.Score;
import Tetris.Tetris.model.Tetrominos;
import Tetris.Tetris.repository.ScoreRepository;

import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * WebSocket handler for managing game communication.
 * This class handles WebSocket connections, messages, and game state updates.
 */
@Component
public class GameWebSocketHandler extends TextWebSocketHandler {
    private static ReentrantLock lock = new ReentrantLock();
    private static CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private static GameState gameState = GameState.getInstance();
    private static ConcurrentHashMap<String, Boolean> playerReadyStatus = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> playerNames = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Integer> playerScores = new ConcurrentHashMap<>();
    private static Gson gson = new Gson();
    
    @Autowired
    private ScoreRepository scoreRepository;

     /**
     * Called after a WebSocket connection is established.
     * Adds the session to the list of sessions and initializes player status.
     *
     * @param session the WebSocket session
     * @throws Exception if an error occurs
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        playerReadyStatus.put(session.getId(), false); 
        playerNames.put(session.getId(), "Unknown"); 

        updatePlayersStatus();
    }

    /**
     * Handles incoming text messages from WebSocket clients.
     * Dispatches messages based on their type.
     *
     * @param session the WebSocket session
     * @param message the incoming text message
     * @throws Exception if an error occurs
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        Map<String, Object> data = gson.fromJson(payload, Map.class);
        try {
            String type = (String) data.get("type");
            switch (type) {
                case "JOIN":
                    handleJoin(session, data);
                    break;
                case "PLAYER_READY":
                    handlePlayerReady(session);
                    break;
                case "REQUEST_NEW_TETROMINO":
                    handleRequestNewTetrominoMessage(session);
                    break;
                case "GAME_STATE":
                    handleGameStateMessage(session, data);
                    break;
                case "LINES_CLEARED":
                    handleLinesClearedMessage(session, data);
                    break;
                case "PLAYER_LOST":
                    handlePlayerLostMessage(session, data);
                    break;
                case "SEND_SCORE":
                    handleSendScoreMessage(session, data);
                    break;
                case "REQUEST_FINAL_SCORES":
                    handleRequestFinalScores(session);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown message type: " + type);
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    /**
     * Called after a WebSocket connection is closed.
     * Removes the session from the list of sessions and updates player status.
     *
     * @param session the WebSocket session
     * @param status the close status
     * @throws Exception if an error occurs
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        playerReadyStatus.remove(session.getId());
        playerNames.remove(session.getId());

        updatePlayersStatus();
    }

    /**
     * Handles a request for a new tetromino.
     * Generates a random tetromino and sends it to the requesting player.
     *
     * @param session the WebSocket session
     * @throws Exception if an error occurs
     */
    private void handleRequestNewTetrominoMessage(WebSocketSession session) throws Exception {
        String[][] tetromino = generateRandomTetromino();
        sendTetrominoToPlayer(session.getId(), tetromino);
        broadcastGameStates();
    }

    /**
     * Handles a game state update message.
     * Updates the game state based on the received data.
     *
     * @param session the WebSocket session
     * @param data the game state data
     * @throws Exception if an error occurs
     */
    private void handleGameStateMessage(WebSocketSession session, Map<String, Object> data) throws Exception {
        if (data.containsKey("rotated") && (Boolean) data.get("rotated")) {
            gameState.rotateTetromino(data);
        } else if (data.containsKey("collided") && (Boolean) data.get("collided")) {
            gameState.updateFromClientCollided(data);
        } else {
            gameState.updateFromClient(data);
        }

        broadcastGameStates();
    }

    /**
     * Handles a lines cleared message.
     * Updates the game state based on the number of lines cleared.
     *
     * @param session the WebSocket session
     * @param data the lines cleared data
     * @throws Exception if an error occurs
     */
    private void handleLinesClearedMessage(WebSocketSession session, Map<String, Object> data) throws Exception {
        int linesCleared = gameState.getIntFromData(data, "linesCleared");
        gameState.removeLines(linesCleared);
        broadcastGameStates();
    }

     /**
     * Handles a player lost message.
     * Requests the scores from all players.
     *
     * @param session the WebSocket session
     * @param data the player lost data
     * @throws Exception if an error occurs
     */
    private void handlePlayerLostMessage(WebSocketSession session, Map<String, Object> data) throws Exception {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "REQUEST_SCORES");

        String messageJson = gson.toJson(message);
        for (WebSocketSession sess : sessions) {
            if (sess.isOpen()) {
                sess.sendMessage(new TextMessage(messageJson));
            }
        }
    }

    /**
     * Handles a send score message.
     * Updates the player's score in the game state and database.
     *
     * @param session the WebSocket session
     * @param data the score data
     * @throws Exception if an error occurs
     */
    private void handleSendScoreMessage(WebSocketSession session, Map<String, Object> data) throws Exception {
        String sessionId = (String) data.get("sessionId");
        String playerName = playerNames.get(sessionId); 
        int score = gameState.getIntFromData(data, "score");

        playerScores.put(sessionId, score);


        Score scoreEntry = new Score(playerName, score);
        scoreRepository.save(scoreEntry);
    }

    /**
     * Handles a request for final scores.
     * Sends the final scores to all players.
     *
     * @param session the WebSocket session
     * @throws Exception if an error occurs
     */
    private void handleRequestFinalScores(WebSocketSession session) throws Exception {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "FINAL_SCORES");

        List<Map<String, Object>> scores = new ArrayList<>();
        for (String sessionId : playerScores.keySet()) {
            Map<String, Object> scoreData = new HashMap<>();
            scoreData.put("sessionId", sessionId);
            scoreData.put("score", playerScores.get(sessionId));
            scoreData.put("name", playerNames.get(sessionId));
            scores.add(scoreData);
        }

        message.put("scores", scores);

        String messageJson = gson.toJson(message);
        for (WebSocketSession sess : sessions) {
            if (sess.isOpen()) {
                sess.sendMessage(new TextMessage(messageJson));
            }
        }
    }

    /**
     * Updates the status of all players.
     * Sends the updated player status to all players.
     *
     * @throws Exception if an error occurs
     */
    private void updatePlayersStatus() throws Exception {
        List<Map<String, String>> players = new ArrayList<>();
        for (WebSocketSession session : sessions) {
            Map<String, String> player = new HashMap<>();
            player.put("name", playerNames.get(session.getId()));
            player.put("isReady", playerReadyStatus.get(session.getId()).toString());
            players.add(player);
        }

        Map<String, Object> message = new HashMap<>();
        message.put("type", "UPDATE_PLAYERS");
        message.put("players", players);

        String messageJson = gson.toJson(message);

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(messageJson));
            }
        }
    }

    /**
     * Handles a join message.
     * Adds the player to the game and assigns a random color.
     *
     * @param session the WebSocket session
     * @param data the join data
     * @throws Exception if an error occurs
     */
    private void handleJoin(WebSocketSession session, Map<String, Object> data) throws Exception {
        String name = (String) data.get("name");
        playerNames.put(session.getId(), name);
        gameState.addPlayerColor(session.getId(), gameState.getRandomColor());
        updatePlayersStatus();
    }

    /**
     * Handles a player ready message.
     * Updates the player's ready status and starts the game if all players are ready.
     *
     * @param session the WebSocket session
     * @throws Exception if an error occurs
     */
    private void handlePlayerReady(WebSocketSession session) throws Exception {
        playerReadyStatus.put(session.getId(), true);
        updatePlayersStatus();
        if (allPlayersReady()) {

            for (WebSocketSession sess : sessions) {
                sendStartGame(sess);
            }
        }
    }

     /**
     * Sends a start game message to the player.
     *
     * @param session the WebSocket session
     * @throws Exception if an error occurs
     */
    private void sendStartGame(WebSocketSession session) throws Exception {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "START_GAME");
        message.put("stage", gameState.stage);
        message.put("id", session.getId());
        message.put("color", gameState.getPlayerColor(session.getId()));

        String messageJson = gson.toJson(message);
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(messageJson));
        }
    }

     /**
     * Checks if all players are ready.
     *
     * @return true if all players are ready, false otherwise
     */
    private boolean allPlayersReady() {
        return playerReadyStatus.values().stream().allMatch(Boolean::booleanValue);
    }

    /**
     * Generates a random tetromino.
     *
     * @return a random tetromino
     */
    private String[][] generateRandomTetromino() {
        String[] tetrominoKeys = Tetrominos.TETROMINOS.keySet().toArray(new String[0]);
        Random random = new Random();
        String randomKey = tetrominoKeys[random.nextInt(tetrominoKeys.length)];
        return Tetrominos.TETROMINOS.get(randomKey);

    }

     /**
     * Sends a tetromino to the player.
     *
     * @param playerId the player's ID
     * @param tetromino the tetromino to send
     */
    private void sendTetrominoToPlayer(String playerId, String[][] tetromino) {
        gameState.addPlayerTetromino(playerId, tetromino);
        Map<String, Object> message = new HashMap<>();
        message.put("type", "NEW_TETROMINO");
        message.put("tetromino", tetromino);
        message.put("posX", gameState.getNextXPosition());

        String messageJson = gson.toJson(message);

        for (WebSocketSession session : sessions) {

            if (session.getId().equals(playerId)) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(messageJson));
                        message.put("sessionId", playerId);
                        message.put("posY", 0);
                        gameState.updateFromClient(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

     /**
     * Broadcasts the current game states to all players.
     */
    private void broadcastGameStates() {
        lock.lock();
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "GAME_STATES");
            message.put("gameState", gameState.stage);

            String messageJson = gson.toJson(message);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(messageJson));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

}
