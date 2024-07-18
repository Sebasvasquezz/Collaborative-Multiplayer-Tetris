package Tetris.Tetris.controller;

import Tetris.Tetris.model.Cell;
import Tetris.Tetris.model.GameState;
import Tetris.Tetris.model.Tetrominos;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
    private static int totalPlayers = 2; // Número total de jugadores necesarios para comenzar el juego
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
                    System.out.println("JOIN");
                    handleJoin(session, data);
                    break;
                case "PLAYER_READY":
                    System.out.println("PLAYER_READY");
                    handlePlayerReady(session, data);
                    break;
                case "PLAYER_STATE":
                    System.out.println("PLAYER_STATE");
                    handlePlayerState(session, data);
                    break;
                case "REQUEST_NEW_TETROMINO":
                    System.out.println("REQUEST_NEW_TETROMINO");
                    String[][] tetromino = generateRandomTetromino();
                    sendTetrominoToPlayer(session.getId(), tetromino);
                    broadcastGameStates();
                    break;
                case "GAME_STATE":
                    System.out.println("PLAYER_STATE");
                    handleGameStateMessage(data);
                    break;
                case "STATE":
                    System.out.println("STATE");
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

    private void handleGameStateMessage(Map<String, Object> data) throws Exception {
        System.out.println("Received GAME_STATE message: " + data);
        // Obtener el tablero de juego del mensaje
        List<List<Map<String, String>>> gameBoardData = (List<List<Map<String, String>>>) data.get("gameBoard");

        // Convertir el gameBoardData en un tablero de celdas
        Cell[][] newGameBoard = new Cell[gameBoardData.size()][];
        for (int i = 0; i < gameBoardData.size(); i++) {
            List<Map<String, String>> rowData = gameBoardData.get(i);
            Cell[] cellRow = new Cell[rowData.size()];
            for (int j = 0; j < rowData.size(); j++) {
                Map<String, String> cellData = rowData.get(j);
                String value = cellData.get("value");
                String status = cellData.get("status");
                cellRow[j] = new Cell(value, status);
            }
            newGameBoard[i] = cellRow;
        }

        // Aquí asignas el nuevo estado del tablero a tu estado actual del juego
        gameState.setStage(newGameBoard);

        // Lógica adicional si es necesario
        // Por ejemplo, podrías querer notificar a otros jugadores sobre el nuevo estado
        // del juego
        broadcastGameStates();
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
        System.out.println("id:" + session.getId() + " name: " + name);
        updatePlayersStatus();
    }

    private void handlePlayerReady(WebSocketSession session, Map<String, Object> data) throws Exception {
        playerReadyStatus.put(session.getId(), true);
        updatePlayersStatus();
        if (allPlayersReady()) {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "START_GAME");

            String messageJson = gson.toJson(message);

            for (WebSocketSession sess : sessions) {
                if (sess.isOpen()) {
                    sess.sendMessage(new TextMessage(messageJson));
                    System.out.println("se mando a START_GAME" + sess.getId() + "    " + messageJson);
                }
            }
        }
    }

    private void handlePlayerState(WebSocketSession session, Map<String, Object> data) throws Exception {
        // Actualiza el estado del juego basado en el mensaje
        gameState.updateFromClient(data);
        // Transmitir el estado actualizado a todos los clientes
        broadcastGameStates();
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
        Map<String, Object> message = new HashMap<>();
        message.put("type", "NEW_TETROMINO");
        message.put("tetromino", tetromino);
        message.put("posX", gameState.getNextXPosition());

        String messageJson = gson.toJson(message);

        for (WebSocketSession session : sessions) {
            System.out.println(session.getId());
            if (session.getId().equals(playerId)) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(messageJson));
                        System.out.println("se mando a NEW_TETROMINO" + playerId + "    " + messageJson);
                        System.out.println(playerNames.get(session.getId()));
                        gameState.updateFromClient(message);
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
        message.put("gameState", gameState.stage);

        String messageJson = gson.toJson(message);

        for (WebSocketSession session : sessions) {
            System.out.println(session.getId());
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(messageJson));
                System.out.println("se mando GAME_STATES a " + session.getId() + "    " + messageJson);
            }
        }
    }

    private void broadcastStartGame() throws Exception {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "START_GAME");
        message.put("matrix", gameState.stage);

        String messageJson = gson.toJson(message);

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(messageJson));
                System.out.println("se mando a START_GAME" + session.getId() + "    " + messageJson);
            }
        }
    }
}