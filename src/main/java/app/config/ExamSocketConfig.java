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

    @Autowired
    public ExamSocketConfig(ExamSocketHandler examSocketHandler) {
        this.examSocketHandler = examSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(examSocketHandler, "/ws")
                .setAllowedOriginPatterns("*");
    }
}
