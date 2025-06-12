package app.service;

import app.entity.Category;
import app.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public void addCategory(String name) {
        if (categoryRepository.existsByName(name)){
            throw new IllegalArgumentException("Category already exists.");
        }
        Category category = new Category();
        category.setName(name);
        categoryRepository.save(category);
    }
}
