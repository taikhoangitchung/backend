package app.service;

import app.dto.AddQuestionRequest;
import app.entity.*;
import app.exception.NotFoundException;
import app.repository.CategoryRepository;
import app.repository.DifficultyRepository;
import app.repository.QuestionRepository;
import app.repository.TypeRepository;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final TypeRepository typeRepository;
    private final DifficultyRepository difficultyRepository;
    private final MessageHelper messageHelper;

    @Transactional
    public void addQuestion(AddQuestionRequest request) {
        Question question = new Question();
        Category category =  categoryRepository.findById(request.getCategoryId()).
                orElseThrow(() -> new NotFoundException(messageHelper.get("category.notFound")));
        question.setCategory(category);
        Type type = typeRepository.findById(request.getTypeId()).
                orElseThrow(() -> new NotFoundException(messageHelper.get("type.notFound")));
        question.setType(type);
        Difficulty difficulty = difficultyRepository.findById(request.getDifficultyId()).
                orElseThrow(() -> new NotFoundException(messageHelper.get("difficulty.notFound")));
        question.setDifficulty(difficulty);

        List<Answer> answers = request.getAnswers();
        for (Answer answer : answers) {
            answer.setQuestion(question);
        }
        question.setAnswers(answers);

        questionRepository.save(question);
    }
}
