package app.service;

import app.dto.AddQuestionRequest;
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
    public List<Question> findByUserId(long userId) {
        return questionRepository.findAllByUserId(userId);
    }
}
