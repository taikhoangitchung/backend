package app.controller;

import app.dto.question.AddQuestionRequest;
import app.dto.question.EditQuestionRequest;
import app.dto.question.FilterQuestionRequest;
import app.service.QuestionService;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/questions")
public class QuestionController {
    private final QuestionService questionService;
    private final MessageHelper messageHelper;

    @GetMapping
    public ResponseEntity<?> getALl() {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.getAll());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAllByUser(@PathVariable long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.findByUserId(userId));
    }

    @PostMapping("/import")
    public ResponseEntity<?> importFromExcel(@RequestParam("file") MultipartFile file,
                                             @RequestParam("userId") long userId) {
        questionService.addAllQuestionFromExcel(file, userId);
        return ResponseEntity.ok(messageHelper.get("excel.import.success"));
    }

    @PostMapping("/filter")
    public ResponseEntity<?> filterByCategoryAndSource(@RequestBody FilterQuestionRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.findWithFilters(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestionById(@PathVariable long id) {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editQuestion(@ModelAttribute EditQuestionRequest request, @PathVariable long id, @RequestParam(required = false) MultipartFile image) {
        try {
            questionService.update(request, id, image);
            return ResponseEntity.status(HttpStatus.OK).body(messageHelper.get("update.success"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(messageHelper.get("upload.failed"));
        }
    }

    @PostMapping
    public ResponseEntity<?> createQuestion(@ModelAttribute AddQuestionRequest request, @RequestParam(required = false) MultipartFile image) {
        try {
            questionService.addQuestion(request, image);
            return ResponseEntity.status(HttpStatus.CREATED).body(messageHelper.get("question.create.success"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(messageHelper.get("upload.failed"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable long id) {
        questionService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(messageHelper.get("delete.success"));
    }
}