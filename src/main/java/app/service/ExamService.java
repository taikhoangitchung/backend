package app.service;

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
    private final MessageHelper messageHelper;

    public PlayExamResponse getToPlayById(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(messageHelper.get("exam.not.found")));

        PlayExamResponse response = new PlayExamResponse();
        response.setDuration(exam.getDuration());
        response.setQuestions(exam.getQuestions());
        return response;
    }
}
