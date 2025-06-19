package app.service;

import app.dto.exam.SubmittedQuestion;
import app.dto.history.AddHistoryRequest;
import app.entity.*;
import app.exception.NotFoundException;
import app.repository.ExamRepository;
import app.repository.HistoryRepository;
import app.repository.QuestionRepository;
import app.repository.UserRepository;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryService {
    private final HistoryRepository historyRepository;
    private final MessageHelper messageHelper;
    private final ExamRepository examRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;

    public void add(AddHistoryRequest request) {
        Exam foundExam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new NotFoundException(messageHelper.get("exam.not.found")));
        User foundUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));

        History history = new History();
        history.setExam(foundExam);
        history.setUser(foundUser);
        history.setTimeTaken(request.getTimeTaken());
        history.setFinishedAt(LocalDateTime.parse(request.getFinishedAt()));
        history.setScore(calculateScore(request.getQuestions()));
        historyRepository.save(history);
    }

    public int calculateScore(List<SubmittedQuestion> submittedQuestions) {
        int correctCount = 0;

        for (SubmittedQuestion item : submittedQuestions) {
            Question savedQuestion = questionRepository.findById(item.getId())
                    .orElseThrow(() -> new NotFoundException(messageHelper.get("question.not.found")));

            List<Long> correctAnswerIds = savedQuestion.getAnswers().stream()
                    .filter(Answer::getCorrect)
                    .map(Answer::getId)
                    .toList();

            if (isCorrectChoice(item, savedQuestion.getType().getName(), correctAnswerIds)) {
                correctCount++;
            }
        }
        return correctCount;
    }

    private static boolean isCorrectChoice(SubmittedQuestion submittedQuestion, String type, List<Long> correctAnswerIds) {
        List<Long> submittedAnswerIds = submittedQuestion.getAnswerIds() != null
                ? submittedQuestion.getAnswerIds()
                : List.of();

        boolean isCorrect;

        if ("multiple".equalsIgnoreCase(type)) {
            isCorrect = new HashSet<>(submittedAnswerIds).equals(new HashSet<>(correctAnswerIds));
        } else {
            isCorrect = !submittedAnswerIds.isEmpty() && correctAnswerIds.contains(submittedAnswerIds.get(0));
        }
        return isCorrect;
    }


}
