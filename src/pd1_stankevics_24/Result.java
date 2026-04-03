package pd1_stankevics_24;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Testa rezultātu klase, kas satur informāciju par studenta veikumu konkrētā testā.
 * 
 * @author armins.stankevics_24
 */
public class Result {
    private String resultId;
    private String userId;
    private String testId;
    private int score;
    private int maxScore;
    private double percentage;
    private String grade;
    private LocalDateTime dateCompleted;
    private String comment;
    private String studentName;
    private String testName;
    private String studentGroup;

    /**
     * Konstruktors rezultāta izveidei ar pamatinformāciju.
     *
     * @param resultId      rezultāta unikālais identifikators
     * @param userId        lietotāja ID, kuram pieder rezultāts
     * @param testId        testa ID, par kuru ir rezultāts
     * @param score         iegūto punktu skaits
     * @param maxScore      maksimāli iegūstamais punktu skaits
     * @param grade         atzīme (skaitliski vai burtiski)
     * @param dateCompleted testa izpildes datums un laiks
     */
    public Result(String resultId, String userId, String testId, int score, int maxScore, String grade, LocalDateTime dateCompleted) {
        this.resultId = resultId;
        this.userId = userId;
        this.testId = testId;
        this.score = score;
        this.maxScore = maxScore;
        this.grade = grade;
        this.dateCompleted = (dateCompleted != null) ? dateCompleted : LocalDateTime.now();
        this.comment = "";
        this.studentName = "";
        this.testName = "";
        this.studentGroup = "";
        calculatePercentage();
    }

    /**
     * Konstruktors rezultāta izveidei ar visu informāciju, ieskaitot komentāru un lietotāja datus.
     *
     * @param resultId      rezultāta unikālais identifikators
     * @param userId        lietotāja ID, kuram pieder rezultāts
     * @param testId        testa ID, par kuru ir rezultāts
     * @param score         iegūto punktu skaits
     * @param maxScore      maksimāli iegūstamais punktu skaits
     * @param grade         atzīme (skaitliski vai burtiski)
     * @param dateCompleted testa izpildes datums un laiks
     * @param comment       papildu komentārs par rezultātu
     * @param studentName   studenta vārds un uzvārds
     * @param testName      testa nosaukums
     * @param studentGroup  studenta grupa
     */
    public Result(String resultId, String userId, String testId, int score, int maxScore, String grade, LocalDateTime dateCompleted, String comment, String studentName, String testName, String studentGroup) {
        this(resultId, userId, testId, score, maxScore, grade, dateCompleted);
        setComment(comment);
        setStudentName(studentName);
        setTestName(testName);
        setStudentGroup(studentGroup);
    }

    /**
     * Iestata rezultāta komentāru.
     *
     * @param comment komentāra teksts
     */
    public final void setComment(String comment) { this.comment = (comment != null) ? comment : ""; }

    /**
     * Iestata studenta vārdu un uzvārdu.
     *
     * @param studentName studenta vārds un uzvārds
     */
    public final void setStudentName(String studentName) { this.studentName = (studentName != null) ? studentName : ""; }

    /**
     * Iestata testa nosaukumu.
     *
     * @param testName testa nosaukums
     */
    public final void setTestName(String testName) { this.testName = (testName != null) ? testName : ""; }

    /**
     * Iestata studenta grupu.
     *
     * @param studentGroup studenta grupa
     */
    public final void setStudentGroup(String studentGroup) { this.studentGroup = (studentGroup != null) ? studentGroup : ""; }

    /**
     * Aprēķina procentuālo rezultātu.
     */
    private void calculatePercentage() { this.percentage = (maxScore > 0) ? (double) score / maxScore * 100 : 0; }

    /**
     * Pārbauda, vai tests ir nokārtots (nokārtošanas slieksnis ir 50%).
     *
     * @return true, ja procentuālais rezultāts ir vismaz 50%
     */
    public boolean isPassed() { return percentage >= 50; }

    /**
     * Pārbauda, vai tests ir nokārtots ar norādīto nokārtošanas slieksni.
     *
     * @param passingThreshold nokārtošanas slieksnis procentos
     * @return true, ja procentuālais rezultāts ir vismaz passingThreshold
     */
    public boolean isPassed(int passingThreshold) { return percentage >= passingThreshold; }

    /**
     * Atgriež rezultāta kopsavilkumu formatētā veidā.
     *
     * @return formatēts rezultāta kopsavilkums
     */
    public String getSummary() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        String status = isPassed() ? "✓ NOKĀRTOŠANA" : "✗ NAV NOKĀRTOŠANA";
        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════╗\n");
        sb.append(String.format("║ Rezultāts: %3d / %-3d (%5.1f%%) %8s ║\n", score, maxScore, percentage, status));
        sb.append(String.format("║ Atzīme: %-44s ║\n", grade));
        sb.append(String.format("║ Datums: %-43s ║\n", dateCompleted.format(formatter)));
        sb.append("╚════════════════════════════════════════════════════╝");
        return sb.toString();
    }

    /**
     * Atgriež detalizētu rezultāta informāciju formatētā veidā.
     *
     * @return detalizēta rezultāta informācija
     */
    public String getDetailedInfo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════\n");
        sb.append("              DETALIZĒTS TESTA REZULTĀTS\n");
        sb.append("═══════════════════════════════════════════════════════════\n\n");
        sb.append(String.format("Iegūtie punkti:  %d\n", score));
        sb.append(String.format("Maksimālie:      %d\n", maxScore));
        sb.append(String.format("Procenti:        %.1f%%\n", percentage));
        sb.append(String.format("Atzīme:          %s\n", grade));
        sb.append(String.format("Datums:          %s\n", dateCompleted.format(formatter)));
        return sb.toString();
    }

    /**
     * Eksportē rezultātu CSV formātā.
     *
     * @return CSV formātā formatēts rezultāts
     */
    public String toCSV() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("%s,%s,%s,%d,%d,%.1f,%s,%s", resultId, userId, testId, score, maxScore, percentage, grade, dateCompleted.format(formatter));
    }

    // Getter metodes
    public String getResultId() { return resultId; }
    public String getUserId() { return userId; }
    public String getTestId() { return testId; }
    public int getScore() { return score; }
    public int getMaxScore() { return maxScore; }
    public double getPercentage() { return percentage; }
    public String getGrade() { return grade; }
    public LocalDateTime getDateCompleted() { return dateCompleted; }
    public String getComment() { return comment; }
    public String getStudentName() { return studentName; }
    public String getTestName() { return testName; }
    public String getStudentGroup() { return studentGroup; }
    
    @Override
    public String toString() { return String.format("Result[%s: %d/%d (%.1f%%) %s]", testId, score, maxScore, percentage, grade); }
}