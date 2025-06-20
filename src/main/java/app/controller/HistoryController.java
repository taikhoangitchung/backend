package app.controller;

import app.auth.CustomUserDetails;
import app.dto.history.AddHistoryRequest;
import app.dto.history.HistoryResponse;
import app.service.HistoryService;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

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
    public ResponseEntity<List<HistoryResponse>> getHistory(Authentication authentication) {
        Long userId = extractUserId(authentication);
        List<HistoryResponse> histories = historyService.getHistoryByUser(userId);
        return ResponseEntity.ok(histories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistoryResponse> getHistoryDetail(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = extractUserId(authentication);
        HistoryResponse historyDetail = historyService.getHistoryDetail(userId, id);
        return ResponseEntity.ok(historyDetail);
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getId();
        }
        throw new IllegalArgumentException(messageHelper.get("id.not.found"));
    }
}