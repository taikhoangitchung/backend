package app.controller;

import app.service.DifficultyService;
import app.util.BindingHandler;
import app.util.MessageHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/difficulties")
@RequiredArgsConstructor
public class DifficultyController {
    private final DifficultyService difficultyService;
    private final MessageHelper messageHelper;

    @PostMapping
    public ResponseEntity<?> addDifficulty(@Valid @RequestBody String difficultyName,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(BindingHandler.getErrorMessages(bindingResult));
        }
        difficultyService.addDifficulty(difficultyName);
        return ResponseEntity.status(HttpStatus.CREATED).body(messageHelper.get("difficulty.create.success"));
    }
}
