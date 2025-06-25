package app.config;

import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class KickWebSocketHandler extends TextWebSocketHandler {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(@Nullable WebSocketSession session) {
        String email = getEmail(session);

        if (email != null) {
            sessions.put(email, session);
        }
    }

    @Override
    public void afterConnectionClosed(@Nullable WebSocketSession session, @Nullable CloseStatus status) {
        String email = getEmail(session);
        if (email != null) {
            sessions.remove(email);
        }
    }

    public void kickUser(String email) throws IOException {
        WebSocketSession session = sessions.get(email);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage("KICK"));
            session.close();
        }
    }

    private String getEmail(@Nullable WebSocketSession session) {
        if (session != null) {
            URI uri = session.getUri();
            if (uri == null) return null;
            String query = uri.getQuery();
            if (query != null && query.startsWith("email=")) {
                return query.split("=")[1];
            }
        }
        return null;
    }
}
