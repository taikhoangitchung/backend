package app.controller;

import app.dto.difficulty.AddDifficultyRequest;
import app.service.DifficultyService;
import app.util.BindingHandler;
import app.util.MessageHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/difficulties")
@RequiredArgsConstructor
public class DifficultyController {
    private final DifficultyService difficultyService;
    private final MessageHelper messageHelper;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> addDifficulty(@Valid @RequestBody AddDifficultyRequest request,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(BindingHandler.getErrorMessages(bindingResult));
        }
        difficultyService.addDifficulty(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(messageHelper.get("difficulty.create.success"));
    }
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok().body(difficultyService.getAllDifficulties());
    }
}
