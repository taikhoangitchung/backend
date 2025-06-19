package app.controller;

import app.dto.HistoryDTO;
import app.entity.History;
import app.entity.User;
import app.repository.HistoryRepository;
import app.repository.UserRepository;
import app.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {
    private final ExamService examService;
    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;

    @GetMapping("/history/user/{userId}")
    public ResponseEntity<Page<HistoryDTO>> getUserHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String email = principal.getName();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Page<HistoryDTO> historyPage = examService.getUserHistory(user, PageRequest.of(page, size));
        return ResponseEntity.ok(historyPage);
    }

    @GetMapping("/history/{historyId}/user/{userId}")
    public ResponseEntity<?> getExamHistoryDetail(@PathVariable Long historyId, @PathVariable Long userId) {
        try {
            History history = examService.getExamHistoryDetail(historyId, userId);
            HistoryDTO dto = new HistoryDTO();
            dto.setId(history.getId());
            dto.setExamName(history.getExam().getTitle());
            dto.setCompletedAt(history.getCompletedAt());
            dto.setTimeTaken(history.getTimeTaken());
            dto.setScore(history.getScore());
            dto.setAttempts(historyRepository.countAttemptsByUserIdAndExamId(userId, history.getExam().getId()));
            dto.setPassed(history.isPassed());
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("History not found for user " + userId);
        }
    }
}