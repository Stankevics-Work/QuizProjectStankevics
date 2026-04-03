package pd1_stankevics_24;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Testa klase, kas nodrošina testa izveidi, jautājumu pārvaldību,
 * atbilžu vērtēšanu un rezultātu aprēķināšanu. Katrs tests satur
 * jautājumu sarakstu, laika ierobežojumu, nokārtošanas slieksni
 * un citus parametrus.
 * 
 * @author armins.stankevics_24
 * @see Question
 * @see Result
 * @see User
 */
public class Test {
    // Testa pamatinformācija
    private String testId;
    private String title;
    private String topic;
    private User createdBy;
    private LocalDateTime dateCreated;
    
    // Testa saturs un iestatījumi
    private List<Question> questions;
    private List<Question> originalQuestions;
    
    // Testa parametri
    private int timeLimitMinutes;
    private boolean isActive;
    private int passingScore;
    private String description;
    private int maxAttempts;
    private boolean shuffleQuestions;

    /**
     * Izveido jaunu Test objektu ar noklusējuma iestatījumiem.
     *
     * @param testId      unikāls testa identifikators
     * @param title       testa nosaukums
     * @param topic       testa tēma
     * @param createdBy   testa autors (User objekts)
     * @param dateCreated testa izveides datums un laiks
     * @throws IllegalArgumentException ja testId vai title ir null vai tukšs
     */
    public Test(String testId, String title, String topic, User createdBy, LocalDateTime dateCreated) {
        setTestId(testId);
        setTitle(title);
        setTopic(topic);
        this.createdBy = createdBy;
        this.dateCreated = (dateCreated != null) ? dateCreated : LocalDateTime.now();
        this.questions = new ArrayList<>();
        this.originalQuestions = new ArrayList<>();
        this.timeLimitMinutes = 30;
        this.isActive = true;
        this.passingScore = 50;
        this.description = "";
        this.maxAttempts = 3;
        this.shuffleQuestions = false;
    }

    // ==================== SETTER METODES ====================

    /**
     * Iestata testa ID.
     *
     * @param testId testa identifikators (nedrīkst būt null vai tukšs)
     * @throws IllegalArgumentException ja testId ir null vai tukšs
     */
    public final void setTestId(String testId) {
        if (testId == null || testId.trim().isEmpty()) {
            throw new IllegalArgumentException("Testa ID nevar būt tukšs!");
        }
        this.testId = testId.trim();
    }

