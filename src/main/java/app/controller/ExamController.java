package app.controller;

import app.dto.CreateExamRequest;
import app.dto.ExamCardResponse;
import app.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {
    private final ExamService examService;

    @PostMapping
    public ResponseEntity<?> createExam(@RequestBody CreateExamRequest request) {
        examService.createExam(request);
        return ResponseEntity.ok(HttpStatus.CREATED);
    }

    @GetMapping("/categories/{categoryId}/exams")
    public ResponseEntity<?> getExamsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(examService.getExamsByCategory(categoryId));
    }
}
