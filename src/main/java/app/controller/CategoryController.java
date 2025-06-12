package app.controller;

import app.dto.AddCategoryRequest;
import app.service.CategoryService;
import app.util.BindingHandler;
import app.util.MessageHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final MessageHelper messageHelper;

    @PostMapping
    public ResponseEntity<?> addCategory(@Valid @RequestBody AddCategoryRequest request,
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

//    @DeleteMapping("{id}")
//    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
//        if (id == null || id <= 0) {
//            return ResponseEntity.badRequest().body(messageHelper.get("category.not.found"));
//        }
//        categoryService.deleteCategory(id);
//        return ResponseEntity.ok().body(messageHelper.get("category.delete.success"));
//    }
}
