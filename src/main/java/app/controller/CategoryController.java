package app.controller;

import app.dto.category.AddOrUpdateCategoryRequest;
import app.service.CategoryService;
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
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final MessageHelper messageHelper;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> addCategory(@Valid @RequestBody AddOrUpdateCategoryRequest request,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(BindingHandler.getErrorMessages(bindingResult));
        }
        categoryService.addCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(messageHelper.get("category.create.success"));
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok().body(categoryService.getAllCategories());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok().body(categoryService.getCategoryById(id));
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateCategory(@Valid @RequestBody AddOrUpdateCategoryRequest request,
                                            BindingResult bindingResult, @PathVariable Long id) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(BindingHandler.getErrorMessages(bindingResult));
        }
        categoryService.update(id, request);
        return ResponseEntity.ok().body(messageHelper.get("update.success"));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().body(messageHelper.get("delete.success"));
    }
}
