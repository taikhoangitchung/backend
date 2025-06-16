package app.service;

import app.dto.AddOrUpdateCategoryRequest;
import app.dto.CategoryResponse;
import app.entity.Category;
import app.exception.DuplicateException;
import app.exception.LockedException;
import app.exception.NotFoundException;
import app.repository.CategoryRepository;
import app.repository.QuestionRepository;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;
    private final MessageHelper messageHelper;

    public void addCategory(AddOrUpdateCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateException(messageHelper.get("category.exists"));
        }
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        categoryRepository.save(category);
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(item -> {
                    CategoryResponse response = new CategoryResponse();
                    response.setId(item.getId());
                    response.setName(item.getName());
                    response.setDescription(item.getDescription());
                    response.setQuestionCount(questionRepository.countByCategoryId(item.getId()));
                    return response;
                }).toList();
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException(messageHelper.get("category.not.found"));
        }
        if (questionRepository.existsByCategoryId(id)) {
            throw new LockedException(messageHelper.get("category.has.questions"));
        }
        categoryRepository.deleteById(id);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageHelper.get("category.not.found")));
    }

    public void update(Long id, AddOrUpdateCategoryRequest request) {
        Category category = getCategoryById(id);
        if (categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new DuplicateException(messageHelper.get("category.exists"));
        }
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        categoryRepository.save(category);
    }
}
