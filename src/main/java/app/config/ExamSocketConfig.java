package app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class ExamSocketConfig implements WebSocketConfigurer {
    private final ExamSocketHandler examSocketHandler;
    private final KickWebSocketHandler kickWebSocketHandler;

    @Autowired
    public ExamSocketConfig(ExamSocketHandler examSocketHandler, KickWebSocketHandler kickWebSocketHandler) {
        this.examSocketHandler = examSocketHandler;
        this.kickWebSocketHandler = kickWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(examSocketHandler, "/ws/rooms")
                .setAllowedOriginPatterns("*");
        registry.addHandler(kickWebSocketHandler, "/ws/kick")
                .setAllowedOrigins("*");
    }
}
