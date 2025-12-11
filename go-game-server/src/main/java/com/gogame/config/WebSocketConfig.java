package com.gogame.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

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
