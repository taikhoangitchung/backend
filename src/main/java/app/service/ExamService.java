package app.service;

import app.dto.exam.ExamCardResponse;
import app.dto.exam.PlayExamResponse;
import app.entity.Exam;
import app.exception.NotFoundException;
import app.repository.ExamRepository;
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

    public List<ExamCardResponse> getExamsByCategory(Long categoryId) {
        List<Exam> exams = examRepository.findByCategoryId(categoryId);
        return exams.stream().map(exam ->
                new ExamCardResponse(
                        exam.getId(),
                        exam.getTitle(),
                        exam.getPlayedTimes(),
                        exam.getQuestions() != null ? exam.getQuestions().size() : 0
                )
        ).toList();
    }
}