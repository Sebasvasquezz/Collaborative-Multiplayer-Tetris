package Tetris.Tetris.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import Tetris.Tetris.controller.GameWebSocketHandler;

/**
 * Configuration class for WebSocket support in the Tetris game.
 * This class enables WebSocket communication and registers WebSocket handlers.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GameWebSocketHandler gameWebSocketHandler;

    /**
     * Constructor for WebSocketConfig.
     *
     * @param gameWebSocketHandler the WebSocket handler for the game
     */
    public WebSocketConfig(GameWebSocketHandler gameWebSocketHandler) {
        this.gameWebSocketHandler = gameWebSocketHandler;
    }

    /**
     * Registers WebSocket handlers.
     *
     * @param registry the registry to add the WebSocket handlers to
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gameWebSocketHandler, "/lobby").setAllowedOrigins("*");
    }
}