package pd1_stankevics_24;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pasniedzēja lietotāja klase, kas paplašina {@link User} funkcionalitāti ar
 * jautājumu un testu veidošanas tiesībām.
 * 
 * @author armins.stankevics_24
 */
public class Teacher extends User {

    private String teacherId;
    private String department;
    private String position;
    private List<String> subjects;
    private List<Question> questionsCreated;
    private List<Test> testsCreated;
    private Map<String, List<Result>> studentResults;
    private Map<String, Integer> testStatistics;

    /**
     * Konstruktors pasniedzēja izveidei ar pamatinformāciju.
     *
     * @param name     pasniedzēja vārds un uzvārds
     * @param login    pieteikšanās lietotājvārds
     * @param password pieteikšanās parole
     */
    public Teacher(String name, String login, String password) {
        super(name, login, password);
        this.teacherId = generateTeacherId();
        this.department = "Datorzinātņu katedra";
        this.position = "Lektors";
        this.subjects = new ArrayList<>();
        this.questionsCreated = new ArrayList<>();
        this.testsCreated = new ArrayList<>();
        this.studentResults = new HashMap<>();
        this.testStatistics = new HashMap<>();
    }

    /**
     * Konstruktors pasniedzēja izveidei ar visu informāciju.
     *
     * @param name       pasniedzēja vārds un uzvārds
     * @param login      pieteikšanās lietotājvārds
     * @param password   pieteikšanās parole
     * @param email      pasniedzēja e-pasta adrese
     * @param department pasniedzēja katedra
     * @param position   pasniedzēja amats
     */
    public Teacher(String name, String login, String password, String email, String department, String position) {
        super(name, login, password, email);
        this.teacherId = generateTeacherId();
        setDepartment(department);
        setPosition(position);
        this.subjects = new ArrayList<>();
        this.questionsCreated = new ArrayList<>();
        this.testsCreated = new ArrayList<>();
        this.studentResults = new HashMap<>();
        this.testStatistics = new HashMap<>();
    }

    /**
     * Ģenerē unikālu pasniedzēja identifikatoru.
     *
     * @return unikāls pasniedzēja ID
     */
    private String generateTeacherId() {
        return "TCH" + System.currentTimeMillis() + (int)(Math.random() * 100);
    }

    /**
     * Iestata pasniedzēja katedru.
     *
     * @param department pasniedzēja katedra
     */
    public final void setDepartment(String department) {
        this.department = (department != null && !department.trim().isEmpty()) ? 
                          department.trim() : "Datorzinātņu katedra";
    }

    /**
     * Iestata pasniedzēja amatu.
     *
     * @param position pasniedzēja amats
     */
    public final void setPosition(String position) {
        this.position = (position != null && !position.trim().isEmpty()) ? 
                        position.trim() : "Lektors";
    }

    /**
     * Pievieno pasniedzējam mācību priekšmetu.
     *
     * @param subject pievienojamais priekšmets
     */
    public void addSubject(String subject) {
        if (subject != null && !subject.trim().isEmpty() && !subjects.contains(subject.trim())) {
            subjects.add(subject.trim());
        }
    }

    /**
     * Noņem pasniedzējam mācību priekšmetu.
     *
     * @param subject noņemamais priekšmets
     * @return {@code true} ja priekšmets tika atrasts un noņemts
     */
    public boolean removeSubject(String subject) {
        return subjects.remove(subject);
    }

