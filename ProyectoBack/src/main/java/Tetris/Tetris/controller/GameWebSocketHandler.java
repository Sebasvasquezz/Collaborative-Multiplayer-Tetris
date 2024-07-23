package Tetris.Tetris.controller;

import Tetris.Tetris.model.Cell;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {
    private static ReentrantLock lock = new ReentrantLock();
    private static CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private static GameState gameState = new GameState();
    private static ConcurrentHashMap<String, Boolean> playerReadyStatus = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> playerNames = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> playerScores = new ConcurrentHashMap<>();
    private static Gson gson = new Gson();
    
    @Autowired
    private ScoreRepository scoreRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        playerReadyStatus.put(session.getId(), false); // Asegúrate de que el jugador no esté listo inicialmente
        playerNames.put(session.getId(), "Unknown"); // Nombre predeterminado

        updatePlayersStatus();
    }

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
                    String[][] tetromino = generateRandomTetromino();
                    sendTetrominoToPlayer(session.getId(), tetromino);
                    broadcastGameStates();
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

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        playerReadyStatus.remove(session.getId());
        playerNames.remove(session.getId());

        updatePlayersStatus();
    }

    private void handleGameStateMessage(WebSocketSession session, Map<String, Object> data) throws Exception {

        // Lógica adicional si es necesario
        if (data.containsKey("rotated") && (Boolean) data.get("rotated")) {
            // Manejar la rotación del tetromino
            gameState.rotateTetromino(data);
        } else if (data.containsKey("collided") && (Boolean) data.get("collided")) {
            // No limpiar el tetromino si ha colisionado
            gameState.updateFromClientCollided(data);
        } else {
            gameState.updateFromClient(data);
        }

        broadcastGameStates();
    }

    private void handleLinesClearedMessage(WebSocketSession session, Map<String, Object> data) throws Exception {

        int linesCleared = gameState.getIntFromData(data, "linesCleared");
        // Lógica adicional para manejar las líneas limpiadas
        gameState.removeLines(linesCleared);

        broadcastGameStates();
    }

    private void handlePlayerLostMessage(WebSocketSession session, Map<String, Object> data) throws Exception {
        // Crear el mensaje para solicitar los puntajes de todos los jugadores
        Map<String, Object> message = new HashMap<>();
        message.put("type", "REQUEST_SCORES");

        String messageJson = gson.toJson(message);
        for (WebSocketSession sess : sessions) {
            if (sess.isOpen()) {
                sess.sendMessage(new TextMessage(messageJson));
            }
        }
    }

    private void handleSendScoreMessage(WebSocketSession session, Map<String, Object> data) throws Exception {
        System.out.println("Received SEND_SCORE message: " + data);

        String sessionId = (String) data.get("sessionId");
        String playerName = playerNames.get(sessionId); // Retrieve the player's name
        int score = gameState.getIntFromData(data, "score");

        playerScores.put(sessionId, score);

        // Guardar en MongoDB
        Score scoreEntry = new Score(playerName, score);
        scoreRepository.save(scoreEntry);

        System.out.println("Player Scores: " + playerScores);
    }

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

    private void handleJoin(WebSocketSession session, Map<String, Object> data) throws Exception {
        String name = (String) data.get("name");
        playerNames.put(session.getId(), name);
        gameState.addPlayerColor(session.getId(), gameState.getRandomColor());
        updatePlayersStatus();
        System.out.println("Se unió " + name + "con el id: " + session.getId());
    }

    private void handlePlayerReady(WebSocketSession session) throws Exception {
        playerReadyStatus.put(session.getId(), true);
        updatePlayersStatus();
        if (allPlayersReady()) {

            for (WebSocketSession sess : sessions) {
                sendStartGame(sess);
            }
            // TimeUnit.SECONDS.sleep(3);
            // for (WebSocketSession sess : sessions) {
            // sendStartGame(sess);
            // }
        }
    }

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

    private boolean allPlayersReady() {
        return playerReadyStatus.values().stream().allMatch(Boolean::booleanValue);
    }

    private String[][] generateRandomTetromino() {
        String[] tetrominoKeys = Tetrominos.TETROMINOS.keySet().toArray(new String[0]);
        Random random = new Random();
        String randomKey = tetrominoKeys[random.nextInt(tetrominoKeys.length)];
        return Tetrominos.TETROMINOS.get(randomKey);

    }

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
