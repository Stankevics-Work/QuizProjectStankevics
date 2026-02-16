package pd1_stankevics_27;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.InputStream;

public class Interface extends javax.swing.JFrame {
    
    private static final String DRIVER = "org.apache.derby.jdbc.ClientDriver";

    private String URL;
    private String USER;
    private String PASSWORD;

    private Student currentStudent;
    private List<Question> testQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int selectedAnswer = -1;

    private Connection connection = null;
    
    public Interface() {
    initComponents();

    loadConfig();

    StartChoice.setSize(500, 500);
    StartChoice.setLocationRelativeTo(null);
    StartChoice.setModal(true);
    StartChoice.setVisible(true);

    SwingUtilities.invokeLater(() -> {
        try {
            initializeDatabaseConnection();
        } catch (Exception e) {
            System.err.println("Database init failed, but continuing: " + e.getMessage());
        }
    });
}

    
   private void loadConfig() {
        try {
            Properties p = new Properties();

            InputStream in = getClass().getResourceAsStream("/pd1_stankevics_27/config.properties");

            if (in == null) {
                JOptionPane.showMessageDialog(this,
                    "config.properties NAV ATRASTS!\n" +
                    "Tam jābūt: src/pd1_stankevics_27/config.properties",
                    "Konfigurācijas kļūda",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            p.load(in);

            URL = p.getProperty("db.url");
            USER = p.getProperty("db.user");
            PASSWORD = p.getProperty("db.password");

            System.out.println("Config loaded:");
            System.out.println("URL = " + URL);
            System.out.println("USER = " + USER);

        } catch (Exception e) {
            e.printStackTrace();
        }
   }
    
private void initializeDatabaseConnection() {
    try {
        System.out.println("=== DERBY NETWORK SERVER CONNECTION ===");
        System.out.println("Driver: " + DRIVER);
        System.out.println("URL: " + URL);
        System.out.println("User: " + USER);
        
        // 1. Ielādē Derby ClientDriver (nevis EmbeddedDriver)
        try {
            Class.forName(DRIVER);
            System.out.println("✅ ClientDriver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Derby ClientDriver not found!");
            JOptionPane.showMessageDialog(this,
                "Apache Derby Client draiveris nav atrasts!\n" +
                "Pievienojiet derbyclient.jar (nevis derby.jar) projekta bibliotēkām.",
                "Draivera kļūda",
                JOptionPane.ERROR_MESSAGE);
            connection = null;
            return;
        }
        
        // 2. Izveido savienojumu ar lietotājvārdu/paroli
        System.out.println("⏳ Connecting to Derby Network Server...");
        Properties props = new Properties();
        props.put("user", USER);
        props.put("password", PASSWORD);
        props.put("create", "true"); // Izveido datubāzi, ja tāda nav
        
        connection = DriverManager.getConnection(URL, props);
        System.out.println("✅✅✅ CONNECTED to Derby Network Server!");
        
        // 3. Pārbauda savienojumu
        try {
            Statement testStmt = connection.createStatement();
            ResultSet rs = testStmt.executeQuery("SELECT 1 FROM SYS.SYSTABLES FETCH FIRST 1 ROWS ONLY");
            System.out.println("✅ Test query executed successfully");
            rs.close();
            testStmt.close();
        } catch (SQLException e) {
            System.err.println("⚠ Test query failed: " + e.getMessage());
        }
        
        // 4. Izveido tabulas
        initializeTables();
        
    } catch (SQLException e) {
        System.err.println("❌ Database connection failed: " + e.getMessage());
        System.err.println("Error code: " + e.getErrorCode());
        System.err.println("SQL state: " + e.getSQLState());
        
        String errorMessage = "Nevar izveidot savienojumu ar datu bāzi!\n\n";
        
        // Pārbauda konkrētas kļūdas
        if (e.getSQLState() != null && e.getSQLState().startsWith("08")) {
            // Connection error - server not running
            errorMessage += "Kļūda: Nav savienojuma ar Derby serveri\n\n";
            errorMessage += "Lai salabotu:\n";
            errorMessage += "1. Palaidiet Derby Network Server:\n";
            errorMessage += "   startNetworkServer.bat (Windows) vai\n";
            errorMessage += "   ./startNetworkServer (Linux/Mac)\n";
            errorMessage += "2. Pārbaudiet, vai ports 1527 ir brīvs\n";
            errorMessage += "3. Pārbaudiet, vai serveris darbojas\n\n";
        } else if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
            errorMessage += "Kļūda: Serveris neatbild (connection refused)\n\n";
            errorMessage += "Pārbaudiet:\n";
            errorMessage += "1. Vai Derby Network Server ir palaists?\n";
            errorMessage += "2. Vai ports 1527 nav aizņemts?\n";
            errorMessage += "3. Vai varat pieslēgties ar ij (Derby tool)?\n";
        }
        
        errorMessage += "Tehniskā informācija:\n";
        errorMessage += "Kļūda: " + e.getMessage() + "\n";
        errorMessage += "SQL State: " + e.getSQLState() + "\n";
        
        JOptionPane.showMessageDialog(this,
            errorMessage,
            "Savienojuma kļūda",
            JOptionPane.ERROR_MESSAGE);
        connection = null;
        
    } catch (Exception e) {
        System.err.println("❌ Unexpected error: " + e.getMessage());
        e.printStackTrace();
        JOptionPane.showMessageDialog(this,
            "Negaidīta kļūda:\n" + e.getMessage(),
            "Sistēmas kļūda",
            JOptionPane.ERROR_MESSAGE);
        connection = null;
    }
}

private boolean ensureConnection() {
    // 1. Ja savienojums jau pastāv
    if (connection != null) {
        try {
            if (!connection.isClosed() && connection.isValid(2)) {
                System.out.println("✅ Connection is active and valid");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("⚠ Error checking connection: " + e.getMessage());
        }
    }
    
    // 2. Mēģina izveidot jaunu savienojumu
    System.out.println("🔄 Re-establishing database connection...");
    
    try {
        // Aizver veco savienojumu, ja tas ir atvērts
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    } catch (SQLException e) {
        // Ignorē aizvēršanas kļūdas
    }
    
    // Izveido jaunu savienojumu
    initializeDatabaseConnection();
    
    return connection != null;
}

private void initializeTables() {
    if (connection == null) return;
    
    try {
        Statement stmt = connection.createStatement();
        
        // **NEBŪTISKAS IZMAINAS** - tikai pārbauda savienojumu
        // Mēs NENEDARĪSIM CREATE TABLE, jo tabulas jau eksistē no jūsu skripta
        
        System.out.println("=== Checking database structure ===");
        
        // Pārbauda, vai ir dati jautājumos
        ResultSet rs = stmt.executeQuery(
            "SELECT COUNT(*) as question_count, " +
            "(SELECT COUNT(*) FROM answer_options) as option_count " +
            "FROM questions"
        );
        
        if (rs.next()) {
            int questionCount = rs.getInt("question_count");
            int optionCount = rs.getInt("option_count");
            System.out.println("ℹ️ Questions in DB: " + questionCount);
            System.out.println("ℹ️ Answer options in DB: " + optionCount);
            
            if (questionCount == 0) {
                System.out.println("⚠ Database is empty - please run your SQL script!");
            }
        }
        
        rs.close();
        stmt.close();
        
    } catch (SQLException e) {
        System.err.println("Table check error: " + e.getMessage());
        // Tas var būt normāli, ja tabulas vēl neeksistē
    }
}
        
    private void closeDatabaseConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 Savienojums aizvērts");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

private void loadQuestionsFromDatabase() {
    testQuestions.clear();
    
    if (!ensureConnection()) {
        JOptionPane.showMessageDialog(this, 
            "Nav savienojuma ar datu bāzi!", 
            "Kļūda", 
            JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    try {
        // Vienkāršs SQL vaicājums, kas savieno questions un answer_options
        String query = "SELECT " +
                      "q.question_text, " +
                      "MAX(CASE WHEN ao.option_order = 1 THEN ao.option_text END) as option1, " +
                      "MAX(CASE WHEN ao.option_order = 2 THEN ao.option_text END) as option2, " +
                      "MAX(CASE WHEN ao.option_order = 3 THEN ao.option_text END) as option3, " +
                      "MAX(CASE WHEN ao.is_correct = true THEN ao.option_order END) as correct_order " +
                      "FROM questions q " +
                      "JOIN answer_options ao ON q.id = ao.question_id " +
                      "GROUP BY q.id, q.question_text " +
                      "ORDER BY q.id";
        
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        
        int questionCount = 0;
        while (rs.next()) {
            String questionText = rs.getString("question_text");
            String option1 = rs.getString("option1");
            String option2 = rs.getString("option2");
            String option3 = rs.getString("option3");
            int correctOrder = rs.getInt("correct_order"); // 1, 2 vai 3
            
            // Konvertē no 1,2,3 uz 0,1,2 (Java indeksi)
            int correctIndex = correctOrder - 1;
            
            if (questionText != null && option1 != null && option2 != null && option3 != null && correctIndex >= 0) {
                Question question = new Question(questionText, option1, option2, option3, correctIndex);
                testQuestions.add(question);
                questionCount++;
                
                System.out.println("✅ Jautājums " + questionCount + ": " + questionText);
                System.out.println("   Atbilžu varianti: " + option1 + " | " + option2 + " | " + option3);
                System.out.println("   Pareizais variants: " + correctIndex);
            }
        }
        
        rs.close();
        stmt.close();
        
        System.out.println("=== KOPĀ IELĀDĒTI " + questionCount + " JAUTĀJUMI ===");
        
        if (questionCount == 0) {
            System.out.println("⚠ Nav jautājumu datubāzē");
            JOptionPane.showMessageDialog(this, 
                "Datu bāzē nav jautājumu!", 
                "Kļūda", 
                JOptionPane.ERROR_MESSAGE);
        }
        
    } catch (SQLException e) {
        System.err.println("❌ Kļūda ielādējot jautājumus: " + e.getMessage());
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, 
            "Kļūda ielādējot jautājumus: " + e.getMessage(), 
            "Datu bāzes kļūda", 
            JOptionPane.ERROR_MESSAGE);
    }
}

private void loadQuestionsAlternative() throws SQLException {
    System.out.println("⚠ Mēģinu alternatīvo metodi...");
    
    // Vienkāršāks vaicājums
    String query = 
        "SELECT q.id, q.question_text, " +
        "ao1.option_text AS opt1, " +
        "ao2.option_text AS opt2, " +
        "ao3.option_text AS opt3, " +
        "CASE " +
        "  WHEN ao1.is_correct = true THEN 0 " +
        "  WHEN ao2.is_correct = true THEN 1 " +
        "  WHEN ao3.is_correct = true THEN 2 " +
        "  ELSE -1 " +
        "END AS correct_index " +
        "FROM questions q " +
        "LEFT JOIN answer_options ao1 ON q.id = ao1.question_id AND ao1.option_order = 1 " +
        "LEFT JOIN answer_options ao2 ON q.id = ao2.question_id AND ao2.option_order = 2 " +
        "LEFT JOIN answer_options ao3 ON q.id = ao3.question_id AND ao3.option_order = 3 " +
        "WHERE q.test_id = 1 " +
        "ORDER BY q.id";
    
    Statement stmt = connection.createStatement();
    ResultSet rs = stmt.executeQuery(query);
    
    int count = 0;
    while (rs.next()) {
        String questionText = rs.getString("question_text");
        String option1 = rs.getString("opt1");
        String option2 = rs.getString("opt2");
        String option3 = rs.getString("opt3");
        int correctOption = rs.getInt("correct_index");
        
        if (option1 != null && option2 != null && option3 != null && correctOption >= 0) {
            Question question = new Question(questionText, option1, option2, option3, correctOption);
            testQuestions.add(question);
            count++;
        }
    }
    
    rs.close();
    stmt.close();
    
    System.out.println("✅ Ielādēti " + count + " jautājumi ar alternatīvo metodi");
}

private void loadQuestionsSimplified() throws SQLException {
    System.out.println("⚠ Mēģinu vienkāršāku vaicājumu...");
    
    // Alternatīvs vaicājums, kas var būt vienkāršāks
    String query = "SELECT q.question_text, " +
                  "(SELECT option_text FROM answer_options ao1 WHERE ao1.question_id = q.id AND ao1.option_order = 1) AS opt1, " +
                  "(SELECT option_text FROM answer_options ao2 WHERE ao2.question_id = q.id AND ao2.option_order = 2) AS opt2, " +
                  "(SELECT option_text FROM answer_options ao3 WHERE ao3.question_id = q.id AND ao3.option_order = 3) AS opt3, " +
                  "(SELECT option_order-1 FROM answer_options ao4 WHERE ao4.question_id = q.id AND ao4.is_correct = true FETCH FIRST ROW ONLY) AS correct " +
                  "FROM questions q " +
                  "WHERE q.test_id = 1 " +
                  "ORDER BY q.id " +
                  "FETCH FIRST 10 ROWS ONLY";
    
    Statement stmt = connection.createStatement();
    ResultSet rs = stmt.executeQuery(query);
    
    int count = 0;
    while (rs.next()) {
        String questionText = rs.getString("question_text");
        String option1 = rs.getString("opt1");
        String option2 = rs.getString("opt2");
        String option3 = rs.getString("opt3");
        int correctOption = rs.getInt("correct");
        
        if (option1 != null && option2 != null && option3 != null) {
            Question question = new Question(questionText, option1, option2, option3, correctOption);
            testQuestions.add(question);
            count++;
        }
    }
    
    rs.close();
    stmt.close();
    
    System.out.println("✅ Ielādēti " + count + " jautājumi ar vienkāršotu vaicājumu");
}
    
    private void addDefaultQuestions() {
    // Netiek izsaukts automātiski vairs
    System.out.println("⚠ Using default questions as fallback");
    
    testQuestions.add(new Question(
        "Kļūda: Nav jautājumu datubāzē!", 
        "Lūdzu, ielādējiet datus no SQL skripta",
        "Palaidiet derby serveri",
        "Pārbaudiet savienojumu",
        0
    ));
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        StartChoice = new javax.swing.JDialog();
        jRegisterButton = new javax.swing.JButton();
        jLoginButton = new javax.swing.JButton();
        jProgramNameLabel = new javax.swing.JLabel();
        LoginOrRegister = new javax.swing.JDialog();
        jLoginButton1 = new javax.swing.JButton();
        jRegisterButton1 = new javax.swing.JButton();
        jUsernameLabel1 = new javax.swing.JLabel();
        jUsernameField1 = new javax.swing.JTextField();
        jPasswordField1 = new javax.swing.JTextField();
        jPasswordLabel1 = new javax.swing.JLabel();
        Register = new javax.swing.JDialog();
        jRegisterButton2 = new javax.swing.JButton();
        jNameLabel = new javax.swing.JLabel();
        jNameField = new javax.swing.JTextField();
        jSurnameLabel = new javax.swing.JLabel();
        jSurnameField = new javax.swing.JTextField();
        jUsernameLabel2 = new javax.swing.JLabel();
        jUsernameField2 = new javax.swing.JTextField();
        jPasswordLabel2 = new javax.swing.JLabel();
        jPasswordField2 = new javax.swing.JTextField();
        jRepeatPasswordLabel = new javax.swing.JLabel();
        jRepeatPasswordField3 = new javax.swing.JTextField();
        StartTest = new javax.swing.JDialog();
        jButtonStartTest2 = new javax.swing.JButton();
        jLabelTestName = new javax.swing.JLabel();
        ChoiceBetweenAnswers = new javax.swing.JDialog();
        jCheckBox1 = new javax.swing.JCheckBox();
        QuestionName = new javax.swing.JLabel();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jButtonNextQuestion = new javax.swing.JButton();
        ResultOutput = new javax.swing.JDialog();
        jTextFieldProcents = new javax.swing.JTextField();
        jTextFieldMark = new javax.swing.JTextField();
        jButtonEnd = new javax.swing.JButton();
        jLabelProcents = new javax.swing.JLabel();
        jLabelMark = new javax.swing.JLabel();
        jLabelResults = new javax.swing.JLabel();

        jRegisterButton.setText("Reģistrēties");
        jRegisterButton.addActionListener(this::jRegisterButtonActionPerformed);

        jLoginButton.setText("Ielogoties");
        jLoginButton.addActionListener(this::jLoginButtonActionPerformed);

        jProgramNameLabel.setText("Testēšanas sistēma \"E-Testi\"");

        javax.swing.GroupLayout StartChoiceLayout = new javax.swing.GroupLayout(StartChoice.getContentPane());
        StartChoice.getContentPane().setLayout(StartChoiceLayout);
        StartChoiceLayout.setHorizontalGroup(
            StartChoiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, StartChoiceLayout.createSequentialGroup()
                .addContainerGap(139, Short.MAX_VALUE)
                .addGroup(StartChoiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jProgramNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jRegisterButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLoginButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(112, 112, 112))
        );
        StartChoiceLayout.setVerticalGroup(
            StartChoiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(StartChoiceLayout.createSequentialGroup()
                .addGap(95, 95, 95)
                .addComponent(jProgramNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRegisterButton, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLoginButton, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(70, Short.MAX_VALUE))
        );

        jLoginButton1.setText("Ielogoties");
        jLoginButton1.addActionListener(this::jLoginButton1ActionPerformed);

        jRegisterButton1.setText("Reģistrēties");
        jRegisterButton1.addActionListener(this::jRegisterButton1ActionPerformed);

        jUsernameLabel1.setText("Lietotājvārds:");

        jUsernameField1.addActionListener(this::jUsernameField1ActionPerformed);

        jPasswordField1.addActionListener(this::jPasswordField1ActionPerformed);

        jPasswordLabel1.setText("Parole:");

        javax.swing.GroupLayout LoginOrRegisterLayout = new javax.swing.GroupLayout(LoginOrRegister.getContentPane());
        LoginOrRegister.getContentPane().setLayout(LoginOrRegisterLayout);
        LoginOrRegisterLayout.setHorizontalGroup(
            LoginOrRegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LoginOrRegisterLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(LoginOrRegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(LoginOrRegisterLayout.createSequentialGroup()
                        .addGroup(LoginOrRegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLoginButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jRegisterButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(LoginOrRegisterLayout.createSequentialGroup()
                        .addGroup(LoginOrRegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jUsernameLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
                            .addComponent(jPasswordLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(LoginOrRegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jUsernameField1, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(17, Short.MAX_VALUE))))
        );
        LoginOrRegisterLayout.setVerticalGroup(
            LoginOrRegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LoginOrRegisterLayout.createSequentialGroup()
                .addGap(62, 62, 62)
                .addGroup(LoginOrRegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jUsernameLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jUsernameField1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(LoginOrRegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPasswordLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(7, 7, 7)
                .addComponent(jLoginButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRegisterButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );

        jRegisterButton2.setText("Reģistrēties");
        jRegisterButton2.addActionListener(this::jRegisterButton2ActionPerformed);

        jNameLabel.setText("Vārds:");

        jNameField.addActionListener(this::jNameFieldActionPerformed);

        jSurnameLabel.setText("Uzvārds:");

        jSurnameField.addActionListener(this::jSurnameFieldActionPerformed);

        jUsernameLabel2.setText("Lietotājvārds:");

        jUsernameField2.addActionListener(this::jUsernameField2ActionPerformed);

        jPasswordLabel2.setText("Parole:");

        jPasswordField2.addActionListener(this::jPasswordField22ActionPerformed);

        jRepeatPasswordLabel.setText("Apstriprināt paroli:");

        jRepeatPasswordField3.addActionListener(this::jRepeatPasswordField3ActionPerformed);

        javax.swing.GroupLayout RegisterLayout = new javax.swing.GroupLayout(Register.getContentPane());
        Register.getContentPane().setLayout(RegisterLayout);
        RegisterLayout.setHorizontalGroup(
            RegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RegisterLayout.createSequentialGroup()
                .addGroup(RegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(RegisterLayout.createSequentialGroup()
                        .addGap(68, 68, 68)
                        .addGroup(RegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jRepeatPasswordLabel)
                            .addComponent(jSurnameLabel)
                            .addComponent(jNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jUsernameLabel2)
                            .addComponent(jPasswordLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(RegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPasswordField2)
                            .addComponent(jNameField)
                            .addComponent(jSurnameField)
                            .addComponent(jUsernameField2)
                            .addComponent(jRepeatPasswordField3, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(RegisterLayout.createSequentialGroup()
                        .addGap(130, 130, 130)
                        .addComponent(jRegisterButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(94, Short.MAX_VALUE))
        );
        RegisterLayout.setVerticalGroup(
            RegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RegisterLayout.createSequentialGroup()
                .addGap(63, 63, 63)
                .addGroup(RegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(RegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSurnameLabel)
                    .addComponent(jSurnameField, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(RegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jUsernameLabel2)
                    .addComponent(jUsernameField2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(RegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPasswordField2, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(jPasswordLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(RegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRepeatPasswordField3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRepeatPasswordLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRegisterButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButtonStartTest2.setText("Sākt testu");
        jButtonStartTest2.addActionListener(this::jButtonStartTest2ActionPerformed);

        jLabelTestName.setText("Tests „Nosaukums”");

        javax.swing.GroupLayout StartTestLayout = new javax.swing.GroupLayout(StartTest.getContentPane());
        StartTest.getContentPane().setLayout(StartTestLayout);
        StartTestLayout.setHorizontalGroup(
            StartTestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(StartTestLayout.createSequentialGroup()
                .addGap(124, 124, 124)
                .addGroup(StartTestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelTestName, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonStartTest2, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(132, Short.MAX_VALUE))
        );
        StartTestLayout.setVerticalGroup(
            StartTestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, StartTestLayout.createSequentialGroup()
                .addContainerGap(128, Short.MAX_VALUE)
                .addComponent(jLabelTestName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonStartTest2, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(106, 106, 106))
        );

        jCheckBox1.setText("1. atbilde");
        jCheckBox1.addActionListener(this::jCheckBox1ActionPerformed);

        QuestionName.setText("1. jautājums „Jautājuma teksts”");

        jCheckBox2.setText("2. atbilde");
        jCheckBox2.addActionListener(this::jCheckBox2ActionPerformed);

        jCheckBox3.setText("3. atbilde");
        jCheckBox3.addActionListener(this::jCheckBox3ActionPerformed);

        jButtonNextQuestion.setText("Tālāk");
        jButtonNextQuestion.addActionListener(this::jButtonNextQuestionActionPerformed);

        javax.swing.GroupLayout ChoiceBetweenAnswersLayout = new javax.swing.GroupLayout(ChoiceBetweenAnswers.getContentPane());
        ChoiceBetweenAnswers.getContentPane().setLayout(ChoiceBetweenAnswersLayout);
        ChoiceBetweenAnswersLayout.setHorizontalGroup(
            ChoiceBetweenAnswersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ChoiceBetweenAnswersLayout.createSequentialGroup()
                .addGroup(ChoiceBetweenAnswersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ChoiceBetweenAnswersLayout.createSequentialGroup()
                        .addGap(83, 83, 83)
                        .addGroup(ChoiceBetweenAnswersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox3)
                            .addComponent(jCheckBox2)
                            .addComponent(QuestionName)
                            .addComponent(jCheckBox1)))
                    .addGroup(ChoiceBetweenAnswersLayout.createSequentialGroup()
                        .addGap(104, 104, 104)
                        .addComponent(jButtonNextQuestion, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(155, Short.MAX_VALUE))
        );
        ChoiceBetweenAnswersLayout.setVerticalGroup(
            ChoiceBetweenAnswersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ChoiceBetweenAnswersLayout.createSequentialGroup()
                .addGap(86, 86, 86)
                .addComponent(QuestionName, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonNextQuestion, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(47, Short.MAX_VALUE))
        );

        jTextFieldProcents.addActionListener(this::jTextFieldProcentsActionPerformed);

        jTextFieldMark.addActionListener(this::jTextFieldMarkActionPerformed);

        jButtonEnd.setText("Beigt");
        jButtonEnd.addActionListener(this::jButtonEndActionPerformed);

        jLabelProcents.setText("Procenti:");

        jLabelMark.setText("Atzīme:");

        jLabelResults.setText("Rezultāti");

        javax.swing.GroupLayout ResultOutputLayout = new javax.swing.GroupLayout(ResultOutput.getContentPane());
        ResultOutput.getContentPane().setLayout(ResultOutputLayout);
        ResultOutputLayout.setHorizontalGroup(
            ResultOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ResultOutputLayout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(ResultOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(ResultOutputLayout.createSequentialGroup()
                        .addComponent(jLabelProcents)
                        .addGap(18, 18, 18)
                        .addComponent(jTextFieldProcents))
                    .addGroup(ResultOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jTextFieldMark, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(ResultOutputLayout.createSequentialGroup()
                            .addComponent(jLabelMark)
                            .addGap(55, 55, 55)
                            .addComponent(jLabelResults)
                            .addGap(44, 44, 44))
                        .addComponent(jButtonEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(167, Short.MAX_VALUE))
        );
        ResultOutputLayout.setVerticalGroup(
            ResultOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ResultOutputLayout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addComponent(jLabelResults)
                .addGroup(ResultOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ResultOutputLayout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jLabelProcents))
                    .addGroup(ResultOutputLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextFieldProcents, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(ResultOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ResultOutputLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabelMark))
                    .addGroup(ResultOutputLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldMark, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(52, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jRegisterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRegisterButtonActionPerformed
    StartChoice.dispose();
        Register.setSize(500, 500);
        Register.setModal(true);
        Register.setLocationRelativeTo(null);
        Register.setVisible(true);
    }//GEN-LAST:event_jRegisterButtonActionPerformed

    private void jLoginButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLoginButtonActionPerformed
    StartChoice.dispose();
        LoginOrRegister.setSize(500, 500);
        LoginOrRegister.setModal(true);
        LoginOrRegister.setLocationRelativeTo(null);
        LoginOrRegister.setVisible(true);
    }//GEN-LAST:event_jLoginButtonActionPerformed

    private void jRegisterButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRegisterButton2ActionPerformed
    String vards = jNameField.getText();
    String uzvards = jSurnameField.getText();
    String lietotajvards = jUsernameField2.getText();
    String parole = jPasswordField2.getText();
    String parole2 = jRepeatPasswordField3.getText();

    if (vards.isEmpty() || uzvards.isEmpty() || lietotajvards.isEmpty() ||
            parole.isEmpty() || parole2.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Lūdzu, aizpildiet visus laukus!");
        return;
    }

    if (!parole.equals(parole2)) {
        JOptionPane.showMessageDialog(this, "Paroles nesakrīt!");
        return;
    }

    if (parole.length() < 6) {
        JOptionPane.showMessageDialog(this, "Parolei jābūt vismaz 6 simboliem!");
        return;
    }

    // PIRMS DATUBĀZES VAICĀJUMA, PĀRBAUDIET SAVIENOJUMU
    if (connection == null) {
        JOptionPane.showMessageDialog(this, 
            "Nav savienojuma ar datu bāzi! Lūdzu restartējiet programmu.",
            "Savienojuma kļūda",
            JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        // Pārbauda, vai lietotājvārds jau eksistē datu bāzē
        String checkUserSQL = "SELECT COUNT(*) FROM users WHERE username = ?";
        PreparedStatement checkStmt = connection.prepareStatement(checkUserSQL);
        checkStmt.setString(1, lietotajvards);
        ResultSet rs = checkStmt.executeQuery();
        
        if (rs.next() && rs.getInt(1) > 0) {
            JOptionPane.showMessageDialog(this, "Šāds lietotājvārds jau eksistē!");
            return;
        }
        
        // Ievieto jaunu lietotāju datu bāzē
        String insertSQL = "INSERT INTO users (username, password, first_name, last_name, role) VALUES (?, ?, ?, ?, 'STUDENT')";
        PreparedStatement pstmt = connection.prepareStatement(insertSQL);
        pstmt.setString(1, lietotajvards);
        pstmt.setString(2, parole);
        pstmt.setString(3, vards);
        pstmt.setString(4, uzvards);
        
        int rowsAffected = pstmt.executeUpdate();
        
        if (rowsAffected > 0) {
            JOptionPane.showMessageDialog(this, "Reģistrācija veiksmīga!");
            
            // Izveido lokālo Student objektu
            currentStudent = new Student(vards, lietotajvards, parole);
            
            // Aizver reģistrācijas logu
            Register.dispose();
            
            // Atver ielogošanās logu
            LoginOrRegister.setModal(true);
            LoginOrRegister.setSize(500, 500);
            LoginOrRegister.setLocationRelativeTo(this);
            LoginOrRegister.setVisible(true);
        }
        
        // Aizverat PreparedStatement
        rs.close();
        checkStmt.close();
        pstmt.close();
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, 
            "Kļūda reģistrējoties:\n" + e.getMessage(), 
            "Datu bāzes kļūda", 
            JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
    }//GEN-LAST:event_jRegisterButton2ActionPerformed

    private void jNameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jNameFieldActionPerformed
    jSurnameField.requestFocus();
    }//GEN-LAST:event_jNameFieldActionPerformed

    private void jSurnameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSurnameFieldActionPerformed
    jUsernameField2.requestFocus();
    }//GEN-LAST:event_jSurnameFieldActionPerformed

    private void jUsernameField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jUsernameField2ActionPerformed
    jPasswordField2.requestFocus();
    }//GEN-LAST:event_jUsernameField2ActionPerformed

    private void jPasswordField22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPasswordField22ActionPerformed
    jRepeatPasswordField3.requestFocus();
    }//GEN-LAST:event_jPasswordField22ActionPerformed

    private void jRepeatPasswordField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRepeatPasswordField3ActionPerformed
    jRegisterButton2ActionPerformed(evt);
    }//GEN-LAST:event_jRepeatPasswordField3ActionPerformed

    private void jLoginButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLoginButton1ActionPerformed
    try {
        String login = jUsernameField1.getText();
        String password = jPasswordField1.getText();

        if (login.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lūdzu aizpildiet visus laukus!");
            return;
        }

        // Pārbauda vai ir savienojums ar datubāzi
        if (!ensureConnection()) {
            JOptionPane.showMessageDialog(this, 
                "Nevar izveidot savienojumu ar datu bāzi!\n" +
                "Lūdzu restartējiet programmu vai pārbaudiet savienojumu.",
                "Savienojuma kļūda",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            // Pārbauda lietotāju datu bāzē
            String sql = "SELECT first_name, last_name FROM users WHERE username = ? AND password = ?";
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, login);
            pstmt.setString(2, password);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // Lietotājs atrasts
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                
                // Izveido Student objektu lokālajai lietošanai
                currentStudent = new Student(firstName, login, password);
                
                JOptionPane.showMessageDialog(this, 
                    "Ieeja veiksmīga!\nSveicināti, " + firstName + " " + lastName);
                
                // Aizver ielogošanās logu
                LoginOrRegister.dispose();
                
                // Atver testa sākuma logu
                StartTest.setSize(500, 500);
                StartTest.setModal(true);
                StartTest.setLocationRelativeTo(null);
                StartTest.setVisible(true);
                
            } else {
                JOptionPane.showMessageDialog(this, "Nepareizs lietotājvārds vai parole!");
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Kļūda ielogojoties:\n" + e.getMessage() + 
                "\nSQL State: " + e.getSQLState(), 
                "Datu bāzes kļūda", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            // Vienmēr aizver resursus
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, 
            "Negaidīta kļūda:\n" + e.getMessage(), 
            "Sistēmas kļūda", 
            JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
    }//GEN-LAST:event_jLoginButton1ActionPerformed

    private void jRegisterButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRegisterButton1ActionPerformed
    LoginOrRegister.dispose();
    Register.setModal(true);
    Register.setLocationRelativeTo(this);
    Register.setSize(500, 500);
    Register.setVisible(true);
    }//GEN-LAST:event_jRegisterButton1ActionPerformed

    private void jUsernameField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jUsernameField1ActionPerformed
        jPasswordField1.requestFocus();
    }//GEN-LAST:event_jUsernameField1ActionPerformed

    private void jPasswordField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPasswordField1ActionPerformed
        jLoginButton1ActionPerformed(evt);
    }//GEN-LAST:event_jPasswordField1ActionPerformed

    private void jButtonStartTest2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartTest2ActionPerformed
    // Ielādē jautājumus no datu bāzes
        loadQuestionsFromDatabase();
        
        // Pārbauda, vai ir jautājumi
        if (testQuestions.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Nav jautājumu testam!", 
                "Kļūda", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Resetē testa datus
        currentQuestionIndex = 0;
        score = 0;
        selectedAnswer = -1;
        
        // Ielādē pirmo jautājumu
        loadQuestion();
        
        // Aizver sākuma logu un atver jautājumu logu
        StartTest.dispose();
        ChoiceBetweenAnswers.setSize(500, 500);
        ChoiceBetweenAnswers.setModal(true);
        ChoiceBetweenAnswers.setLocationRelativeTo(null);
        ChoiceBetweenAnswers.setVisible(true);
    }
    
    private void loadQuestion() {
        if (currentQuestionIndex >= testQuestions.size()) {
            return;
        }
        
        Question q = testQuestions.get(currentQuestionIndex);
        
        // Parāda jautājumu
        QuestionName.setText((currentQuestionIndex + 1) + ". " + q.getText());
        
        // Parāda atbilžu variantus
        String[] opts = q.getOptions();
        jCheckBox1.setText(opts[0]);
        jCheckBox2.setText(opts[1]);
        jCheckBox3.setText(opts[2]);
        
        // Notīra iepriekšējo izvēli
        selectedAnswer = -1;
        jCheckBox1.setSelected(false);
        jCheckBox2.setSelected(false);
        jCheckBox3.setSelected(false);
    }//GEN-LAST:event_jButtonStartTest2ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
    selectedAnswer = 0;
    jCheckBox2.setSelected(false);
    jCheckBox3.setSelected(false);
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jButtonNextQuestionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNextQuestionActionPerformed
    if (selectedAnswer == -1) {
            JOptionPane.showMessageDialog(this, "Lūdzu, izvēlieties atbildi!");
            return;
        }

        // Paņem pašreizējo jautājumu
        Question q = testQuestions.get(currentQuestionIndex);

        // Pārbauda pareizo atbildi
        if (q.isCorrect(selectedAnswer)) {
            score++;
        }

        currentQuestionIndex++;
        selectedAnswer = -1;

        if (currentQuestionIndex < testQuestions.size()) {
            loadQuestion();
        } else {
            // TESTA BEIGAS
            int total = testQuestions.size();
            int percent = (int) Math.round((score * 100.0) / total);
            int mark = calculateMark(percent);

            jTextFieldProcents.setText(percent + "%");
            jTextFieldMark.setText(String.valueOf(mark));

            // Saglabā rezultātus datu bāzē
            saveTestResultsToDatabase();
            
            // Parāda rezultātus
            ChoiceBetweenAnswers.dispose();
            ResultOutput.setSize(400, 300);
            ResultOutput.setModal(true);
            ResultOutput.setLocationRelativeTo(null);
            ResultOutput.setVisible(true);
        }
    }//GEN-LAST:event_jButtonNextQuestionActionPerformed

    private int calculateMark(int percent) {
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
    
     private void saveTestResultsToDatabase() {
        if (currentStudent == null || connection == null) {
            return;
        }
        
        try {
            // Atrod lietotāja ID
            String findUserSQL = "SELECT id FROM users WHERE username = ?";
            PreparedStatement findUserStmt = connection.prepareStatement(findUserSQL);
            findUserStmt.setString(1, currentStudent.getLogin());
            ResultSet userRs = findUserStmt.executeQuery();
            
            int userId = -1;
            if (userRs.next()) {
                userId = userRs.getInt("id");
            }
            userRs.close();
            findUserStmt.close();
            
            if (userId == -1) {
                System.out.println("⚠ Lietotājs nav atrasts datu bāzē!");
                return;
            }
            
            // Aprēķina rezultātus
            int totalQuestions = testQuestions.size();
            int percent = (int) Math.round((score * 100.0) / totalQuestions);
            int mark = calculateMark(percent);
            
            // Ievieto rezultātu datu bāzē (pieņemot, ka ir test_results tabula)
            String insertResultSQL = "INSERT INTO test_results (user_id, test_id, score, max_score, percentage, grade) " +
                                    "VALUES (?, 1, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = connection.prepareStatement(insertResultSQL);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, score);
            pstmt.setInt(3, totalQuestions);
            pstmt.setDouble(4, percent);
            pstmt.setString(5, String.valueOf(mark));
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Testa rezultāts saglabāts datu bāzē");
            }
            
            pstmt.close();
            
        } catch (SQLException e) {
            System.err.println("Kļūda saglabājot rezultātus: " + e.getMessage());
            // Neizmet kļūdu lietotājam, jo tas nav kritisks
        }
    }
    
    private void jButtonEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEndActionPerformed
    closeDatabaseConnection();
   System.exit(0);
}

// Pārraksti loga aizvēršanas metodi
@Override
public void dispose() {
    closeDatabaseConnection();
    super.dispose();
    }//GEN-LAST:event_jButtonEndActionPerformed

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
    selectedAnswer = 1;
    jCheckBox1.setSelected(false);
    jCheckBox3.setSelected(false);
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    private void jCheckBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox3ActionPerformed
    selectedAnswer = 2;
    jCheckBox1.setSelected(false);
    jCheckBox2.setSelected(false);
    }//GEN-LAST:event_jCheckBox3ActionPerformed

    private void jTextFieldProcentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldProcentsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldProcentsActionPerformed

    private void jTextFieldMarkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldMarkActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMarkActionPerformed

    // Pievieno jaunu pogu ResultOutput dialogā
private void initializeResultDialog() {
    // Pievieno pogu "Skatīt visus rezultātus"
    JButton viewAllResultsButton = new JButton("Skatīt visus rezultātus");
    viewAllResultsButton.addActionListener(e -> showAllResults());
    
    // Pievieno pogu rezultātu dialoga paneļam
    // (Pievieno šo kodu ResultOutput paneļa inicializācijā)
}

private void showAllResults() {
    if (currentStudent == null || connection == null) {
        JOptionPane.showMessageDialog(this, "Nav pieslēgts lietotājs!");
        return;
    }
    
    try {
        String sql = "SELECT tr.score, tr.max_score, tr.percentage, tr.grade, tr.completed_at, t.title " +
                     "FROM test_results tr " +
                     "JOIN tests t ON tr.test_id = t.id " +
                     "JOIN users u ON tr.user_id = u.id " +
                     "WHERE u.username = ? " +
                     "ORDER BY tr.completed_at DESC";
        
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, currentStudent.getLogin());
        ResultSet rs = pstmt.executeQuery();
        
        StringBuilder results = new StringBuilder();
        results.append("=== JŪSU TESTA REZULTĀTI ===\n\n");
        
        int count = 0;
        while (rs.next()) {
            count++;
            String title = rs.getString("title");
            int score = rs.getInt("score");
            int maxScore = rs.getInt("max_score");
            double percentage = rs.getDouble("percentage");
            String grade = rs.getString("grade");
            Timestamp completedAt = rs.getTimestamp("completed_at");
            
            results.append(count).append(". ").append(title).append("\n");
            results.append("   Rezultāts: ").append(score).append("/").append(maxScore)
                   .append(" (").append(percentage).append("%)\n");
            results.append("   Atzīme: ").append(grade).append("\n");
            results.append("   Datums: ").append(completedAt).append("\n\n");
        }
        
        rs.close();
        pstmt.close();
        
        if (count == 0) {
            results.append("Jums vēl nav pildīti testi.");
        }
        
        // Rāda rezultātus dialoglodziņā
        JTextArea textArea = new JTextArea(results.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Jūsu rezultāti", JOptionPane.INFORMATION_MESSAGE);
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, 
            "Kļūda ielādējot rezultātus:\n" + e.getMessage(), 
            "Kļūda", 
            JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog ChoiceBetweenAnswers;
    private javax.swing.JDialog LoginOrRegister;
    private javax.swing.JLabel QuestionName;
    private javax.swing.JDialog Register;
    private javax.swing.JDialog ResultOutput;
    private javax.swing.JDialog StartChoice;
    private javax.swing.JDialog StartTest;
    private javax.swing.JButton jButtonEnd;
    private javax.swing.JButton jButtonNextQuestion;
    private javax.swing.JButton jButtonStartTest2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JLabel jLabelMark;
    private javax.swing.JLabel jLabelProcents;
    private javax.swing.JLabel jLabelResults;
    private javax.swing.JLabel jLabelTestName;
    private javax.swing.JButton jLoginButton;
    private javax.swing.JButton jLoginButton1;
    private javax.swing.JTextField jNameField;
    private javax.swing.JLabel jNameLabel;
    private javax.swing.JTextField jPasswordField1;
    private javax.swing.JTextField jPasswordField2;
    private javax.swing.JLabel jPasswordLabel1;
    private javax.swing.JLabel jPasswordLabel2;
    private javax.swing.JLabel jProgramNameLabel;
    private javax.swing.JButton jRegisterButton;
    private javax.swing.JButton jRegisterButton1;
    private javax.swing.JButton jRegisterButton2;
    private javax.swing.JTextField jRepeatPasswordField3;
    private javax.swing.JLabel jRepeatPasswordLabel;
    private javax.swing.JTextField jSurnameField;
    private javax.swing.JLabel jSurnameLabel;
    private javax.swing.JTextField jTextFieldMark;
    private javax.swing.JTextField jTextFieldProcents;
    private javax.swing.JTextField jUsernameField1;
    private javax.swing.JTextField jUsernameField2;
    private javax.swing.JLabel jUsernameLabel1;
    private javax.swing.JLabel jUsernameLabel2;
    // End of variables declaration//GEN-END:variables
}