    /**
     * Izveido jaunu jautājumu ar visiem parametriem.
     *
     * @param text         jautājuma teksts
     * @param option1      pirmais atbildes variants (A)
     * @param option2      otrais atbildes variants (B)
     * @param option3      trešais atbildes variants (C)
     * @param correctIndex pareizās atbildes indekss (0=A, 1=B, 2=C)
     * @param category     jautājuma kategorija
     * @param points       punktu skaits par pareizu atbildi
     * @param difficulty   grūtības pakāpe (Viegla, Vidēja, Grūta)
     * @param explanation  jautājuma paskaidrojums
     * @return izveidotais jautājuma objekts
     * @throws IllegalArgumentException ja dati ir nederīgi
     */
    public Question createQuestion(String text, String option1, String option2, 
                                   String option3, int correctIndex, String category,
                                   int points, String difficulty, String explanation) {
        try {
            Question q = new Question(text, option1, option2, option3, correctIndex, 
                                      explanation, category, points, difficulty);
            q.setCreatedBy(this.getName());
            questionsCreated.add(q);
            return q;
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Kļūda veidojot jautājumu: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Izveido jaunu jautājumu ar noklusējuma parametriem.
     *
     * @param text         jautājuma teksts
     * @param option1      pirmais atbildes variants (A)
     * @param option2      otrais atbildes variants (B)
     * @param option3      trešais atbildes variants (C)
     * @param correctIndex pareizās atbildes indekss (0=A, 1=B, 2=C)
     * @return izveidotais jautājuma objekts
     */
    public Question createQuestion(String text, String option1, String option2, 
                                   String option3, int correctIndex) {
        return createQuestion(text, option1, option2, option3, correctIndex, 
                             "Vispārīgi", 1, Question.DIFFICULTY_MEDIUM, "");
    }

    /**
     * Izveido jaunu testu ar visiem parametriem.
     *
     * @param testId           testa unikālais identifikators
     * @param title            testa nosaukums
     * @param topic            testa tēma
     * @param timeLimit        laika limits minūtēs
     * @param passingScore     nokārtošanas slieksnis procentos
     * @param description      testa apraksts
     * @param maxAttempts      maksimālais mēģinājumu skaits
     * @param shuffleQuestions vai jautājumi tiek sajaukti
     * @return izveidotais testa objekts
     */
    public Test createTest(String testId, String title, String topic, 
                           int timeLimit, int passingScore, String description,
                           int maxAttempts, boolean shuffleQuestions) {
        Test test = new Test(testId, title, topic, this, LocalDateTime.now());
        test.setTimeLimit(timeLimit);
        test.setPassingScore(passingScore);
        test.setDescription(description);
        test.setMaxAttempts(maxAttempts);
        test.setShuffleQuestions(shuffleQuestions);
        testsCreated.add(test);
        return test;
    }

    /**
     * Izveido jaunu testu ar noklusējuma parametriem.
     *
     * @param testId testa unikālais identifikators
     * @param title  testa nosaukums
     * @param topic  testa tēma
     * @return izveidotais testa objekts
     */
    public Test createTest(String testId, String title, String topic) {
        return createTest(testId, title, topic, 30, 50, "", 3, false);
    }

    /**
     * Pievieno jautājumu testam.
     *
     * @param test     tests, kuram pievieno jautājumu
     * @param question pievienojamais jautājums
     */
    public void addQuestionToTest(Test test, Question question) {
        if (test != null && question != null) test.addQuestion(question);
    }

    /**
     * Saglabā jautājumu datubāzē kopā ar atbilžu variantiem.
     *
     * @param connection aktīvs savienojums ar datubāzi
     * @param testId     testa ID, kuram pieder jautājums
     * @param question   saglabājamais jautājums
     * @return true, ja jautājums veiksmīgi saglabāts
     */
    public boolean saveQuestionToDatabase(Connection connection, int testId, Question question) {
        if (connection == null) return false;
        String sql = "INSERT INTO questions (test_id, question_text, category, points, explanation, difficulty, created_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, testId);
            pstmt.setString(2, question.getText());
            pstmt.setString(3, question.getCategory());
            pstmt.setInt(4, question.getPoints());
            pstmt.setString(5, question.getExplanation());
            pstmt.setString(6, question.getDifficulty());
            pstmt.setString(7, this.getName());
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int questionDbId = rs.getInt(1);
                    saveAnswerOptions(connection, questionDbId, question);
                }
                rs.close();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Kļūda saglabājot jautājumu: " + e.getMessage());
        }
        return false;
    }

