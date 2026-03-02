package pd1_stankevics_27;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Studentu lietotāja klase.
 * 
 * @author stankevics_27
 * @version 2.0
 */
public class Student extends User {

    private int questionsCount = 0;
    private int rightAnswers = 0;
    private List<Result> testResults = new ArrayList<>();

    /**
     * Izveido jaunu Student objektu.
     * 
     * @param name studenta vārds un uzvārds
     * @param login studenta lietotājvārds
     * @param password studenta parole
     */
    public Student(String name, String login, String password) {
        super(name, login, password);
    }

    /**
     * Pievieno jaunu atbildi.
     * 
     * @param question jautājums
     * @param selectedIndex izvēlētās atbildes indekss
     */
    public void addQuestion(Question question, int selectedIndex) {
        questionsCount++;
        if (question.isCorrect(selectedIndex)) {
            rightAnswers++;
        }
    }

    /**
     * Atgriež kopējo atbildēto jautājumu skaitu.
     */
    public int getAnswersCount() {
        return questionsCount;
    }

    /**
     * Atgriež pareizo atbilžu skaitu.
     */
    public int getRightAnswersCount() {
        return rightAnswers;
    }

    /**
     * Aprēķina veiksmīgo atbilžu procentu.
     */
    public double getSuccessPercentage() {
        return questionsCount > 0 ? (double) rightAnswers / questionsCount * 100 : 0;
    }

    /**
     * Saglabā testa rezultātu datubāzē.
     * 
     * @param connection datubāzes savienojums
     * @param score iegūto punktu skaits
     * @param totalQuestions kopējais jautājumu skaits
     * @param test tests, kas tika pildīts (nepieciešams atzīmes aprēķinam)
     * @return true ja saglabāšana veiksmīga
     */
    public boolean saveTestResult(Connection connection, int score, int totalQuestions, Test test) {
        if (connection == null) return false;
        
        try {
            String findUserSQL = "SELECT id FROM users WHERE username = ?";
            PreparedStatement findUserStmt = connection.prepareStatement(findUserSQL);
            findUserStmt.setString(1, this.getLogin());
            ResultSet userRs = findUserStmt.executeQuery();
            
            int userId = -1;
            if (userRs.next()) userId = userRs.getInt("id");
            userRs.close();
            findUserStmt.close();
            
            if (userId == -1) return false;
            
            int percent = (int) Math.round((score * 100.0) / totalQuestions);
            int mark = test.calculateMark(percent);
            
            Result result = new Result(
                String.valueOf(System.currentTimeMillis()),
                String.valueOf(userId),
                test.getTestId(),
                score,
                percent,
                String.valueOf(mark),
                java.time.LocalDateTime.now(),
                ""
            );
            testResults.add(result);
            
            String insertResultSQL = "INSERT INTO test_results (user_id, test_id, score, max_score, percentage) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(insertResultSQL);
            pstmt.setInt(1, userId);
            pstmt.setString(2, test.getTestId());
            pstmt.setInt(3, score);
            pstmt.setInt(4, totalQuestions);
            pstmt.setDouble(5, percent);
            pstmt.executeUpdate();
            pstmt.close();
            
            System.out.println("✅ Testa rezultāts saglabāts studentam: " + this.getName());
            return true;
            
        } catch (SQLException e) {
            System.err.println("Kļūda saglabājot rezultātus: " + e.getMessage());
            return false;
        }
    }

    /**
     * Parāda visus studenta testu rezultātus.
     * 
     * @param connection datubāzes savienojums
     * @return formatēts rezultātu teksts
     */
    public String showAllResults(Connection connection) {
        if (connection == null) return "Nav savienojuma ar datubāzi!";
        
        StringBuilder results = new StringBuilder();
        results.append("=== ").append(this.getName()).append(" TESTA REZULTĀTI ===\n\n");
        
        try {
            String sql = "SELECT tr.score, tr.max_score, tr.percentage, tr.completed_at, t.title " +
                         "FROM test_results tr " +
                         "JOIN tests t ON tr.test_id = t.id " +
                         "JOIN users u ON tr.user_id = u.id " +
                         "WHERE u.username = ? " +
                         "ORDER BY tr.completed_at DESC";
            
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, this.getLogin());
            ResultSet rs = pstmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count++;
                String title = rs.getString("title");
                int score = rs.getInt("score");
                int maxScore = rs.getInt("max_score");
                double percentage = rs.getDouble("percentage");
                Timestamp completedAt = rs.getTimestamp("completed_at");
                
                results.append(count).append(". ").append(title).append("\n");
                results.append("   Rezultāts: ").append(score).append("/").append(maxScore)
                       .append(" (").append(percentage).append("%)\n");
                results.append("   Datums: ").append(completedAt).append("\n\n");
            }
            
            rs.close();
            pstmt.close();
            
            if (count == 0) {
                results.append("Jums vēl nav pildīti testi.");
            }
            
        } catch (SQLException e) {
            results.append("Kļūda ielādējot rezultātus: ").append(e.getMessage());
            System.err.println("Kļūda ielādējot rezultātus: " + e.getMessage());
        }
        
        return results.toString();
    }

    /**
     * Atgriež visu studenta testu rezultātu sarakstu.
     */
    public List<Result> getTestResults() {
        return testResults;
    }
}