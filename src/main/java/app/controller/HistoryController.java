package app.controller;

import app.dto.history.AddHistoryRequest;
import app.dto.history.HistoryResponse;
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
        return ResponseEntity.ok().body(historyService.submitAndEvaluate(request));
    }

    @GetMapping
    public ResponseEntity<List<HistoryResponse>> getHistory() {
        return ResponseEntity.ok(historyService.getHistoryByUser());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistoryResponse> getHistoryDetail(@PathVariable Long id) {
        return ResponseEntity.ok(historyService.getHistoryDetail(id));
    }
}