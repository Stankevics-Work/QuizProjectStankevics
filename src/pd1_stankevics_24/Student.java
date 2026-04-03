package pd1_stankevics_24;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Studentu lietotāja klase, kas paplašina {@link User} funkcionalitāti ar
 * testu pildīšanas iespējām.
 * 
 * @author armins.stankevics_24
 */
public class Student extends User {

    private String studentId;
    private String group;
    private int course;
    private String studentNumber;
    private List<Result> testResults;
    private Map<String, List<Integer>> testAnswers;
    private Test currentTest;
    private int currentQuestionIndex;
    private int rightAnswers;
    private int questionsCount;
    private LocalDateTime testStartTime;

    /**
     * Konstruktors studenta izveidei ar pamatinformāciju.
     *
     * @param name     studenta vārds un uzvārds
     * @param login    pieteikšanās lietotājvārds
     * @param password pieteikšanās parole
     */
    public Student(String name, String login, String password) {
        super(name, login, password);
        this.studentId = generateStudentId();
        this.group = "IT-21";
        this.course = 1;
        this.studentNumber = "S" + System.currentTimeMillis() % 10000;
        this.testResults = new ArrayList<>();
        this.testAnswers = new HashMap<>();
        this.currentQuestionIndex = 0;
        this.rightAnswers = 0;
        this.questionsCount = 0;
    }

    /**
     * Konstruktors studenta izveidei ar visu informāciju.
     *
     * @param name     studenta vārds un uzvārds
     * @param login    pieteikšanās lietotājvārds
     * @param password pieteikšanās parole
     * @param email    studenta e-pasta adrese
     * @param group    studenta grupa
     * @param course   studenta kurss (1-4)
     */
    public Student(String name, String login, String password, String email, String group, int course) {
        super(name, login, password, email);
        this.studentId = generateStudentId();
        setGroup(group);
        setCourse(course);
        this.studentNumber = "S" + System.currentTimeMillis() % 10000;
        this.testResults = new ArrayList<>();
        this.testAnswers = new HashMap<>();
        this.currentQuestionIndex = 0;
        this.rightAnswers = 0;
        this.questionsCount = 0;
    }

    /**
     * Ģenerē unikālu studenta identifikatoru.
     *
     * @return unikāls studenta ID
     */
    private String generateStudentId() {
        return "STU" + System.currentTimeMillis() + (int)(Math.random() * 100);
    }

    /**
     * Iestata studenta grupu.
     *
     * @param group studenta grupa
     */
    public final void setGroup(String group) {
        this.group = (group != null && !group.trim().isEmpty()) ? group.trim() : "IT-21";
    }

    /**
     * Iestata studenta kursu.
     *
     * @param course studenta kurss (1-4)
     * @throws IllegalArgumentException ja kurss nav diapazonā 1-4
     */
    public final void setCourse(int course) {
        if (course < 1 || course > 4) {
            throw new IllegalArgumentException("Kursam jābūt 1-4!");
        }
        this.course = course;
    }

    /**
     * Uzsāk testa pildīšanu.
     *
     * @param test pildāmais tests
     * @throws IllegalArgumentException ja tests ir null
     * @throws IllegalStateException    ja tests nav pieejams izpildei
     */
    public void startTest(Test test) {
        if (test == null) {
            throw new IllegalArgumentException("Tests nevar būt null!");
        }
        if (!test.isAvailable()) {
            throw new IllegalStateException("Tests '" + test.getTitle() + "' nav pieejams izpildei!");
        }
        this.currentTest = test;
        this.currentQuestionIndex = 0;
        this.questionsCount = 0;
        this.rightAnswers = 0;
        this.testStartTime = LocalDateTime.now();
        testAnswers.put(test.getTestId(), new ArrayList<>());
    }

    /**
     * Atbild uz pašreizējo testa jautājumu, izmantojot atbildes indeksu.
     *
     * @param selectedIndex izvēlētā atbildes indekss (0=A, 1=B, 2=C)
     * @return true, ja tests nav pabeigts un ir nākamais jautājums
     * @throws IllegalStateException    ja tests nav uzsākts
     * @throws IllegalArgumentException ja indekss nav diapazonā 0-2
     */
    public boolean answerQuestion(int selectedIndex) {
        if (currentTest == null) {
            throw new IllegalStateException("Vispirms jāuzsāk tests!");
        }
        if (currentQuestionIndex >= currentTest.getQuestions().size()) {
            return false;
        }
        if (selectedIndex < 0 || selectedIndex > 2) {
            throw new IllegalArgumentException("Atbildes indeksam jābūt 0-2!");
        }
        Question q = currentTest.getQuestions().get(currentQuestionIndex);
        testAnswers.get(currentTest.getTestId()).add(selectedIndex);
        questionsCount++;
        if (q.isCorrect(selectedIndex)) {
            rightAnswers++;
        }
        currentQuestionIndex++;
        return currentQuestionIndex < currentTest.getQuestions().size();
    }

