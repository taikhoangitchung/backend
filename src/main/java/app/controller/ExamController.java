package app.controller;

import app.dto.exam.CreateExamRequest;
import app.service.ExamService;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {
    private final ExamService examService;
    private final MessageHelper messageHelper;

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

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateExamRequest request) {
        examService.createExam(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(messageHelper.get("exam.create.success"));
    }

    @GetMapping("/is-exists/{title}")
    public ResponseEntity<?> existsByName(@PathVariable String title) {
        return ResponseEntity.status(HttpStatus.OK).body(examService.existExam(title));
    }
}
