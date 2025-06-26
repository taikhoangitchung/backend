package app.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ExamSocketHandler extends TextWebSocketHandler {
    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private final Map<String, Integer> submitExpectCount = new ConcurrentHashMap<>();
    private final Map<String, Integer> submittedCount = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("✅ Client connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        if (payload.startsWith("JOIN:")) {
            String[] parts = payload.split(":");
            if (parts.length >= 4) {
                String roomId = parts[1];
                String email = parts[2];
                String username = parts[3];

                rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);

                session.getAttributes().put("roomId", roomId);
                session.getAttributes().put("email", email);
                session.getAttributes().put("username", username);

                broadcast(roomId, String.format("""
                    {
                        "type": "JOIN",
                        "username": "%s"
                    }
                """, username));
            }

        } else if (payload.startsWith("START:")) {
            String[] parts = payload.split(":");
            if (parts.length >= 3) {
                String roomId = parts[1];
                int expected = Integer.parseInt(parts[2]);

                submitExpectCount.put(roomId, expected);
                submittedCount.put(roomId, 0);

                broadcast(roomId, """
                    {
                        "type": "START"
                    }
                """);
            }

        } else if (payload.startsWith("SUBMIT:")) {
            String[] parts = payload.split(":");
            if (parts.length >= 2) {
                String roomId = parts[1];
                String email = (String) session.getAttributes().get("email");
                String username = (String) session.getAttributes().get("username");

                submittedCount.compute(roomId, (k, v) -> (v == null ? 1 : v + 1));

                broadcast(roomId, String.format("""
                    {
                        "type": "SUBMIT",
                        "email": "%s",
                        "username":"%s"
                    }
                """, email, username));
                checkAndBroadcastEnd(roomId);
            }
        }
    }

    private void checkAndBroadcastEnd(String roomId) {
        int submitted = submittedCount.getOrDefault(roomId, 0);
        int expected = submitExpectCount.getOrDefault(roomId, 0);
        if (submitted >= expected && expected > 0) {
            try {
                broadcast(roomId, """
                    {
                        "type": "END"
                    }
                """);
                submittedCount.remove(roomId);
                submitExpectCount.remove(roomId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void broadcast(String roomId, String message) throws Exception {
        Set<WebSocketSession> sessions = rooms.get(roomId);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String roomId = (String) session.getAttributes().get("roomId");
        String email = (String) session.getAttributes().get("email");

        if (roomId != null && rooms.containsKey(roomId)) {
            rooms.get(roomId).remove(session);
        }

        if (email != null && roomId != null) {
            try {
                broadcast(roomId, String.format("""
                    {
                        "type": "LEAVE",
                        "email": "%s"
                    }
                """, email));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
