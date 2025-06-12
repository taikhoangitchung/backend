package app.controller;

import app.dto.AddQuestionRequest;
import app.service.QuestionService;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/questions")
public class QuestionController {
    private final QuestionService questionService;
    private final MessageHelper messageHelper;

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAllQuestions(@PathVariable long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(questionService.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<?> createQuestion(@RequestBody AddQuestionRequest request ) {
        questionService.addQuestion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(messageHelper.get("question.create.success"));
    }
//
}
