package pd1_stankevics_27;

import java.time.LocalDateTime;

/**
 * Testa rezultātu klase.
 * 
 * @author stankevics_27
 * @version 1.0
 */
public class Result {
    private String result_id;
    private String user_id;
    private String test_id;
    private int score;
    private double percentage;
    private String grade;
    private LocalDateTime date_completed;
    private String comment;

    /**
     * Izveido jaunu rezultāta objektu.
     */
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

    /**
     * Aprēķina procentuālo rezultātu.
     * 
     * @param totalQuestions kopējais jautājumu skaits
     */
    public void calculatePercentage(int totalQuestions) {
        percentage = (double) score / totalQuestions * 100;
    }

    /**
     * Atgriež rezultāta kopsavilkumu.
     */
    public String getSummary() {
        return "Rezultāts: " + grade + " (" + percentage + "%)";
    }
}