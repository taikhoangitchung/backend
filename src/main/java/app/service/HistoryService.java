package app.service;

import app.dto.exam.SubmittedQuestion;
import app.dto.history.AddHistoryRequest;
import app.dto.history.LastPlayedResponse;
import app.entity.*;
import app.exception.NotFoundException;
import app.repository.ExamRepository;
import app.repository.HistoryRepository;
import app.repository.UserRepository;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {
    private final HistoryRepository historyRepository;
    private final MessageHelper messageHelper;
    private final ExamRepository examRepository;
    private final UserRepository userRepository;

    public LastPlayedResponse submitAndEvaluate(AddHistoryRequest request) {
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new NotFoundException(messageHelper.get("exam.not.found")));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));

        Map<Long, List<Long>> submittedMap = request.getQuestions().stream()
                .collect(Collectors.toMap(
                        SubmittedQuestion::getId,
                        SubmittedQuestion::getAnswerIds
                ));

        long correct = 0;
        long wrong = 0;

        for (Question question : exam.getQuestions()) {
            List<Long> correctAnswerIds = question.getAnswers().stream()
                    .filter(Answer::getCorrect)
                    .map(Answer::getId)
                    .toList();

            List<Long> submittedAnswerIds = submittedMap.getOrDefault(question.getId(), List.of());

            boolean isCorrect;
            if ("multiple".equalsIgnoreCase(question.getType().getName())) {
                isCorrect = new HashSet<>(correctAnswerIds).equals(new HashSet<>(submittedAnswerIds));
            } else {
                isCorrect = submittedAnswerIds.size() == 1 && correctAnswerIds.contains(submittedAnswerIds.get(0));
            }

            if (isCorrect) correct++;
            else wrong++;
        }

        long score = Math.round(((double) correct / exam.getQuestions().size()) * 100);
        boolean passed = score >= exam.getPassScore();

        History history = new History();
        history.setUser(user);
        history.setExam(exam);
        history.setTimeTaken(request.getTimeTaken());
        history.setScore(score);
        history.setPassed(passed);
        history.setFinishedAt(LocalDateTime.parse(request.getFinishedAt()));
        historyRepository.save(history);

        return new LastPlayedResponse(correct, wrong, request.getTimeTaken(), score);
    }
}
