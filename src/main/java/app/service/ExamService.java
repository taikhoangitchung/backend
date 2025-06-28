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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamService {
    private final ExamRepository examRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final RoomRepository roomRepository;
    private final MessageHelper messageHelper;
    private final DifficultyRepository difficultyRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public void createOrUpdateExam(CreateExamRequest request, long examId) {
        Exam exam = null;

        if (examId != -1) {
            exam = findById(examId);
            exam.getQuestions().clear();
        }

        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));
        Difficulty difficulty = difficultyRepository.findById(request.getDifficultyId())
                .orElseThrow(() -> new NotFoundException(messageHelper.get("difficulty.not.found")));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException(messageHelper.get("category.not.found")));

        List<Question> questions = questionRepository.findAllById(request.getQuestionIds());

        exam = (exam != null) ? exam : new Exam();
        exam.setTitle(request.getTitle());
        exam.setAuthor(author);
        exam.setDifficulty(difficulty);
        exam.setCategory(category);
        exam.setQuestions(questions);
        exam.setDuration(request.getDuration());
        exam.setPassScore(request.getPassScore());
        exam.setPlayedTimes(0);
        exam.setPublic(request.getIsPublic());
        examRepository.save(exam);
    }

    public List<Exam> getAll() {
        return examRepository.findAllByOrderByIdDesc();
    }

    public PlayExamResponse getToPlayById(Long id) {
        Exam exam = findById(id);
        return new PlayExamResponse(
                exam.getDuration(),
                exam.getQuestions(),
                null,
                null
        );
    }

    public List<ExamSummaryResponse> getExamsByCategory(Long categoryId) {
        List<Exam> exams = examRepository.findByCategoryId(categoryId);
        return exams.stream()
                .map(exam -> new ExamSummaryResponse(
                        exam.getId(),
                        exam.getTitle(),
                        exam.getQuestions() != null ? exam.getQuestions().size() : 0,
                        exam.getDifficulty(),
                        exam.getPlayedTimes(),
                        exam.isPublic(),
                        exam.getAuthor().getId()
                ))
                .toList();
    }

    public boolean existExam(String title) {
        return examRepository.existsByTitle(title);
    }

    public Exam findById(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageHelper.get("exam.not.found")));
    }

    public PlayExamResponse getToPlayByRoom(String code) {
        Room foundRoom = roomRepository.findByCode(code);
        Exam exam = foundRoom.getExam();
        return new PlayExamResponse(
                exam.getDuration(),
                exam.getQuestions(),
                foundRoom.getCandidates(),
                foundRoom.getHost().getEmail()
        );
    }

    public void delete(Long id) {
        examRepository.deleteById(id);
    }
}
