package com.demir.ecommerce.notificationservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Kullanıcıya özel mesaj prefix'i
        registry.enableSimpleBroker("/queue");

        // Sunucuya mesaj göndermek için prefix (şimdilik kullanılmıyor)
        registry.setApplicationDestinationPrefixes("/app");

        // Kullanıcıya özel yönlendirme prefix'i
        // convertAndSendToUser(...) bu prefix'i kullanır
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // prod'da frontend URL'ini yaz
                .withSockJS();                  // SockJS fallback desteği
    }
}