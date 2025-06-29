package app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class ExamSocketHandler extends TextWebSocketHandler {
    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private final Map<String, Integer> submitExpectCount = new ConcurrentHashMap<>();
    private final Map<String, Integer> submittedCount = new ConcurrentHashMap<>();
    private final Map<String, List<Map<String, String>>> submittedUsersByRoom = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("✅ Client connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        if (payload.startsWith("JOIN:")) {
            String[] parts = payload.split(":");
            if (parts.length >= 5) {
                String roomId = parts[1];
                String email = parts[2];
                String username = parts[3];
                boolean isHost = Boolean.parseBoolean(parts[4]);
                String avatar = parts[5];
                rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);
                session.getAttributes().put("roomId", roomId);
                session.getAttributes().put("email", email);
                session.getAttributes().put("username", username);
                session.getAttributes().put("host", isHost);
                session.getAttributes().put("avatar", avatar);
                Set<WebSocketSession> roomSessions = rooms.get(roomId);
                String hostEmail = roomSessions.stream()
                        .filter(s -> Boolean.TRUE.equals(s.getAttributes().get("host")))
                        .map(s -> (String) s.getAttributes().get("email"))
                        .findFirst()
                        .orElse(null);
                String candidateList = roomSessions.stream()
                        .filter(s -> {
                            String e = (String) s.getAttributes().get("email");
                            return e != null && !e.equals(hostEmail);
                        })
                        .map(s -> {
                            String name = (String) s.getAttributes().get("username");
                            String avatarUrl = (String) s.getAttributes().get("avatar");
                            return String.format("""
                                        {
                                            "username": "%s",
                                            "avatar": "%s"
                                        }
                                    """, name, avatarUrl);
                        })
                        .collect(Collectors.joining(","));
                String response = String.format("""
                            {
                                "type": "JOIN",
                                "username": "%s",
                                "email": "%s",
                                "candidates": [%s]
                            }
                        """, username, email, candidateList);
                broadcast(roomId, response);
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
                submittedUsersByRoom.computeIfAbsent(roomId, k -> new ArrayList<>());
                List<Map<String, String>> submittedList = submittedUsersByRoom.get(roomId);
                boolean alreadySubmitted = submittedList.stream()
                        .anyMatch(user -> user.get("email").equals(email));
                if (!alreadySubmitted) {
                    Map<String, String> userInfo = new HashMap<>();
                    userInfo.put("email", email);
                    userInfo.put("username", username);
                    submittedList.add(userInfo);
                }
                broadcast(roomId, String.format("""
                            {
                                "type": "SUBMIT",
                                "users": %s
                            }
                        """, new ObjectMapper().writeValueAsString(submittedList)));
                checkAndBroadcastEnd(roomId);
            }
        }
    }

    private void checkAndBroadcastEnd(String roomId) {
        int submitted = submittedCount.getOrDefault(roomId, 0);
        int expected = submitExpectCount.getOrDefault(roomId, 0);
        if (submitted >= expected) {
            try {
                broadcast(roomId, """
                            {
                                "type": "END"
                            }
                        """);
                submittedCount.remove(roomId);
                submitExpectCount.remove(roomId);
                submittedUsersByRoom.remove(roomId);
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
        String username = (String) session.getAttributes().get("username");

        if (roomId != null) {
            Set<WebSocketSession> room = rooms.get(roomId);
            if (room != null) {
                room.remove(session);
                String hostEmail = room.stream()
                        .filter(s -> Boolean.TRUE.equals(s.getAttributes().get("host")))
                        .map(s -> (String) s.getAttributes().get("email"))
                        .findFirst()
                        .orElse(null);
                boolean started = submitExpectCount.containsKey(roomId);
                if (started) {
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Set<WebSocketSession> updatedRoom = rooms.get(roomId);
                            boolean stillMissing = updatedRoom == null || updatedRoom.stream()
                                    .noneMatch(s -> email.equals(s.getAttributes().get("email")));
                            if (stillMissing) {
                                submitExpectCount.computeIfPresent(roomId, (k, v) -> Math.max(0, v - 1));
                                checkAndBroadcastEnd(roomId);
                            }
                        }
                    }, 15000);
                }
                List<Map<String, String>> submittedList = submittedUsersByRoom.get(roomId);
                boolean hasSubmitted = submittedList != null &&
                        submittedList.stream().anyMatch(user -> email != null && email.equals(user.get("email")));
                if (!hasSubmitted && submittedList != null) {
                    submittedList.removeIf(user -> email != null && email.equals(user.get("email")));
                }
                String candidateList = room.stream()
                        .filter(s -> {
                            String e = (String) s.getAttributes().get("email");
                            return e != null && !e.equals(hostEmail);
                        })
                        .map(s -> {
                            String name = (String) s.getAttributes().get("username");
                            String avatarUrl = (String) s.getAttributes().get("avatar");
                            return String.format("""
                                        {
                                            "username": "%s",
                                            "avatar": "%s"
                                        }
                                    """, name, avatarUrl);
                        })
                        .collect(Collectors.joining(","));
                String leaveMessage = String.format("""
                            {
                                "type": "LEAVE",
                                "username": "%s",
                                "email": "%s",
                                "candidates": [%s]
                            }
                        """, username, email, candidateList);
                try {
                    broadcast(roomId, leaveMessage);
                    checkAndBroadcastEnd(roomId);
                } catch (Exception e) {
                    System.err.println("❌ Failed to broadcast LEAVE message:");
                    e.printStackTrace();
                }
            }
        }
    }
}