    /**
     * Saglabā jautājuma atbilžu variantus datubāzē.
     *
     * @param connection    aktīvs savienojums ar datubāzi
     * @param questionDbId  jautājuma ID datubāzē
     * @param question      jautājuma objekts ar atbilžu variantiem
     * @throws SQLException ja rodas kļūda datubāzes darbībā
     */
    private void saveAnswerOptions(Connection connection, int questionDbId, Question question) throws SQLException {
        String sql = "INSERT INTO answer_options (question_id, option_text, is_correct, option_order) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String[] options = question.getOptions();
            for (int i = 0; i < options.length; i++) {
                pstmt.setInt(1, questionDbId);
                pstmt.setString(2, options[i]);
                pstmt.setBoolean(3, i == question.getCorrectIndex());
                pstmt.setInt(4, i + 1);
                pstmt.executeUpdate();
            }
        }
    }

    /**
     * Ielādē visus testu rezultātus no datubāzes.
     *
     * @param connection aktīvs savienojums ar datubāzi
     * @return ielādēto rezultātu saraksts
     */
    public List<Result> loadAllResults(Connection connection) {
        List<Result> results = new ArrayList<>();
        if (connection == null) return results;
        String sql = "SELECT tr.*, u.first_name, u.last_name, t.title " +
                     "FROM test_results tr " +
                     "JOIN users u ON tr.user_id = u.id " +
                     "JOIN tests t ON tr.test_id = t.id " +
                     "ORDER BY tr.completed_at DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String studentName = rs.getString("first_name") + " " + rs.getString("last_name");
                Result result = new Result(
                    rs.getString("result_id"),
                    String.valueOf(rs.getInt("user_id")),
                    rs.getString("test_id"),
                    rs.getInt("score"),
                    rs.getInt("max_score"),
                    rs.getString("grade"),
                    rs.getTimestamp("completed_at").toLocalDateTime(),
                    "",
                    studentName,
                    rs.getString("title"),
                    ""
                );
                results.add(result);
                String testId = rs.getString("test_id");
                studentResults.computeIfAbsent(testId, k -> new ArrayList<>()).add(result);
            }
        } catch (SQLException e) {
            System.err.println("❌ Kļūda ielādējot rezultātus: " + e.getMessage());
        }
        return results;
    }

    /**
     * Aprēķina testa statistiku.
     *
     * @param testId testa identifikators
     */
    public void calculateTestStatistics(String testId) {
        List<Result> results = studentResults.get(testId);
        if (results == null || results.isEmpty()) {
            testStatistics.put(testId + "_count", 0);
            return;
        }
        int count = results.size();
        int passed = 0;
        double sumPercentage = 0;
        int maxScore = 0;
        int minScore = 100;
        for (Result r : results) {
            sumPercentage += r.getPercentage();
            if (r.isPassed()) passed++;
            if (r.getPercentage() > maxScore) maxScore = (int)r.getPercentage();
            if (r.getPercentage() < minScore) minScore = (int)r.getPercentage();
        }
        double avg = sumPercentage / count;
        testStatistics.put(testId + "_count", count);
        testStatistics.put(testId + "_passed", passed);
        testStatistics.put(testId + "_avg", (int)avg);
        testStatistics.put(testId + "_max", maxScore);
        testStatistics.put(testId + "_min", minScore);
    }

    /**
     * Atgriež formatētu testa statistiku.
     *
     * @param testId testa identifikators
     * @return formatēta testa statistika
     */
    public String getTestStatistics(String testId) {
        calculateTestStatistics(testId);
        Integer count = testStatistics.get(testId + "_count");
        if (count == null || count == 0) return "Nav datu par testu " + testId;
        Integer passed = testStatistics.get(testId + "_passed");
        Integer avg = testStatistics.get(testId + "_avg");
        Integer max = testStatistics.get(testId + "_max");
        Integer min = testStatistics.get(testId + "_min");
        double passRate = (passed * 100.0) / count;
        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════╗\n");
        sb.append("║                TESTA STATISTIKA                   ║\n");
        sb.append("╠════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ Testa ID: %-39s ║\n", testId));
        sb.append(String.format("║ Studentu skaits: %-31d ║\n", count));
        sb.append(String.format("║ Nokārtojuši: %-35d ║\n", passed));
        sb.append(String.format("║ Sekmība: %-38.1f%% ║\n", passRate));
        sb.append(String.format("║ Vidējais: %-38d%% ║\n", avg));
        sb.append(String.format("║ Maksimālais: %-35d%% ║\n", max));
        sb.append(String.format("║ Minimālais: %-35d%% ║\n", min));
        sb.append("╚════════════════════════════════════════════════════╝");
        return sb.toString();
    }

    /**
     * Eksportē visus rezultātus CSV formātā.
     *
     * @return CSV formātā formatēti rezultāti
     */
    public String exportResultsToCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append("Testa ID,Studenta Vārds,Grupa,Punkti,Maksimums,Procenti,Atzīme,Datums\n");
        for (Map.Entry<String, List<Result>> entry : studentResults.entrySet()) {
            for (Result r : entry.getValue()) {
                sb.append(r.toCSV()).append("\n");
            }
        }
        return sb.toString();
    }

    // Getter metodes
    public String getTeacherId() { return teacherId; }
    public String getDepartment() { return department; }
    public String getPosition() { return position; }
    public List<String> getSubjects() { return new ArrayList<>(subjects); }
    public List<Question> getQuestionsCreated() { return new ArrayList<>(questionsCreated); }
    public List<Test> getTestsCreated() { return new ArrayList<>(testsCreated); }
    public int getQuestionsCount() { return questionsCreated.size(); }
    public int getTestsCount() { return testsCreated.size(); }
    public Map<String, List<Result>> getStudentResults() { return studentResults; }

    @Override
    public String getRole() { return "TEACHER"; }
    
    @Override
    public String getUserType() { return "Pasniedzējs"; }
    
    @Override
    public String toString() { return super.toString() + " [Katedra: " + department + ", Amats: " + position + "]"; }
}