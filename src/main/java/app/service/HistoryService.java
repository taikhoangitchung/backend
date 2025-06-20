package app.service;

import app.dto.answer.AnswerResponse;
import app.dto.exam.SubmittedQuestion;
import app.dto.history.AddHistoryRequest;
import app.dto.history.LastPlayedResponse;
import app.dto.question.QuestionResultResponse;
import app.entity.*;
import app.exception.NotFoundException;
import app.repository.ExamRepository;
import app.repository.HistoryRepository;
import app.repository.UserRepository;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public LastPlayedResponse addHistory(AddHistoryRequest request) {
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
        List<QuestionResultResponse> questionResults = new ArrayList<>();

        for (Question question : exam.getQuestions()) {
            List<AnswerResponse> answerResponses = new ArrayList<>();
            List<Long> correctAnswerIds = new ArrayList<>();
            List<Long> selectedAnswerIds = submittedMap.getOrDefault(question.getId(), List.of());

            for (Answer answer : question.getAnswers()) {
                answerResponses.add(new AnswerResponse(
                        answer.getId(),
                        answer.getContent(),
                        answer.getCorrect(),
                        answer.getColor()
                ));
                if (answer.getCorrect()) {
                    correctAnswerIds.add(answer.getId());
                }
            }

            boolean isCorrect;
            if ("multiple".equalsIgnoreCase(question.getType().getName())) {
                isCorrect = new HashSet<>(correctAnswerIds).equals(new HashSet<>(selectedAnswerIds));
            } else {
                isCorrect = selectedAnswerIds.size() == 1 && correctAnswerIds.contains(selectedAnswerIds.get(0));
            }

            if (isCorrect) correct++;
            else wrong++;

            QuestionResultResponse questionResult = new QuestionResultResponse(
                    question.getId(), question.getContent(), question.getType().getName(), answerResponses, selectedAnswerIds
            );
            questionResults.add(questionResult);
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
        exam.setPlayedTimes(exam.getPlayedTimes() + 1);
        historyRepository.save(history);

        return new LastPlayedResponse(correct, wrong, request.getTimeTaken(), score, questionResults);
    }
}
