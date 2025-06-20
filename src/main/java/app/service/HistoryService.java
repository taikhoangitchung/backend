package app.service;

import app.dto.answer.AnswerResponse;
import app.dto.exam.SubmittedQuestion;
import app.dto.history.AddHistoryRequest;
import app.dto.history.HistoryResponse;
import app.dto.history.LastPlayedResponse;
import app.dto.history.QuestionDetailResponse;
import app.dto.question.QuestionResultResponse;
import app.entity.*;
import app.exception.NotFoundException;
import app.repository.*;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {
    private final HistoryRepository historyRepository;
    private final MessageHelper messageHelper;
    private final ExamRepository examRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final UserAnswerRepository userAnswerRepository;

    @Transactional
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
        List<QuestionResultResponse> questionResults = new ArrayList<>();
        List<UserAnswer> userAnswers = new ArrayList<>();

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

            UserAnswer ua = new UserAnswer();
            ua.setQuestion(question);
            ua.setSelectedAnswerIds(selectedAnswerIds.stream().map(Object::toString).collect(Collectors.joining(",")));
            ua.setCorrectAnswerIds(correctAnswerIds.stream().map(Object::toString).collect(Collectors.joining(",")));
            userAnswers.add(ua);
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
        history = historyRepository.save(history);

        History finalHistory = history;
        userAnswers.forEach(ua -> ua.setHistory(finalHistory));
        userAnswerRepository.saveAll(userAnswers);
        System.out.println("Saved UserAnswers for historyId " + history.getId() + ": " + userAnswers); // Debug

        return new LastPlayedResponse(correct, wrong, request.getTimeTaken(), score, questionResults);
    }

    public List<HistoryResponse> getHistoryByUser() {
        User foundUser = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElse(null);
        assert foundUser != null;
        List<History> histories = historyRepository.findByUserIdOrderByFinishedAtDesc(foundUser.getId());
        return histories.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public HistoryResponse getHistoryDetail( Long historyId) {
        User foundUser = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElse(null);
        assert foundUser != null;
        History history = historyRepository.findByIdAndUserId(historyId, foundUser.getId());
        if (history == null) {
            throw new IllegalArgumentException(messageHelper.get("history.not.found"));
        }
        return convertToResponse(history);
    }

    private HistoryResponse convertToResponse(History history) {
        HistoryResponse response = new HistoryResponse();
        response.setId(history.getId());
        response.setExamTitle(history.getExam().getTitle());
        response.setFinishedAt(history.getFinishedAt());
        response.setTimeTakenFormatted(formatTimeTaken(history.getTimeTaken()));
        response.setScorePercentage((float) history.getScore());
        response.setUsername(history.getUser().getUsername());
        response.setPassed(history.isPassed());

        List<History> allAttempts = historyRepository.findByExamIdAndUserId(history.getExam().getId(), history.getUser().getId());
        allAttempts.sort(Comparator.comparing(History::getFinishedAt));
        response.setAttemptNumber(allAttempts.indexOf(history) + 1);

        response.setQuestions(getQuestionDetails(history.getId()));
        return response;
    }

    private List<QuestionDetailResponse> getQuestionDetails(Long historyId) {
        List<UserAnswer> userAnswers = userAnswerRepository.findByHistoryId(historyId);
        System.out.println("UserAnswers for historyId " + historyId + ": " + userAnswers);
        if (userAnswers.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, QuestionDetailResponse> questionMap = new HashMap<>();
        for (UserAnswer ua : userAnswers) {
            Question question = ua.getQuestion();
            if (question == null) continue;
            QuestionDetailResponse qdr = questionMap.computeIfAbsent(question.getId(), k -> {
                QuestionDetailResponse newQdr = new QuestionDetailResponse();
                newQdr.setId(question.getId());
                newQdr.setContent(question.getContent());
                newQdr.setAnswers(new ArrayList<>());
                return newQdr;
            });

            if (question.getAnswers() != null) {
                for (Answer answer : question.getAnswers()) {
                    AnswerResponse answerResponse = new AnswerResponse(
                            answer.getId(),
                            answer.getContent(),
                            answer.getCorrect(),
                            answer.getColor()
                    );
                    qdr.getAnswers().add(answerResponse);
                }
            }

            String selectedAnswerIdsStr = ua.getSelectedAnswerIds();
            if (selectedAnswerIdsStr != null && !selectedAnswerIdsStr.isEmpty()) {
                List<Long> selectedAnswerIds = Arrays.stream(selectedAnswerIdsStr.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::valueOf)
                        .collect(Collectors.toList());
                qdr.setSelectedAnswerIds(selectedAnswerIds);
            }

            String correctAnswerIdsStr = ua.getCorrectAnswerIds();
            if (correctAnswerIdsStr != null && !correctAnswerIdsStr.isEmpty()) {
                List<Long> correctAnswerIds = Arrays.stream(correctAnswerIdsStr.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::valueOf)
                        .collect(Collectors.toList());
                qdr.getAnswers().forEach(answer -> {
                    answer.setCorrect(correctAnswerIds.contains(answer.getId()));
                });
            }
        }

        return new ArrayList<>(questionMap.values());
    }

    private String formatTimeTaken(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }
}