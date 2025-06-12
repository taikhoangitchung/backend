package app.service;

import app.dto.AddQuestionRequest;
import app.dto.EditQuestionRequest;
import app.entity.*;
import app.repository.*;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TypeRepository typeRepository;
    private final DifficultyRepository difficultyRepository;
    private final MessageHelper messageHelper;

    @Transactional
    public void addQuestion(AddQuestionRequest request) {
        Question question = new Question();
        question.setUser(userRepository.findByUsername("admin"));
        question.setCategory(categoryRepository.findByName(request.getCategory()));
        question.setType(typeRepository.findByName(request.getType()));
        question.setDifficulty(difficultyRepository.findByName(request.getDifficulty()));
        question.setContent(request.getContent());
        questionRepository.save(question);
    }

    @Transactional
    public Optional<Question> findByUserId(long userId) {
        return questionRepository.findById(userId);
    }

    public Question findById(long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(messageHelper.get("question.notFound")));
    }


    public void update(EditQuestionRequest request, long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(messageHelper.get("question.notFound")));
        Category category = categoryRepository.findByName(request.getCategory());
        Type type = typeRepository.findByName(request.getType());
        Difficulty difficulty = difficultyRepository.findByName(request.getDifficulty());

        if (category == null || type == null || difficulty == null) {
            throw new RuntimeException(messageHelper.get("question.update.error"));
        }

        question.setCategory(category);
        question.setType(type);
        question.setDifficulty(difficulty);
        question.setContent(request.getContent());

        questionRepository.save(question);
    }
}
