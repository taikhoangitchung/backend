package app.controller;

import app.dto.history.*;
import app.entity.History;
import app.entity.Room;
import app.service.HistoryService;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/histories")
@RequiredArgsConstructor
public class HistoryController {
    private final HistoryService historyService;
    private final MessageHelper messageHelper;

    @PostMapping
    public ResponseEntity<?> addHistory(@RequestBody AddHistoryRequest request) {
        return ResponseEntity.ok().body(historyService.addHistory(request));
    }

    @GetMapping
    public ResponseEntity<Page<MyHistoryResponse>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(historyService.getAllMy(page, size));
    }

    @GetMapping("/room/{id}")
    public ResponseEntity<Room> getRoomByHistoryId(@PathVariable Long id) {
        return ResponseEntity.ok(historyService.getRoomByHistoryId(id));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<MyCreatedHistoryResponse>> getAllCreateByMe(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(historyService.getAllCreateByMe(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistoryDetailResponse> getHistoryDetail(@PathVariable Long id) {
        return ResponseEntity.ok(historyService.getDetailById(id));
    }

    @GetMapping("/exams/{examId}")
    public ResponseEntity<?> getExamSummary(@PathVariable Long examId) {
        return ResponseEntity.ok(historyService.getSummaryByExamId(examId));
    }

    @GetMapping("/{roomCode}/rank")
    public ResponseEntity<?> getRank(@PathVariable String roomCode) {
        return ResponseEntity.ok().body(historyService.getRoomRanking(roomCode));
    }
}