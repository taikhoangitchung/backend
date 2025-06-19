package app.dto;

public class ExamCardResponse {
    private Long id;
    private String title;
    private long playedTimes;
    private int questionCount;

    public ExamCardResponse(Long id, String title, long playedTimes, int questionCount) {
        this.id = id;
        this.title = title;
        this.playedTimes = playedTimes;
        this.questionCount = questionCount;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public long getPlayedTimes() { return playedTimes; }
    public int getQuestionCount() { return questionCount; }
} 