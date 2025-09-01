package com.smartcar.monitoring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${websocket.endpoint:/ws}")
    private String websocketEndpoint;

    @Value("${websocket.allowed-origins:*}")
    private String allowedOrigins;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(websocketEndpoint)
                .setAllowedOriginPatterns(allowedOrigins)
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple memory-based message broker for sending messages to clients
        registry.enableSimpleBroker("/topic", "/queue");
        
        // Set prefix for client-to-server messages
        registry.setApplicationDestinationPrefixes("/app");
        
        // Set prefix for user-specific messages
        registry.setUserDestinationPrefix("/user");
    }
}
