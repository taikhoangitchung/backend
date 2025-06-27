package app.service;

import app.dto.room.CreateRoomRequest;
import app.dto.room.RoomWaitingResponse;
import app.entity.Room;
import app.entity.User;
import app.exception.LockedException;
import app.exception.NotFoundException;
import app.repository.RoomRepository;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

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
        room.setStatus(Room.Status.WAITING);
        room.setCode(generateRoomCode());
        room.setHost(userService.findInAuth());
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

        if (room.getStatus() != Room.Status.WAITING) {
            throw new LockedException(messageHelper.get("room.locked"));
        }

        room.setStatus(Room.Status.STARTED);
        roomRepository.save(room);
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
                .anyMatch(foundUser -> foundUser.getId().equals(user.getId()))
                || user.getId().equals(room.getHost().getId());
        if (!alreadyJoined) {
            room.getCandidates().add(user);
            roomRepository.save(room);
        }
        return new RoomWaitingResponse(
                room.getExam().getTitle(),
                room.getExam().getAuthor().getUsername(),
                room.getHost().getEmail()
        );
    }

    public Room check(String code) {
        Room foundRoom = findByCode(code);
        if (foundRoom.getStatus() != Room.Status.WAITING) {
            throw new LockedException(messageHelper.get("room.locked"));
        }
        return foundRoom;
    }

    public void leaveRoom(String code) {
        Room room = findByCode(code);
        room.getCandidates().remove(userService.findInAuth());
        roomRepository.save(room);
    }
}
