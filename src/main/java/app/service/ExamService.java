package app.service;

import app.dto.exam.CreateExamRequest;
import app.dto.exam.ExamSummaryResponse;
import app.dto.exam.PlayExamResponse;
import app.entity.*;
import app.exception.NotFoundException;
import app.repository.*;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamService {
    private final ExamRepository examRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final UserService userService;
    private final MessageHelper messageHelper;
    private final DifficultyRepository difficultyRepository;
    private final CategoryRepository categoryRepository;

    public void createExam(CreateExamRequest request) {
        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));
        Difficulty difficulty = difficultyRepository.findById(request.getDifficultyId())
                .orElseThrow(() -> new NotFoundException(messageHelper.get("difficulty.not.found")));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException(messageHelper.get("category.not.found")));

        System.err.println(request.getQuestionIds());
        List<Question> questions = questionRepository.findAllById(request.getQuestionIds());

        Exam exam = new Exam();
        exam.setTitle(request.getTitle());
        exam.setAuthor(author);
        exam.setDifficulty(difficulty);
        exam.setCategory(category);
        exam.setQuestions(questions);
        exam.setDuration(request.getDuration());
        exam.setPassScore(request.getPassScore());
        exam.setPlayedTimes(0);
        examRepository.save(exam);
    }

    public List<Exam> getAll() {
        return examRepository.findAll();
    }

    public PlayExamResponse getToPlayById(Long id) {
        Exam exam = findById(id);
        PlayExamResponse response = new PlayExamResponse();
        response.setDuration(exam.getDuration());
        response.setQuestions(exam.getQuestions());
        return response;
    }

    public List<ExamSummaryResponse> getExamsByCategory(Long categoryId) {
        List<Exam> exams = examRepository.findByCategoryId(categoryId);
        return exams.stream().map(exam ->
                new ExamSummaryResponse(
                        exam.getId(),
                        exam.getTitle(),
                        exam.getQuestions() != null ? exam.getQuestions().size() : 0,
                        exam.getDifficulty(),
                        exam.getPlayedTimes()
                )
        ).toList();
    }

    public boolean existExam(String title) {
        return examRepository.existsByTitle(title);
    }

    public Exam findById(Long id){
        return examRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageHelper.get("exam.not.found")));
    }
}
