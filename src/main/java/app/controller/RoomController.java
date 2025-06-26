package app.controller;

import app.dto.room.CreateRoomRequest;
import app.service.RoomService;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;
    private final MessageHelper messageHelper;

    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody CreateRoomRequest request) {
        return ResponseEntity.ok().body(roomService.createRoom(request));
    }

    @PatchMapping("/{code}/join")
    public ResponseEntity<?> joinRoom(@PathVariable String code) {
        return ResponseEntity.ok().body(roomService.joinRoom(code));
    }

    @PatchMapping("/{code}/leave")
    public ResponseEntity<?> leaveRoom(@PathVariable String code) {
        roomService.leaveRoom(code);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{code}/check")
    public ResponseEntity<?> checkRoom(@PathVariable String code) {
        return ResponseEntity.ok().body(roomService.check(code));
    }

    @PatchMapping("/{code}/start")
    public ResponseEntity<?> startRoom(@PathVariable String code) {
        roomService.startRoom(code);
        return ResponseEntity.ok(messageHelper.get("room.started"));
    }
}
