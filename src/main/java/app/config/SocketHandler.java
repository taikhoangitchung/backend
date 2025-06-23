package app.config;

import lombok.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SocketHandler extends TextWebSocketHandler {
    private static final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private static final Map<String, String> sessionRoomMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("Client kết nối: " + session.getId());
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        if (payload.startsWith("JOIN:")) {
            String[] parts = payload.split(":");
            if (parts.length >= 3) {
                String roomId = parts[1];
                String username = parts[2];
                rooms.putIfAbsent(roomId, ConcurrentHashMap.newKeySet());
                rooms.get(roomId).add(session);
                sessionRoomMap.put(session.getId(), roomId);
                session.getAttributes().put("username", username); // 🔧 thêm dòng này
                broadcast(roomId, String.format("""
                            {
                                "type": "JOIN",
                                "username": "%s",
                                "message": "%s đã vào phòng"
                            }
                        """, username, username));
            }
        }
    }

    public static void broadcast(String roomCode, String message) throws Exception {
        Set<WebSocketSession> sessions = rooms.get(roomCode);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) {
        String roomId = sessionRoomMap.remove(session.getId());
        String username = (String) session.getAttributes().get("username");

        if (roomId != null) {
            Set<WebSocketSession> sessions = rooms.get(roomId);
            if (sessions != null) {
                sessions.remove(session);
            }

            if (username != null) {
                try {
                    broadcast(roomId, String.format("""
                                {
                                    "type": "LEAVE",
                                    "username": "%s",
                                    "message": "%s đã rời phòng"
                                }
                            """, username, username));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
