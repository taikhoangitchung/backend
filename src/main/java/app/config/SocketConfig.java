package app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class SocketConfig implements WebSocketConfigurer {
    @Autowired
    private final KickWebSocketHandler kickWebSocketHandler;

    public SocketConfig(KickWebSocketHandler kickWebSocketHandler) {
        this.kickWebSocketHandler = kickWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new SocketHandler(), "/ws")
                .setAllowedOriginPatterns("*");
        registry.addHandler(kickWebSocketHandler, "/ws/kick").setAllowedOrigins("*");
    }
}
