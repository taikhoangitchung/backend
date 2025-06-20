package app.controller;

import app.auth.CustomUserDetails;
import app.dto.history.HistoryResponse;
import app.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/histories")
@RequiredArgsConstructor
public class HistoryController {
    private final HistoryService historyService;

    @GetMapping
    public ResponseEntity<Page<HistoryResponse>> getHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = extractUserId(authentication);
        Page<HistoryResponse> historyPage = historyService.getHistoryByUser(userId, page, size);
        return ResponseEntity.ok(historyPage);
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
        throw new IllegalArgumentException("Không thể xác định ID người dùng.");
    }
}