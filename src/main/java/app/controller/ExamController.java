package app.controller;

import app.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {
    private final ExamService examService;

    @GetMapping("/{id}/play")
    public ResponseEntity<?> getToPlayById(@PathVariable Long id) {
        return ResponseEntity.ok(examService.getToPlayById(id));
    }

    @GetMapping("/categories/{categoryId}/exams")
    public ResponseEntity<?> getExamsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(examService.getExamsByCategory(categoryId));
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(examService.getAll());
    }
}
