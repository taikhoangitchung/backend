package app.service;

import app.dto.AddCategoryRequest;
import app.dto.CategoryResponse;
import app.entity.Category;
import app.entity.Question;
import app.exception.DuplicateException;
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

    public void addCategory(AddCategoryRequest request) {
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

//    public void deleteCategory(Long id) {
//        if (!categoryRepository.existsById(id)) {
//            throw new NotFoundException(messageHelper.get("category.not.found"));
//        }
//        if ()
//        categoryRepository.deleteById(id);
//    }
}
