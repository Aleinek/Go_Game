package com.gogame.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time game event notifications.
 * <p>
 * Configures STOMP over WebSocket with SockJS fallback for browsers
 * that don't support native WebSocket connections.
 * </p>
 * <p>
 * Message flow:
 * <ul>
 *   <li>Clients subscribe to {@code /topic/game/{playerId}} for game events</li>
 *   <li>Server publishes events like GAME_STARTED, OPPONENT_MOVED, etc.</li>
 *   <li>Client-to-server messages use {@code /app} prefix</li>
 * </ul>
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Włącz prosty broker wiadomości dla subskrypcji
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefix dla wiadomości wysyłanych z klienta do serwera
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefix dla wiadomości do konkretnych użytkowników
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Rejestruj endpoint WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // Fallback dla starszych przeglądarek
    }
}