    /**
     * Atbild uz pašreizējo testa jautājumu, izmantojot burtu.
     *
     * @param letter izvēlētais burts (A, B vai C)
     * @return true, ja tests nav pabeigts un ir nākamais jautājums
     * @throws IllegalArgumentException ja burts nav A, B vai C
     */
    public boolean answerQuestion(char letter) {
        int index = -1;
        if (letter == 'A' || letter == 'a') index = 0;
        else if (letter == 'B' || letter == 'b') index = 1;
        else if (letter == 'C' || letter == 'c') index = 2;
        else throw new IllegalArgumentException("Atbildei jābūt A, B vai C!");
        return answerQuestion(index);
    }

    /**
     * Pabeidz testu un aprēķina rezultātu.
     *
     * @return testa rezultātu objekts
     * @throws IllegalStateException ja tests nav uzsākts vai nav pabeigts
     */
    public Result finishTest() {
        if (currentTest == null) {
            throw new IllegalStateException("Nav uzsākta testa!");
        }
        if (currentQuestionIndex < currentTest.getQuestions().size()) {
            throw new IllegalStateException("Tests nav pabeigts! Atlikuši " + 
                (currentTest.getQuestions().size() - currentQuestionIndex) + " jautājumi.");
        }
        List<Integer> answers = testAnswers.get(currentTest.getTestId());
        int totalPoints = 0;
        int earnedPoints = 0;
        for (int i = 0; i < currentTest.getQuestions().size(); i++) {
            Question q = currentTest.getQuestions().get(i);
            totalPoints += q.getPoints();
            if (answers.size() > i && q.isCorrect(answers.get(i))) {
                earnedPoints += q.getPoints();
            }
        }
        int percent = (int) Math.round((earnedPoints * 100.0) / totalPoints);
        int mark = currentTest.calculateMark(percent);
        Result result = new Result(
            generateResultId(),
            this.getUserId(),
            currentTest.getTestId(),
            earnedPoints,
            totalPoints,
            String.valueOf(mark),
            LocalDateTime.now(),
            currentTest.getGradeDescription(mark),
            this.getName(),
            currentTest.getTitle(),
            this.group
        );
        testResults.add(result);
        return result;
    }

    /**
     * Ģenerē unikālu rezultāta identifikatoru.
     *
     * @return unikāls rezultāta ID
     */
    private String generateResultId() {
        return "R" + System.currentTimeMillis();
    }

