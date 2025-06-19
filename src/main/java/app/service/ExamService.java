package app.service;

import app.dto.exam.ExamCardResponse;
import app.dto.exam.CreateExamRequest;
import app.dto.ExamCardResponse;
import app.dto.exam.PlayExamResponse;
import app.entity.Exam;
import app.entity.Question;
import app.entity.User;
import app.exception.NotFoundException;
import app.repository.ExamRepository;
import app.repository.QuestionRepository;
import app.repository.UserRepository;
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
    private final MessageHelper messageHelper;

    public void createExam(CreateExamRequest request) {
        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Question> questions = questionRepository.findAllById(request.getQuestionIds());

        Exam exam = new Exam();
        exam.setTitle(request.getTitle());
        exam.setAuthor(author);
        exam.setDuration(request.getDuration());
        exam.setQuestions(questions);
        exam.setDifficulty(request.getDifficulty());
        exam.setPassScore(request.getPassScore());
        exam.setPlayedTimes(0);
        examRepository.save(exam);
    }

    public PlayExamResponse getToPlayById(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageHelper.get("exam.not.found")));

        PlayExamResponse response = new PlayExamResponse();
        response.setDuration(exam.getDuration());
        response.setQuestions(exam.getQuestions());
        return response;
    }

    public List<ExamCardResponse> getExamsByCategory(Long categoryId) {
        List<Exam> exams = examRepository.findByCategoryId(categoryId);
        return exams.stream().map(exam ->
            new ExamCardResponse(
                exam.getId(),
                exam.getTitle(),
                exam.getPlayedTimes(),
                exam.getQuestions() != null ? exam.getQuestions().size() : 0
                , exam.getDifficulty()

            )
        ).toList();
    }
}
