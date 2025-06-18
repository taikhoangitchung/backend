package app.service;

import app.dto.CreateExamRequest;
import app.entity.Exam;
import app.entity.Question;
import app.entity.User;
import app.repository.ExamRepository;
import app.repository.QuestionRepository;
import app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamService {
    private final ExamRepository examRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;

    public void createExam(CreateExamRequest request) {
        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Question> questions = questionRepository.findAllById(request.getQuestionIds());

        Exam exam = new Exam();
        exam.setTitle(request.getTitle());
        exam.setAuthor(author);
        exam.setQuestions(questions);
        examRepository.save(exam);
    }
}
