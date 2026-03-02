package pd1_stankevics_27;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Testa klase.
 * 
 * @author stankevics_27
 * @version 2.0
 */
public class Test {
    private String test_id;
    private String title;
    private String topic;
    private User created_by;
    private LocalDateTime date_created;
    private List<Question> questions = new ArrayList<>();

    /**
     * Izveido jaunu Test objektu.
     * 
     * @param testId testa identifikators
     * @param title testa nosaukums
     * @param topic testa tēma
     * @param createdBy testa izveidotājs
     * @param dateCreated izveides datums
     */
    public Test(String testId, String title, String topic, User createdBy, LocalDateTime dateCreated) {
        this.test_id = testId;
        this.title = title;
        this.topic = topic;
        this.created_by = createdBy;
        this.date_created = dateCreated;
    }

    /**
     * Pievieno jautājumu testam.
     */
    public void addQuestion(Question question) {
        questions.add(question);
    }

    /**
     * Atgriež visus testa jautājumus.
     */
    public List<Question> getQuestions() {
        return questions;
    }

    /**
     * Atgriež testa nosaukumu.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Atgriež testa ID.
     */
    public String getTestId() {
        return test_id;
    }

    /**
     * Ielādē testa jautājumus no datubāzes.
     * 
     * @param connection datubāzes savienojums
     * @param testId testa ID
     * @return true ja ielāde veiksmīga
     */
    public boolean loadQuestionsFromDatabase(Connection connection, int testId) {
        if (connection == null) return false;
        
        questions.clear();
        
        try {
            String sql = "SELECT q.id, q.question_text, " +
                         "ao1.option_text as opt1, " +
                         "ao2.option_text as opt2, " +
                         "ao3.option_text as opt3, " +
                         "CASE " +
                         "  WHEN ao1.is_correct = true THEN 0 " +
                         "  WHEN ao2.is_correct = true THEN 1 " +
                         "  WHEN ao3.is_correct = true THEN 2 " +
                         "  ELSE 0 END as correct_index " +
                         "FROM questions q " +
                         "LEFT JOIN answer_options ao1 ON q.id = ao1.question_id AND ao1.option_order = 1 " +
                         "LEFT JOIN answer_options ao2 ON q.id = ao2.question_id AND ao2.option_order = 2 " +
                         "LEFT JOIN answer_options ao3 ON q.id = ao3.question_id AND ao3.option_order = 3 " +
                         "WHERE q.test_id = ? " +
                         "ORDER BY q.id";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, testId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    
                    int count = 0;
                    while (rs.next()) {
                        String questionText = rs.getString("question_text");
                        String opt1 = rs.getString("opt1");
                        String opt2 = rs.getString("opt2");
                        String opt3 = rs.getString("opt3");
                        int correctIndex = rs.getInt("correct_index");
                        
                        if (questionText != null && opt1 != null && opt2 != null && opt3 != null) {
                            Question q = new Question(questionText, opt1, opt2, opt3, correctIndex);
                            questions.add(q);
                            count++;
                        }
                    }
                    
                    System.out.println("✅ Ielādēti " + count + " jautājumi testam ID=" + testId);
                    
                    if (count == 0) {
                        addDefaultQuestions();
                    }
                    return count > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Kļūda ielādējot jautājumus: " + e.getMessage());
            e.printStackTrace();
            addDefaultQuestions();
            return false;
        }
    }

    /**
     * Pievieno pagaidu testa jautājumus (ja datubāzē nav datu).
     */
    private void addDefaultQuestions() {
        System.out.println("⚠ Izmantoju pagaidu testa jautājumus");
        questions.add(new Question("Kas ir Java?", "Programmēšanas valoda", "Operētājsistēma", "Datu bāze", 0));
        questions.add(new Question("Kas ir SQL?", "Datu bāzes valoda", "Programmēšanas valoda", "Operētājsistēma", 0));
        questions.add(new Question("Kas ir OOP?", "Objektorientēta programmēšana", "Funkcionālā programmēšana", "Procedurālā programmēšana", 0));
    }

    /**
     * Novērtē testa atbildes un atgriež rezultātu.
     * 
     * @param userAnswers lietotāja atbilžu indeksu saraksts
     * @param userId lietotāja ID
     * @return Result objekts ar testa rezultātu
     */
    public Result evaluateTest(List<Integer> userAnswers, String userId) {
        if (userAnswers.size() != questions.size()) {
            throw new IllegalArgumentException("Atbilžu skaits nesakrīt ar jautājumu skaitu");
        }
        
        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).isCorrect(userAnswers.get(i))) {
                score++;
            }
        }
        
        int percent = (int) Math.round((score * 100.0) / questions.size());
        int mark = calculateMark(percent);
        
        return new Result(
            String.valueOf(System.currentTimeMillis()),
            userId,
            this.test_id,
            score,
            percent,
            String.valueOf(mark),
            LocalDateTime.now(),
            ""
        );
    }

    /**
     * Aprēķina atzīmi pēc procentuālā rezultāta.
     * 
     * @param percent procentuālais rezultāts
     * @return atzīme 10 ballu skalā
     */
    public int calculateMark(int percent) {
        if (percent >= 97) return 10;
        if (percent >= 92) return 9;
        if (percent >= 84) return 8;
        if (percent >= 76) return 7;
        if (percent >= 68) return 6;
        if (percent >= 60) return 5;
        if (percent >= 45) return 4;
        if (percent >= 30) return 3;
        if (percent >= 15) return 2;
        return 1;
    }

    /**
     * Pievieno lomu testam.
     */
    public void assignRole(Role role) {
        // TODO
    }
}