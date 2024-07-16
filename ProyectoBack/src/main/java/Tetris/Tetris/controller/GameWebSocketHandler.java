package Tetris.Tetris.controller;

import Tetris.Tetris.model.GameState;
import Tetris.Tetris.model.Tetrominos;

import com.google.gson.Gson;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;

public class GameWebSocketHandler extends TextWebSocketHandler {

    private static CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private static GameState gameState = new GameState();
    private static ConcurrentHashMap<String, Boolean> playerReadyStatus = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> playerNames = new ConcurrentHashMap<>();
    private static int totalPlayers = 2;  // Número total de jugadores necesarios para comenzar el juego
    private static Gson gson = new Gson();

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
                    System.out.println("uno");
                    handleJoin(session, data);
                    break;
                case "PLAYER_READY":
                    System.out.println("dos");
                    handlePlayerReady(session, data);
                    break;
                case "PLAYER_STATE":
                    System.out.println("tres");
                    handlePlayerState(session, data);
                    break;
                case "REQUEST_NEW_TETROMINO":
                    System.out.println("cuatro");
                    String[][] tetromino = generateRandomTetromino();
                    gameState.assignTetromino(session.getId(), tetromino);
                    sendTetrominoToPlayer(session.getId(),tetromino);
                    broadcastGameStates();
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

        String jsonMessage = gson.toJson(message);

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(jsonMessage));
            }
        }
    }

    private void handleJoin(WebSocketSession session, Map<String, Object> data) throws Exception{
        String name = (String) data.get("name");
        playerNames.put(session.getId(), name);
        updatePlayersStatus();
    }

    private void handlePlayerReady(WebSocketSession session, Map<String, Object> data) throws Exception{
        playerReadyStatus.put(session.getId(), true);
        updatePlayersStatus();
        if (allPlayersReady()) {
            startGame();
        }
    }

    private void handlePlayerState(WebSocketSession session, Map<String, Object> data) throws Exception{
        // Actualiza el estado del juego basado en el mensaje
        gameState.updateFromClient(data);
        // Transmitir el estado actualizado a todos los clientes
        broadcastGameStates();
    }

    private boolean allPlayersReady() {
        return playerReadyStatus.values().stream().allMatch(Boolean::booleanValue);
    }

    private void startGame() throws Exception {
        gameState.reset(); // Resetear el estado del juego
        broadcastStartGame();
    }
    private String[][] generateRandomTetromino() {
        String[] tetrominoKeys = Tetrominos.TETROMINOS.keySet().toArray(new String[0]);
        Random random = new Random();
        String randomKey = tetrominoKeys[random.nextInt(tetrominoKeys.length)];
        return Tetrominos.TETROMINOS.get(randomKey);
    }

    private void sendTetrominoToPlayer(String playerId, String[][] tetromino) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "NEW_TETROMINO");
        message.put("tetromino", tetromino);
        message.put("pos", gameState.getNextXPosition());

        String messageJson = gson.toJson(message);
       
        for (WebSocketSession session : sessions) {
            System.out.println(session.getId());
            if (session.getId().equals(playerId)) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(messageJson));
                        System.out.println("se mando a "+ playerId + "    "+ messageJson);
                        System.out.println(playerNames.get(session.getId()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void broadcastGameStates() throws Exception {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "GAME_STATES");
        message.put("gameState", gameState);

        String jsonMessage = gson.toJson(message);

        for (WebSocketSession session : sessions) {
            System.out.println(session.getId());
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(jsonMessage));
            }
        }
    }
    private void broadcastStartGame() throws Exception {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "START_GAME");

        String jsonMessage = gson.toJson(message);

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(jsonMessage));
            }
        }
    }
}