    /**
     * Iestata testa nosaukumu.
     *
     * @param title testa nosaukums (nedrīkst būt null vai tukšs)
     * @throws IllegalArgumentException ja title ir null vai tukšs
     */
    public final void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Testa nosaukums nevar būt tukšs!");
        }
        this.title = title.trim();
    }

    /**
     * Iestata testa tēmu.
     *
     * @param topic testa tēma (null vai tukšs tiks aizstāts ar "Vispārīga")
     */
    public final void setTopic(String topic) {
        this.topic = (topic != null && !topic.trim().isEmpty()) ? topic.trim() : "Vispārīga";
    }

    /**
     * Iestata testa aprakstu.
     *
     * @param description testa apraksts (null tiks konvertēts uz tukšu String)
     */
    public void setDescription(String description) {
        this.description = (description != null) ? description : "";
    }

    /**
     * Iestata laika ierobežojumu.
     *
     * @param minutes laika limits minūtēs (jābūt pozitīvam)
     * @throws IllegalArgumentException ja minutes &lt;= 0
     */
    public void setTimeLimit(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("Laika limitam jābūt pozitīvam skaitlim!");
        }
        this.timeLimitMinutes = minutes;
    }

    /**
     * Iestata nokārtošanas slieksni.
     *
     * @param passingScore nokārtošanas slieksnis procentos (0-100)
     * @throws IllegalArgumentException ja passingScore nav diapazonā 0-100
     */
    public void setPassingScore(int passingScore) {
        if (passingScore < 0 || passingScore > 100) {
            throw new IllegalArgumentException("Nokārtošanas slieksnim jābūt 0-100!");
        }
        this.passingScore = passingScore;
    }

    /**
     * Iestata maksimālo mēģinājumu skaitu.
     *
     * @param maxAttempts maksimālais mēģinājumu skaits (jābūt vismaz 1)
     * @throws IllegalArgumentException ja maxAttempts &lt; 1
     */
    public void setMaxAttempts(int maxAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("Maksimālajam mēģinājumu skaitam jābūt vismaz 1!");
        }
        this.maxAttempts = maxAttempts;
    }

    /**
     * Iestata jautājumu sajaukšanu.
     *
     * @param shuffle {@code true} ja jā sajauc jautājumu secība
     */
    public void setShuffleQuestions(boolean shuffle) {
        this.shuffleQuestions = shuffle;
    }

    /**
     * Aktivizē vai deaktivizē testu.
     *
     * @param active {@code true} ja tests ir pieejams, {@code false} ja nav
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }

    // ==================== JAUTĀJUMU PĀRVALDĪBA ====================

    /**
     * Pievieno jautājumu testam.
     *
     * @param question pievienojamais jautājums (nedrīkst būt null)
     */
    public void addQuestion(Question question) {
        if (question != null) {
            question.setTestId(this.testId);
            questions.add(question);
            originalQuestions.add(question);
        }
    }

    /**
     * Pievieno vairākus jautājumus testam.
     *
     * @param newQuestions pievienojamo jautājumu saraksts
     */
    public void addQuestions(List<Question> newQuestions) {
        if (newQuestions != null) {
            for (Question q : newQuestions) {
                if (q != null) {
                    addQuestion(q);
                }
            }
        }
    }

    /**
     * Noņem jautājumu no testa.
     *
     * @param questionId noņemamā jautājuma ID
     * @return {@code true} ja jautājums tika atrasts un noņemts
     */
    public boolean removeQuestion(String questionId) {
        boolean removed = questions.removeIf(q -> q.getQuestionId().equals(questionId));
        originalQuestions.removeIf(q -> q.getQuestionId().equals(questionId));
        return removed;
    }

    /**
     * Sajauc jautājumus testam, ja shuffleQuestions ir iestatīts uz true.
     */
    public void shuffleQuestions() {
        if (shuffleQuestions) {
            Collections.shuffle(questions);
        }
    }

    /**
     * Atjauno oriģinālo jautājumu secību.
     */
    public void restoreOriginalOrder() {
        questions = new ArrayList<>(originalQuestions);
    }

    // ==================== DATUBĀZES INTEGRĀCIJA ====================

    /**
     * Iekšējā klase atbilžu glabāšanai datubāzes ielādē.
     */
    private static class AnswerOption {
        String text;
        boolean isCorrect;
        int order;
        
        AnswerOption(String text, boolean isCorrect, int order) {
            this.text = text;
            this.isCorrect = isCorrect;
            this.order = order;
        }
    }

    /**
     * Ielādē testa jautājumus no datubāzes.
     * Ja datubāze nav pieejama vai nav jautājumu, izmanto noklusējuma jautājumus.
     *
     * @param connection aktīvs datubāzes savienojums (var būt null)
     * @param testId     testa ID datubāzē
     * @return {@code true} ja ielāde veiksmīga (vai izmantoti noklusējuma jautājumi)
     */
    public boolean loadQuestionsFromDatabase(Connection connection, int testId) {
        return loadQuestionsFromDatabase(connection, testId, 5);
    }

    /**
     * Ielādē testa jautājumus no datubāzes ar iespēju norādīt minimālo jautājumu skaitu.
     * Ja datubāze nav pieejama vai nav jautājumu, izmanto noklusējuma jautājumus.
     *
     * @param connection aktīvs datubāzes savienojums (var būt null)
     * @param testId     testa ID datubāzē
     * @param minQuestions minimālais nepieciešamo jautājumu skaits
     * @return {@code true} ja ielāde veiksmīga (vai izmantoti noklusējuma jautājumi)
     */
    public boolean loadQuestionsFromDatabase(Connection connection, int testId, int minQuestions) {
        questions.clear();
        originalQuestions.clear();
        
        // Ja nav savienojuma, izmanto noklusējuma jautājumus
        if (connection == null) {
            System.err.println("❌ Nav savienojuma ar datubāzi!");
            System.out.println("⚠ Izmanto noklusējuma jautājumus (" + minQuestions + " jautājumi)");
            addDefaultQuestions(minQuestions);
            return false;
        }
        
        System.out.println("\n=== SĀK TESTA JAUTĀJUMU IELĀDI NO DATUBĀZES ===");
        System.out.println("Testa ID: " + testId);
        
        boolean testExists = false;
        try {
            PreparedStatement checkStmt = connection.prepareStatement("SELECT COUNT(*) FROM tests WHERE id = ?");
            checkStmt.setInt(1, testId);
            ResultSet checkRs = checkStmt.executeQuery();
            checkRs.next();
            int testCount = checkRs.getInt(1);
            checkRs.close();
            checkStmt.close();
            
            testExists = (testCount > 0);
            if (testExists) {
                System.out.println("✅ Tests eksistē, ID=" + testId);
            } else {
                System.err.println("❌ Tests ar ID=" + testId + " neeksistē datubāzē!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Kļūda pārbaudot testu: " + e.getMessage());
            testExists = false;
        }
        
        if (!testExists) {
            System.out.println("⚠ Tests neeksistē datubāzē, izmanto noklusējuma jautājumus");
            addDefaultQuestions(minQuestions);
            return false;
        }
        
        // Mēģinām ielādēt jautājumus no datubāzes
        String questionSql = "SELECT id, question_text, points FROM questions WHERE test_id = ? ORDER BY id";
        Map<Integer, String> questionTexts = new HashMap<>();
        Map<Integer, Integer> questionPoints = new HashMap<>();
        List<Integer> questionIds = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(questionSql)) {
            pstmt.setInt(1, testId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int qId = rs.getInt("id");
                    String text = rs.getString("question_text");
                    int points = rs.getInt("points");
                    
                    questionIds.add(qId);
                    questionTexts.put(qId, text);
                    questionPoints.put(qId, points);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Kļūda ielādējot jautājumu ID: " + e.getMessage());
            addDefaultQuestions(minQuestions);
            return false;
        }
        
        if (questionIds.isEmpty()) {
            System.out.println("⚠ Nav jautājumu datubāzē, izmanto noklusējuma jautājumus");
            addDefaultQuestions(minQuestions);
            return false;
        }
        
        int count = 0;
        int totalQuestions = questionIds.size();
        
        for (int qId : questionIds) {
            List<AnswerOption> answers = new ArrayList<>();
            
            String answerSql = "SELECT option_text, is_correct, option_order FROM answer_options WHERE question_id = ? ORDER BY option_order";
            
            try (PreparedStatement ansStmt = connection.prepareStatement(answerSql)) {
                ansStmt.setInt(1, qId);
                
                try (ResultSet ansRs = ansStmt.executeQuery()) {
                    while (ansRs.next()) {
                        String optText = ansRs.getString("option_text");
                        boolean isCorrect = ansRs.getBoolean("is_correct");
                        int order = ansRs.getInt("option_order");
                        
                        if (optText != null && !optText.trim().isEmpty()) {
                            answers.add(new AnswerOption(optText, isCorrect, order));
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("❌ Kļūda ielādējot atbildes jautājumam ID=" + qId);
                continue;
            }
            
            // Ja nav 3 atbilžu variantu, izveido noklusējuma jautājumu
            if (answers.size() != 3) {
                String qText = questionTexts.get(qId);
                if (qText == null || qText.trim().isEmpty()) {
                    qText = "Jautājums " + qId;
                }
                
                Question q = new Question(
                    qText,
                    "Atbilde 1", "Atbilde 2", "Atbilde 3",
                    0,
                    "", "Vispārīgi",
                    questionPoints.getOrDefault(qId, 1),
                    Question.DIFFICULTY_MEDIUM
                );
                q.setTestId(String.valueOf(testId));
                questions.add(q);
                originalQuestions.add(q);
                count++;
                continue;
            }
            
            answers.sort((a1, a2) -> Integer.compare(a1.order, a2.order));
            
            int correctIndex = -1;
            for (int i = 0; i < answers.size(); i++) {
                if (answers.get(i).isCorrect) {
                    correctIndex = i;
                    break;
                }
            }
            
            if (correctIndex == -1) {
                correctIndex = 0;
            }
            
            String questionText = questionTexts.get(qId);
            if (questionText == null || questionText.trim().isEmpty()) {
                questionText = "Jautājums " + qId;
            }
            
            Question q = new Question(
                questionText,
                answers.get(0).text,
                answers.get(1).text,
                answers.get(2).text,
                correctIndex,
                "",
                "Vispārīgi",
                questionPoints.getOrDefault(qId, 1),
                Question.DIFFICULTY_MEDIUM
            );
            q.setTestId(String.valueOf(testId));
            
            questions.add(q);
            originalQuestions.add(q);
            count++;
        }
        
        System.out.println("✅ Veiksmīgi ielādēti " + count + " no " + totalQuestions + " jautājumiem");
        
        // Ja ielādēto jautājumu ir mazāk par nepieciešamo minimumu, pievieno noklusējuma jautājumus
        if (questions.size() < minQuestions) {
            System.out.println("⚠ Ielādēti tikai " + questions.size() + " jautājumi, pievienoju noklusējuma jautājumus līdz " + minQuestions);
            addDefaultQuestions(minQuestions - questions.size());
        }
        
        if (questions.isEmpty()) {
            System.out.println("⚠ Neviens jautājums netika ielādēts, izmanto noklusējuma jautājumus");
            addDefaultQuestions(minQuestions);
            return false;
        }
        
        if (shuffleQuestions) {
            shuffleQuestions();
        }
        
        return true;
    }

    /**
     * Pievieno noklusējuma (pagaidu) jautājumus testēšanai.
     * Pievieno 5 noklusējuma jautājumus.
     */
    private void addDefaultQuestions() {
        addDefaultQuestions(5);
    }

    /**
     * Pievieno noklusējuma (pagaidu) jautājumus testēšanai.
     * 
     * @param count vēlamais jautājumu skaits (ja 0 vai mazāk, pievieno 5 jautājumus)
     */
    private void addDefaultQuestions(int count) {
        if (count <= 0) {
            count = 5;
        }
        
        // Pamata jautājumu bāze (10 dažādi jautājumi)
        List<Question> defaultQuestions = new ArrayList<>();
        
        // 1. Jautājums
        defaultQuestions.add(new Question("Kas ir Java?", 
            "Programmēšanas valoda", "Operētājsistēma", "Datu bāze", 0,
            "Java ir objektorientēta programmēšanas valoda.", "Programmēšana", 1, Question.DIFFICULTY_EASY));
        
        // 2. Jautājums
        defaultQuestions.add(new Question("Kas ir JDBC?", 
            "Java Database Connectivity", "Java Debugging Code", "Java Data Binding", 0,
            "JDBC ir API savienojumam ar datubāzēm.", "Datu bāzes", 2, Question.DIFFICULTY_MEDIUM));
        
        // 3. Jautājums
        defaultQuestions.add(new Question("Kurš no šiem ir cikls Java?", 
            "for", "if", "switch", 0,
            "for, while un do-while ir cikli.", "Java", 1, Question.DIFFICULTY_EASY));
        
        // 4. Jautājums
        defaultQuestions.add(new Question("Kas ir OOP?", 
            "Objektorientēta programmēšana", "Funkcionālā programmēšana", "Procedurālā programmēšana", 0,
            "OOP ir programmēšanas paradigma, kas balstās uz objektiem.", "Programmēšana", 2, Question.DIFFICULTY_MEDIUM));
        
        // 5. Jautājums
        defaultQuestions.add(new Question("Kas ir 'public' Java?", 
            "Piekļuves modifikators", "Datu tips", "Cikla operators", 0,
            "'public' nozīmē, ka elements ir pieejams no jebkuras klases.", "Java", 1, Question.DIFFICULTY_EASY));
        
        // 6. Jautājums
        defaultQuestions.add(new Question("Kas ir JVM?", 
            "Java Virtual Machine", "Java Visual Manager", "Java Variable Memory", 0,
            "JVM izpilda Java baitu kodu.", "Java", 1, Question.DIFFICULTY_EASY));
        
        // 7. Jautājums
        defaultQuestions.add(new Question("Ko dara 'break' operators?", 
            "Pārtrauc ciklu", "Turpina ciklu", "Iziet no metodes", 0,
            "break pārtrauc cikla izpildi.", "Java", 1, Question.DIFFICULTY_EASY));
        
        // 8. Jautājums
        defaultQuestions.add(new Question("Kas ir 'array'?", 
            "Datu struktūra", "Klašu tips", "Interfeiss", 0,
            "Array ir datu struktūra fiksēta izmēra elementu glabāšanai.", "Programmēšana", 2, Question.DIFFICULTY_MEDIUM));
        
        // 9. Jautājums
        defaultQuestions.add(new Question("Kas ir SQL?", 
            "Strukturēta vaicājumu valoda", "Programmēšanas valoda", "Skriptu valoda", 0,
            "SQL ir valoda datubāzu pārvaldībai.", "Datu bāzes", 2, Question.DIFFICULTY_MEDIUM));
        
        // 10. Jautājums
        defaultQuestions.add(new Question("Kas ir 'inheritance' OOP?", 
            "Mantojums", "Polimorfisms", "Kapsulācija", 0,
            "Mantojums ļauj klasei pārmantot citas klases īpašības.", "Programmēšana", 2, Question.DIFFICULTY_MEDIUM));
        
        // Pievieno nepieciešamo jautājumu skaitu
        int toAdd = Math.min(count, defaultQuestions.size());
        for (int i = 0; i < toAdd; i++) {
            Question q = defaultQuestions.get(i);
            q.setTestId(this.testId);
            questions.add(q);
            originalQuestions.add(q);
        }
        
        // Ja vajag vairāk jautājumu par pieejamajiem, atkārto esošos
        if (questions.size() < count && !defaultQuestions.isEmpty()) {
            int index = 0;
            while (questions.size() < count) {
                Question originalQ = defaultQuestions.get(index % defaultQuestions.size());
                // Izveido jaunu jautājuma kopiju - izmantojot pareizos getterus
                String[] opts = originalQ.getOptions();
                Question copy = new Question(
                    originalQ.getText() + " (kopija)",
                    opts[0],  // 1. atbilde
                    opts[1],  // 2. atbilde  
                    opts[2],  // 3. atbilde
                    originalQ.getCorrectIndex(),
                    originalQ.getExplanation(),
                    originalQ.getCategory(),
                    originalQ.getPoints(),
                    originalQ.getDifficulty()
                );
                copy.setTestId(this.testId);
                questions.add(copy);
                originalQuestions.add(copy);
                index++;
            }
        }
        
        System.out.println("✅ Pievienoti " + questions.size() + " noklusējuma jautājumi");
    }

    // ==================== TESTA NOVĒRTĒŠANA ====================

    /**
     * Novērtē testa atbildes un atgriež rezultātu.
     *
     * @param userAnswers lietotāja atbilžu indeksu saraksts
     * @param userId      lietotāja ID
     * @return {@link Result} objekts ar testa rezultātu
     */
    public Result evaluateTest(List<Integer> userAnswers, String userId) {
        if (userAnswers == null) {
            throw new IllegalArgumentException("Atbilžu saraksts nevar būt null!");
        }
        
        if (questions.isEmpty()) {
            throw new IllegalStateException("Testā nav jautājumu!");
        }
        
        if (userAnswers.size() != questions.size()) {
            throw new IllegalArgumentException(
                "Atbilžu skaits (" + userAnswers.size() + ") nesakrīt ar jautājumu skaitu (" + 
                questions.size() + ")");
        }
        
        int totalPoints = 0;
        int earnedPoints = 0;
        
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            totalPoints += q.getPoints();
            
            if (i < userAnswers.size() && q.isCorrect(userAnswers.get(i))) {
                earnedPoints += q.getPoints();
            }
        }
        
        int percent = (int) Math.round((earnedPoints * 100.0) / totalPoints);
        int mark = calculateMark(percent);
        
        return new Result(
            "R" + System.currentTimeMillis(),
            userId,
            this.testId,
            earnedPoints,
            totalPoints,
            String.valueOf(mark),
            LocalDateTime.now(),
            getGradeDescription(mark),
            "",
            this.title,
            ""
        );
    }

    /**
     * Aprēķina atzīmi pēc procentuālā rezultāta.
     *
     * @param percent procentuālais rezultāts (0-100)
     * @return atzīme (1-10)
     */
    public int calculateMark(int percent) {
        if (percent >= 97) return 10;
        if (percent >= 92) return 9;
        if (percent >= 84) return 8;
        if (percent >= 76) return 7;
        if (percent >= 68) return 6;
        if (percent >= 60) return 5;
        if (percent >= 50) return 4;
        if (percent >= 40) return 3;
        if (percent >= 30) return 2;
        return 1;
    }

    /**
     * Atgriež atzīmes aprakstu.
     *
     * @param mark atzīme (1-10)
     * @return atzīmes apraksts
     */
    public String getGradeDescription(int mark) {
        switch (mark) {
            case 10: return "Izcili (10)";
            case 9: return "Teicami (9)";
            case 8: return "Ļoti labi (8)";
            case 7: return "Labi (7)";
            case 6: return "Gandrīz labi (6)";
            case 5: return "Viduvēji (5)";
            case 4: return "Gandrīz viduvēji (4)";
            case 3: return "Vāji (3)";
            case 2: return "Ļoti vāji (2)";
            case 1: return "Nepietiekami (1)";
            default: return "Nav vērtējuma";
        }
    }

    // ==================== PĀRBAUDES METODES ====================

    /**
     * Pārbauda vai tests ir izpildāms (aktīvs un satur jautājumus).
     *
     * @return {@code true} ja tests ir pieejams
     */
    public boolean isAvailable() {
        return isActive && !questions.isEmpty();
    }

    /**
     * Pārbauda vai students drīkst kārtot testu pēc iepriekšējo mēģinājumu skaita.
     *
     * @param previousResults iepriekšējie rezultāti
     * @return {@code true} ja mēģinājumu skaits ir mazāks par maxAttempts
     */
    public boolean canTakeTest(List<Result> previousResults) {
        if (previousResults == null) return true;
        
        int attempts = 0;
        for (Result r : previousResults) {
            if (r.getTestId().equals(this.testId)) {
                attempts++;
            }
        }
        return attempts < maxAttempts;
    }

    // ==================== GETTER METODES ====================

    /**
     * Atgriež testa ID.
     *
     * @return testa ID
     */
    public String getTestId() { 
        return testId; 
    }
    
    /**
     * Atgriež testa nosaukumu.
     *
     * @return testa nosaukums
     */
    public String getTitle() { 
        return title; 
    }
    
    /**
     * Atgriež testa tēmu.
     *
     * @return testa tēma
     */
    public String getTopic() { 
        return topic; 
    }
    
    /**
     * Atgriež testa autoru.
     *
     * @return testa autors
     */
    public User getCreatedBy() { 
        return createdBy; 
    }
    
    /**
     * Atgriež testa izveides datumu.
     *
     * @return testa izveides datums
     */
    public LocalDateTime getDateCreated() { 
        return dateCreated; 
    }
    
    /**
     * Atgriež jautājumu saraksta kopiju.
     *
     * @return jautājumu saraksta kopija
     */
    public List<Question> getQuestions() { 
        return new ArrayList<>(questions); 
    }
    
    /**
     * Atgriež laika ierobežojumu.
     *
     * @return laika ierobežojums minūtēs
     */
    public int getTimeLimit() { 
        return timeLimitMinutes; 
    }
    
    /**
     * Pārbauda vai tests ir aktīvs.
     *
     * @return {@code true} ja tests ir aktīvs
     */
    public boolean isActive() { 
        return isActive; 
    }
    
    /**
     * Atgriež nokārtošanas slieksni.
     *
     * @return nokārtošanas slieksnis procentos
     */
    public int getPassingScore() { 
        return passingScore; 
    }
    
    /**
     * Atgriež testa aprakstu.
     *
     * @return testa apraksts
     */
    public String getDescription() { 
        return description; 
    }
    
    /**
     * Atgriež maksimālo mēģinājumu skaitu.
     *
     * @return maksimālais mēģinājumu skaits
     */
    public int getMaxAttempts() { 
        return maxAttempts; 
    }
    
    /**
     * Pārbauda vai jautājumi tiks sajaukti.
     *
     * @return {@code true} ja jautājumi tiks sajaukti
     */
    public boolean isShuffleQuestions() { 
        return shuffleQuestions; 
    }
    
    /**
     * Atgriež jautājumu skaitu.
     *
     * @return jautājumu skaits
     */
    public int getQuestionCount() { 
        return questions.size(); 
    }
    
    /**
     * Aprēķina kopējo punktu skaitu testā.
     *
     * @return kopējais punktu skaits
     */
    public int getTotalPoints() { 
        int total = 0;
        for (Question q : questions) {
            total += q.getPoints();
        }
        return total;
    }

    /**
     * Atgriež testa teksta attēlojumu.
     *
     * @return testa teksta attēlojums
     */
    @Override
    public String toString() {
        return title + " (" + questions.size() + " jaut.) - " + 
               (isActive ? "Aktīvs" : "Neaktīvs");
    }
}