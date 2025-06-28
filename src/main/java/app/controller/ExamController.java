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

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(examService.findById(id));
    }

    @GetMapping("/{id}/play")
    public ResponseEntity<?> getToPlayById(@PathVariable Long id) {
        return ResponseEntity.ok(examService.getToPlayById(id));
    }

    @GetMapping("/rooms/{code}/play")
    public ResponseEntity<?> getToPlayByRoom(@PathVariable String code) {
        return ResponseEntity.ok(examService.getToPlayByRoom(code));
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<?> getExamsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(examService.getExamsByCategory(categoryId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> update(@RequestBody CreateExamRequest request, @PathVariable long id) {
        examService.createOrUpdateExam(request, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(messageHelper.get("exam.update.success"));
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(examService.getAll());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateExamRequest request) {
        examService.createOrUpdateExam(request, -1);
        return ResponseEntity.status(HttpStatus.CREATED).body(messageHelper.get("exam.create.success"));
    }

    @GetMapping("/is-exists/{title}")
    public ResponseEntity<?> existsByName(@PathVariable String title) {
        return ResponseEntity.status(HttpStatus.OK).body(examService.existExam(title));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        examService.delete(id);
        return ResponseEntity.ok().body(messageHelper.get("delete.success"));
    }

}
