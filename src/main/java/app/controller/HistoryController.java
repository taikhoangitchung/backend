package app.controller;

import app.dto.history.AddHistoryRequest;
import app.dto.history.ExamSummaryHistoryResponse;
import app.dto.history.HistorySummaryResponse;
import app.dto.history.HistoryDetailResponse;
import app.service.HistoryService;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<List<HistorySummaryResponse>> getHistory() {
        return ResponseEntity.ok(historyService.getAllSummary());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistoryDetailResponse> getHistoryDetail(@PathVariable Long id) {
        return ResponseEntity.ok(historyService.getDetailById(id));
    }

    @GetMapping("/exams/{examId}")
    public ResponseEntity<?> getExamSummary(@PathVariable Long examId) {
        return ResponseEntity.ok(historyService.getSummaryByExamId(examId));
    }
}