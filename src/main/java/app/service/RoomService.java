package app.service;

import app.config.SocketHandler;
import app.dto.room.CreateRoomRequest;
import app.dto.room.RoomWaitingResponse;
import app.entity.Room;
import app.entity.User;
import app.exception.AuthException;
import app.exception.LockedException;
import app.exception.NotFoundException;
import app.repository.RoomRepository;
import app.util.MessageHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final ExamService examService;
    private final UserService userService;
    private final MessageHelper messageHelper;

    public String createRoom(CreateRoomRequest request) {
        Room room = new Room();
        room.setExam(examService.findById(request.getExamId()));
        room.setHost(userService.findInAuth());
        room.setStatus(Room.Status.WAITING);
        room.setCode(generateRoomCode());
        roomRepository.save(room);
        return room.getCode();
    }

    public Room findByCode(String code) {
        Room room = roomRepository.findByCode(code);
        if (room == null) {
            throw new NotFoundException(messageHelper.get("room.not.found"));
        }
        return room;
    }

    public void startRoom(String code) {
        Room room = findByCode(code);

        User currentUser = userService.findInAuth();

        if (!room.getHost().equals(currentUser)) {
            throw new AuthException(messageHelper.get("room.host.not.match"));
        }

        if (room.getStatus() != Room.Status.WAITING) {
            throw new LockedException(messageHelper.get("room.locked"));
        }

        room.setStatus(Room.Status.STARTED);
        roomRepository.save(room);
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(Map.of("type", "STARTED", "examId", room.getExam().getId()));
            SocketHandler.broadcast(code, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String generateRoomCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
        } while (roomRepository.existsByCode(code));
        return code;
    }

    public RoomWaitingResponse joinRoom(String code) {
        Room room = findByCode(code);

        if (room.getStatus() != Room.Status.WAITING) {
            throw new LockedException(messageHelper.get("room.locked"));
        }

        User user = userService.findInAuth();
        boolean alreadyJoined = room.getCandidates().stream()
                .anyMatch(foundUser -> foundUser.getId().equals(user.getId()));
        if (!alreadyJoined) {
            room.getCandidates().add(user);
            roomRepository.save(room);
        }
        List<String> candidateNames = room.getCandidates().stream()
                .map(User::getUsername)
                .toList();
        return new RoomWaitingResponse(
                room.getCode(),
                room.getExam().getTitle(),
                room.getExam().getAuthor().getUsername(),
                room.getStatus(),
                room.getHost().getUsername(),
                candidateNames
        );
    }

    public Room check(String code) {
        Room foundRoom = findByCode(code);
        if (foundRoom.getStatus() != Room.Status.WAITING) {
            throw new LockedException(messageHelper.get("room.locked"));
        }
        return foundRoom;
    }

    public RoomWaitingResponse leaveRoom(String code) {
        Room room = findByCode(code);
        room.getCandidates().remove(userService.findInAuth());
        roomRepository.save(room);
        List<String> candidateNames = room.getCandidates().stream()
                .map(User::getUsername)
                .toList();
        return new RoomWaitingResponse(
                room.getCode(),
                room.getExam().getTitle(),
                room.getExam().getAuthor().getUsername(),
                room.getStatus(),
                room.getHost().getUsername(),
                candidateNames
        );
    }
}