    /**
     * Saglabā pēdējo testa rezultātu datubāzē.
     *
     * @param connection aktīvs savienojums ar datubāzi
     * @return true, ja rezultāts veiksmīgi saglabāts
     */
    public boolean saveTestResultToDatabase(Connection connection) {
        if (connection == null || testResults.isEmpty()) {
            return false;
        }
        Result lastResult = testResults.get(testResults.size() - 1);
        String sql = "INSERT INTO test_results (user_id, test_id, score, max_score, percentage, grade, time_spent_seconds) " +
                     "VALUES ((SELECT id FROM users WHERE username = ?), ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, this.getLogin());
            pstmt.setString(2, lastResult.getTestId());
            pstmt.setInt(3, lastResult.getScore());
            pstmt.setInt(4, lastResult.getMaxScore());
            pstmt.setDouble(5, lastResult.getPercentage());
            pstmt.setString(6, lastResult.getGrade());
            pstmt.setInt(7, 0);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("✅ Rezultāts saglabāts datubāzē, rindas: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("❌ Kļūda saglabājot rezultātu: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Ielādē studenta testu rezultātus no datubāzes.
     *
     * @param connection aktīvs savienojums ar datubāzi
     * @return ielādēto rezultātu saraksts
     */
    public List<Result> loadResultsFromDatabase(Connection connection) {
        List<Result> results = new ArrayList<>();
        if (connection == null) return results;
        String sql = "SELECT tr.id, tr.test_id, tr.score, tr.max_score, tr.percentage, tr.grade, " +
                     "tr.completed_at FROM test_results tr " +
                     "WHERE tr.user_id = (SELECT id FROM users WHERE username = ?) " +
                     "ORDER BY tr.completed_at DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, this.getLogin());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Result result = new Result(
                        String.valueOf(rs.getInt("id")),
                        this.getUserId(),
                        String.valueOf(rs.getInt("test_id")),
                        rs.getInt("score"),
                        rs.getInt("max_score"),
                        rs.getString("grade"),
                        rs.getTimestamp("completed_at").toLocalDateTime(),
                        "",
                        this.getName(),
                        "Tests",
                        this.group
                    );
                    results.add(result);
                }
            }
            this.testResults = results;
            System.out.println("✅ Ielādēti " + results.size() + " rezultāti no datubāzes");
        } catch (SQLException e) {
            System.err.println("❌ Kļūda ielādējot rezultātus: " + e.getMessage());
        }
        return results;
    }

    /**
     * Atgriež formatētu testu rezultātu pārskatu.
     *
     * @return formatēts rezultātu pārskats
     */
    public String getResultsReport() {
        if (testResults.isEmpty()) return "Nav testu rezultātu.";
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        sb.append("╔════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                    TESTU REZULTĀTI                             ║\n");
        sb.append("╠════════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ Students: %-52s ║\n", getName()));
        sb.append(String.format("║ Grupa:    %-52s ║\n", group));
        sb.append("╠════════════════════════════════════════════════════════════════╣\n");
        int count = 0;
        double totalPercentage = 0;
        for (Result r : testResults) {
            count++;
            totalPercentage += r.getPercentage();
            sb.append(String.format("║ %2d. %-30s %3d/%d %5.1f%% %2s ║\n", 
                count, 
                r.getTestName().length() > 30 ? r.getTestName().substring(0, 27) + "..." : r.getTestName(),
                r.getScore(), r.getMaxScore(), r.getPercentage(), r.getGrade()));
            sb.append(String.format("║     %-52s ║\n", r.getDateCompleted().format(formatter)));
        }
        double avg = count > 0 ? totalPercentage / count : 0;
        sb.append("╠════════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ VIDĒJAIS: %43.1f%% ║\n", avg));
        sb.append("╚════════════════════════════════════════════════════════════════╝");
        return sb.toString();
    }

    /**
     * Pārbauda, vai students pašlaik pilda testu.
     *
     * @return true, ja tests ir uzsākts
     */
    public boolean isTakingTest() { return currentTest != null; }

    /**
     * Atgriež atlikušo laiku testa pabeigšanai minūtēs.
     *
     * @return atlikušais laiks minūtēs vai -1, ja tests nav uzsākts
     */
    public long getRemainingTime() {
        if (currentTest == null || testStartTime == null) return -1;
        long elapsedMinutes = java.time.Duration.between(testStartTime, LocalDateTime.now()).toMinutes();
        return Math.max(0, currentTest.getTimeLimit() - elapsedMinutes);
    }

    /**
     * Pārbauda, vai testa laiks ir beidzies.
     *
     * @return true, ja atlikušais laiks ir 0 vai mazāks
     */
    public boolean isTimeExpired() { return getRemainingTime() <= 0; }

    // Getter metodes
    public String getStudentId() { return studentId; }
    public String getGroup() { return group; }
    public int getCourse() { return course; }
    public String getStudentNumber() { return studentNumber; }
    public Test getCurrentTest() { return currentTest; }
    public int getCurrentQuestionIndex() { return currentQuestionIndex; }
    public int getRightAnswers() { return rightAnswers; }
    public int getQuestionsCount() { return questionsCount; }
    
    /**
     * Atgriež atbilžu precizitātes procentuālo rādītāju.
     *
     * @return precizitāte procentos
     */
    public double getSuccessPercentage() { 
        return questionsCount > 0 ? (double) rightAnswers / questionsCount * 100 : 0; 
    }
    
    public List<Result> getTestResults() { return new ArrayList<>(testResults); }
    public LocalDateTime getTestStartTime() { return testStartTime; }

    @Override
    public String getRole() { return "STUDENT"; }
    
    @Override
    public String getUserType() { return "Students"; }
    
    @Override
    public String toString() { return super.toString() + " [Grupa: " + group + ", Kurss: " + course + "]"; }
}