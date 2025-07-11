package app.controller;

import app.dto.question.*;
import app.entity.Question;
import app.service.QuestionService;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<?> getAll(Pageable pageable) { // Thêm Pageable
        Page<Question> questionPage = questionService.getAll(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(questionPage);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAllByUser(@PathVariable long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.findByUserId(userId));
    }

    @PostMapping("/import")
    public ResponseEntity<?> importFromExcel(@RequestBody AddQuestionFromExcel request) {
        questionService.addAllQuestionFromExcel(request);
        return ResponseEntity.ok(messageHelper.get("excel.import.success"));
    }

    @PostMapping("/filter")
    public ResponseEntity<?> filterByCategoryAndSource(@RequestBody FilterQuestionRequest request, Pageable pageable) { // Thêm Pageable
        Page<Question> questionPage = questionService.findWithFilters(request, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(questionPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestionById(@PathVariable long id) {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editQuestion(@ModelAttribute QuestionRequest request,
                                          @PathVariable long id) {
        try {
            questionService.updateQuestion(request, id);
            return ResponseEntity.status(HttpStatus.OK).body(messageHelper.get("update.success"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(messageHelper.get("upload.failed"));
        }
    }

    @GetMapping("/{id}/edit")
    public ResponseEntity<?> checkEditable(@PathVariable Long id) {
        questionService.ensureEditable(questionService.findById(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<?> createQuestion(
            @RequestBody QuestionRequest request) {

        try {
            questionService.storeQuestion(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(messageHelper.get("question.create.success"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable long id) {
        questionService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(messageHelper.get("delete.success"));
    }
}