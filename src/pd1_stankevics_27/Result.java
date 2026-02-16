package pd1_stankevics_27;

import java.time.LocalDateTime;

public class Result {
    private String result_id;
    private String user_id;
    private String test_id;
    private int score;
    private double percentage;
    private String grade;
    private LocalDateTime date_completed;
    private String comment;

    public Result(String resultId, String userId, String testId, int score,
                  double percentage, String grade, LocalDateTime dateCompleted, String comment) {
        this.result_id = resultId;
        this.user_id = userId;
        this.test_id = testId;
        this.score = score;
        this.percentage = percentage;
        this.grade = grade;
        this.date_completed = dateCompleted;
        this.comment = comment;
    }

    public void calculatePercentage(int totalQuestions) {
        percentage = (double) score / totalQuestions * 100;
    }

    public void assignGrade() {
        if (percentage >= 90) grade = "10";
        else if (percentage >= 75) grade = "8";
        else if (percentage >= 50) grade = "6";
        else grade = "4";
    }

    public String getSummary() {
        return "Rezultāts: " + grade + " (" + percentage + "%)";
    }
}
