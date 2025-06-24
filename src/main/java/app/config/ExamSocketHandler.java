package app.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ExamSocketHandler extends TextWebSocketHandler {

    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private final Map<String, String> sessionRoomMap = new ConcurrentHashMap<>();
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
            if (parts.length >= 3) {
                String roomId = parts[1];
                String username = parts[2];
                Set<WebSocketSession> roomSessions = rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet());
                roomSessions.add(session);
                sessionRoomMap.put(session.getId(), roomId);
                session.getAttributes().put("username", username);
                broadcast(roomId, String.format("""
                            {
                                "type": "JOIN",
                                "username": "%s",
                                "message": "%s đã vào phòng"
                            }
                        """, username, username));
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
                                "type": "START",
                                "message": "Bắt đầu bài thi"
                            }
                        """);
            }

        } else if (payload.startsWith("SUBMIT:")) {
            String[] parts = payload.split(":");
            if (parts.length >= 2) {
                String roomId = parts[1];
                System.out.printf("📩 Nhận SUBMIT từ phòng %s%n", roomId);
                submittedCount.compute(roomId, (key, old) -> old == null ? 1 : old + 1);
                checkAndBroadcastEnd(roomId);
            }
        }
    }

    private void checkAndBroadcastEnd(String roomId) {
        int submitted = submittedCount.getOrDefault(roomId, 0);
        int expected = submitExpectCount.getOrDefault(roomId, 0);

        System.out.printf("📊 Phòng %s: Submitted %d / Expected %d%n", roomId, submitted, expected);

        if (submitted >= expected && expected > 0) {
            try {
                broadcast(roomId, """
                            {
                                "type": "END",
                                "message": "Tất cả đã nộp bài"
                            }
                        """);
                submittedCount.remove(roomId);
                submitExpectCount.remove(roomId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcast(String roomId, String message) throws Exception {
        Set<WebSocketSession> sessions = rooms.get(roomId);
        System.out.println("session còn" + roomId);
        if (sessions != null) {
            System.out.println(sessions);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    System.out.println(message);
                    session.sendMessage(new TextMessage(message));
                }else {
                    System.out.println("session bị đóng");
                }
            }
        }else{
            System.out.println("session null");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        System.out.println("ngắt");
        String roomId = sessionRoomMap.remove(session.getId());
        String username = (String) session.getAttributes().get("username");

        if (roomId != null && rooms.containsKey(roomId)) {
            rooms.get(roomId).remove(session);
        }

        if (username != null && roomId != null) {
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
