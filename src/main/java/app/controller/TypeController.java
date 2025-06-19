package app.controller;

import app.dto.type.AddTypeRequest;
import app.service.TypeService;
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
@RequiredArgsConstructor
@RequestMapping("/types")
public class TypeController {
    private final TypeService typeService;
    private final MessageHelper messageHelper;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> addType(@Valid @RequestBody AddTypeRequest request,
                                     BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(BindingHandler.getErrorMessages(bindingResult));
        }
        typeService.addType(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(messageHelper.get("type.create.success"));
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok().body(typeService.getAllTypes());
    }
}
