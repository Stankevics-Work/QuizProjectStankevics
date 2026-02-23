package pd1_stankevics_27;

import javax.swing.*;
import java.sql.*;
import java.util.Properties;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.awt.Dimension;

public class Interface extends javax.swing.JFrame {
    
    private static final String DRIVER = "org.apache.derby.jdbc.ClientDriver";
    private String URL;
    private String USER;
    private String PASSWORD;

    private User currentUser;
    private AuthService authService;
    
    private Connection connection = null;
    private List<Question> testQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int selectedAnswer = -1;
    
    public Interface() {
        initComponents();
        loadConfig();
        
        // SAVIENOJAMIES UZREIZ
        boolean connected = connectToDatabase();
        
        if (!connected) {
            int choice = JOptionPane.showConfirmDialog(this,
                "Nevar izveidot savienojumu ar datu bāzi!\n" +
                "Vai vēlaties turpināt bez datubāzes (tikai testa režīms)?",
                "Savienojuma kļūda",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
                
            if (choice != JOptionPane.YES_OPTION) {
                System.exit(1);
            }
        }
        
        StartChoice.setSize(500, 500);
        StartChoice.setLocationRelativeTo(null);
        StartChoice.setModal(true);
        StartChoice.setVisible(true);
    }
    
    private void loadConfig() {
        try {
            Properties p = new Properties();
            InputStream in = getClass().getResourceAsStream("/pd1_stankevics_27/config.properties");
            
            if (in == null) {
                JOptionPane.showMessageDialog(this,
                    "config.properties NAV ATRASTS!",
                    "Konfigurācijas kļūda",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            p.load(in);
            URL = p.getProperty("db.url");
            USER = p.getProperty("db.user");
            PASSWORD = p.getProperty("db.password");
            
            System.out.println("Config ielādēts:");
            System.out.println("  URL: " + URL);
            System.out.println("  USER: " + USER);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private boolean connectToDatabase() {
    System.out.println("=== SAVIENOJUMU IZVEIDE ===");
    System.out.println("URL: " + URL);
    System.out.println("USER: " + USER);
    
    try {
        // 1. Ielādē draiveri
        Class.forName(DRIVER);
        System.out.println("✅ Draiveris ielādēts: " + DRIVER);
        
        // 2. Savienojuma parametri - tikai lietotājs un parole
        Properties props = new Properties();
        props.setProperty("user", USER);
        props.setProperty("password", PASSWORD);
        // NOŅEMTS: props.setProperty("create", "true"); - jo lietojam esošu datubāzi
        
        // 3. Izveido savienojumu ar esošo datubāzi
        System.out.println("⏳ Mēģinu savienoties ar esošo datubāzi...");
        connection = DriverManager.getConnection(URL, props);
        System.out.println("✅ Savienojums izveidots!");
        
        // 4. Pārbauda savienojumu
        if (connection != null && !connection.isClosed()) {
            System.out.println("✅ Savienojums ir aktīvs");
            
            // 5. Izveido AuthService
            authService = new AuthService(connection);
            System.out.println("✅ AuthService izveidots");
            
            // 6. Pārbauda datubāzes saturu
            checkUsersTable();
            checkTests();
            checkQuestions();
            
            return true;
        } else {
            System.err.println("❌ Savienojums nav aktīvs!");
            return false;
        }
        
    } catch (ClassNotFoundException e) {
        System.err.println("❌ Draiveris nav atrasts: " + e.getMessage());
        showError("Derby draiveris nav atrasts!\n" +
                 "Pievienojiet derbyclient.jar projekta bibliotēkām.");
        return false;
        
    } catch (SQLException e) {
        System.err.println("❌ SQL KĻŪDA:");
        System.err.println("   State: " + e.getSQLState());
        System.err.println("   Kods: " + e.getErrorCode());
        System.err.println("   Ziņojums: " + e.getMessage());
        
        String errorMsg = "Nevar savienoties ar datu bāzi!\n\n";
        
        if (e.getSQLState() != null) {
            switch (e.getSQLState()) {
                case "08001":
                case "08S01":
                    errorMsg += "Nav savienojuma ar Derby serveri!\n" +
                               "Pārliecinieties, ka Derby serveris darbojas:\n" +
                               "startNetworkServer.bat";
                    break;
                    
                case "28000":
                    errorMsg += "Nepareizs lietotājvārds/parole!\n" +
                               "Lietotājs: " + USER + "\n" +
                               "Pārbaudiet config.properties failu!";
                    break;
                    
                case "XJ004":
                    errorMsg += "Datubāze nav atrasta!\n" +
                               "Meklēju: " + URL + "\n" +
                               "Pārliecinieties, ka datubāze eksistē šajā ceļā!";
                    break;
                    
                case "XJ040":
                    errorMsg += "Datubāze jau ir piesaistīta citam savienojumam!";
                    break;
                    
                default:
                    errorMsg += "Kļūda: " + e.getMessage() + "\n" +
                               "SQL State: " + e.getSQLState();
            }
        } else {
            errorMsg += "Kļūda: " + e.getMessage();
        }
        
        showError(errorMsg);
        return false;
        
    } catch (Exception e) {
        System.err.println("❌ Negaidīta kļūda: " + e.getMessage());
        e.printStackTrace();
        showError("Negaidīta kļūda:\n" + e.getMessage());
        return false;
    }
}
    
    private void checkTests() {
    try {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM tests");
        
        if (rs.next()) {
            int count = rs.getInt("count");
            System.out.println("ℹ️ Testu skaits datubāzē: " + count);
            
            if (count > 0) {
                // Parāda testu sarakstu
                ResultSet tests = stmt.executeQuery("SELECT id, title FROM tests");
                System.out.println("Pieejamie testi:");
                while (tests.next()) {
                    System.out.println("  - ID=" + tests.getInt("id") + ": " + tests.getString("title"));
                }
                tests.close();
            } else {
                System.out.println("⚠ Nav neviena testa datubāzē!");
            }
        }
        rs.close();
        stmt.close();
    } catch (SQLException e) {
        System.err.println("Kļūda pārbaudot testus: " + e.getMessage());
    }
}

/**
 * Pārbauda vai jautājumu tabulā ir dati
 */
private void checkQuestions() {
    try {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM questions");
        
        if (rs.next()) {
            int count = rs.getInt("count");
            System.out.println("ℹ️ Jautājumu skaits datubāzē: " + count);
            
            if (count == 0) {
                System.out.println("⚠ Nav jautājumu datubāzē!");
                System.out.println("   Pievienojiet jautājumus caur SQL skriptu");
            }
        }
        rs.close();
        stmt.close();
    } catch (SQLException e) {
        System.err.println("Kļūda pārbaudot jautājumus: " + e.getMessage());
    }
}

/**
 * Parāda kļūdas paziņojumu lietotājam
 */
private void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Kļūda", JOptionPane.ERROR_MESSAGE);
}
    
    private void checkUsersTable() {
        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet rs = meta.getTables(null, null, "USERS", null);
            
            if (rs.next()) {
                System.out.println("✅ Tabula 'users' eksistē");
                
                // Parāda dažus lietotājus
                Statement stmt = connection.createStatement();
                ResultSet users = stmt.executeQuery("SELECT username, first_name, last_name, role FROM users FETCH FIRST 3 ROWS ONLY");
                
                System.out.println("Pirmie 3 lietotāji:");
                while (users.next()) {
                    System.out.println("  - " + users.getString("username") + 
                                      " (" + users.getString("first_name") + " " + 
                                      users.getString("last_name") + ") - " + 
                                      users.getString("role"));
                }
                users.close();
                stmt.close();
            } else {
                System.out.println("⚠ Tabula 'users' NAV atrasta!");
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Kļūda pārbaudot tabulu: " + e.getMessage());
        }
    }
    
    private void loadQuestionsFromDatabase() {
    testQuestions.clear();
    
    if (connection == null) {
        System.err.println("Nav savienojuma ar datubāzi!");
        return;
    }
    
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
                     "WHERE q.test_id = 1 " +  // Pieņemot, ka tests ar ID=1
                     "ORDER BY q.id";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            int count = 0;
            while (rs.next()) {
                String questionText = rs.getString("question_text");
                String opt1 = rs.getString("opt1");
                String opt2 = rs.getString("opt2");
                String opt3 = rs.getString("opt3");
                int correctIndex = rs.getInt("correct_index");
                
                if (questionText != null && opt1 != null && opt2 != null && opt3 != null) {
                    Question q = new Question(questionText, opt1, opt2, opt3, correctIndex);
                    testQuestions.add(q);
                    count++;
                }
            }
            
            System.out.println("✅ Ielādēti " + count + " jautājumi no datubāzes");
            
            if (count == 0) {
                // Ja nav jautājumu, pievieno pagaidu testa jautājumus
                addDefaultQuestions();
            }
        }
        
    } catch (SQLException e) {
        System.err.println("❌ Kļūda ielādējot jautājumus: " + e.getMessage());
        e.printStackTrace();
        // Pievieno pagaidu jautājumus kļūdas gadījumā
        addDefaultQuestions();
    }
}

private void addDefaultQuestions() {
    System.out.println("⚠ Izmantoju pagaidu testa jautājumus");
    testQuestions.add(new Question("Kas ir Java?", "Programmēšanas valoda", "Operētājsistēma", "Datu bāze", 0));
    testQuestions.add(new Question("Kas ir SQL?", "Datu bāzes valoda", "Programmēšanas valoda", "Operētājsistēma", 0));
    testQuestions.add(new Question("Kas ir OOP?", "Objektorientēta programmēšana", "Funkcionālā programmēšana", "Procedurālā programmēšana", 0));
}
    
    private void closeDatabaseConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 Savienojums ar datu bāzi aizvērts");
            }
        } catch (SQLException e) {
            System.err.println("Kļūda aizverot savienojumu: " + e.getMessage());
        }
    }
    
    private void closeAllWindows() {
    System.out.println("Aizveru visus logus...");
    
    if (StartChoice != null && StartChoice.isVisible()) StartChoice.dispose();
    if (LoginOrRegister != null && LoginOrRegister.isVisible()) LoginOrRegister.dispose();
    if (Register != null && Register.isVisible()) Register.dispose();
    if (StartTest != null && StartTest.isVisible()) StartTest.dispose();
    if (ChoiceBetweenAnswers != null && ChoiceBetweenAnswers.isVisible()) ChoiceBetweenAnswers.dispose();
    if (ResultOutput != null && ResultOutput.isVisible()) ResultOutput.dispose();
    
    dispose();
}
    
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
    if (authService == null) {
            JOptionPane.showMessageDialog(this, 
                "Nav savienojuma ar datu bāzi!", 
                "Kļūda", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String vards = jNameField.getText().trim();
        String uzvards = jSurnameField.getText().trim();
        String lietotajvards = jUsernameField2.getText().trim();
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

        try {
            currentUser = authService.registerStudent(vards, uzvards, lietotajvards, parole);
            
            JOptionPane.showMessageDialog(this, 
                "Reģistrācija veiksmīga!\nSveicināts, " + currentUser.getName() + "!",
                "Veiksme",
                JOptionPane.INFORMATION_MESSAGE);
            
            Register.dispose();
            
            LoginOrRegister.setModal(true);
            LoginOrRegister.setSize(500, 500);
            LoginOrRegister.setLocationRelativeTo(this);
            LoginOrRegister.setVisible(true);
            
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Kļūda", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Datubāzes kļūda:\n" + e.getMessage(), 
                "Kļūda", 
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
    if (authService == null) {
            JOptionPane.showMessageDialog(this, 
                "Nav savienojuma ar datu bāzi!", 
                "Kļūda", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String login = jUsernameField1.getText().trim();
        String password = jPasswordField1.getText();

        if (login.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lūdzu aizpildiet visus laukus!");
            return;
        }

        try {
            currentUser = authService.login(login, password);
            
            String userType = "";
            if (currentUser instanceof Student) userType = "Students";
            else if (currentUser instanceof Teacher) userType = "Skolotājs";
            else if (currentUser instanceof Admin) userType = "Administrators";
            
            JOptionPane.showMessageDialog(this, 
                "Ieeja veiksmīga!\nSveicināts, " + currentUser.getName() + " (" + userType + ")",
                "Veiksme",
                JOptionPane.INFORMATION_MESSAGE);
            
            LoginOrRegister.dispose();
            
            if (currentUser instanceof Student) {
                StartTest.setSize(500, 500);
                StartTest.setModal(true);
                StartTest.setLocationRelativeTo(null);
                StartTest.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Šī lietotāja tipa funkcionalitāte tiks pievienota vēlāk.",
                    "Informācija",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Kļūda", JOptionPane.ERROR_MESSAGE);
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Kļūda", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Datubāzes kļūda:\n" + e.getMessage(), "Kļūda", JOptionPane.ERROR_MESSAGE);
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
    loadQuestionsFromDatabase();
        
        if (testQuestions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nav jautājumu testam!", "Kļūda", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        currentQuestionIndex = 0;
        score = 0;
        selectedAnswer = -1;
        
        loadQuestion();
        
        StartTest.dispose();
        ChoiceBetweenAnswers.setSize(500, 500);
        ChoiceBetweenAnswers.setModal(true);
        ChoiceBetweenAnswers.setLocationRelativeTo(null);
        ChoiceBetweenAnswers.setVisible(true);
    }
    
    private void loadQuestion() {
        if (currentQuestionIndex >= testQuestions.size()) return;
        
        Question q = testQuestions.get(currentQuestionIndex);
        QuestionName.setText((currentQuestionIndex + 1) + ". " + q.getText());
        
        String[] opts = q.getOptions();
        jCheckBox1.setText(opts[0]);
        jCheckBox2.setText(opts[1]);
        jCheckBox3.setText(opts[2]);
        
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

        Question q = testQuestions.get(currentQuestionIndex);
        if (q.isCorrect(selectedAnswer)) score++;

        currentQuestionIndex++;
        selectedAnswer = -1;

        if (currentQuestionIndex < testQuestions.size()) {
            loadQuestion();
        } else {
            int total = testQuestions.size();
            int percent = (int) Math.round((score * 100.0) / total);
            int mark = calculateMark(percent);

            jTextFieldProcents.setText(percent + "%");
            jTextFieldMark.setText(String.valueOf(mark));

            saveTestResultsToDatabase();
            
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
        if (currentUser == null || connection == null || !(currentUser instanceof Student)) return;
        
        try {
            String findUserSQL = "SELECT id FROM users WHERE username = ?";
            PreparedStatement findUserStmt = connection.prepareStatement(findUserSQL);
            findUserStmt.setString(1, currentUser.getLogin());
            ResultSet userRs = findUserStmt.executeQuery();
            
            int userId = -1;
            if (userRs.next()) userId = userRs.getInt("id");
            userRs.close();
            findUserStmt.close();
            
            if (userId == -1) return;
            
            int totalQuestions = testQuestions.size();
            int percent = (int) Math.round((score * 100.0) / totalQuestions);
            
            String insertResultSQL = "INSERT INTO test_results (user_id, test_id, score, max_score, percentage) VALUES (?, 1, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(insertResultSQL);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, score);
            pstmt.setInt(3, totalQuestions);
            pstmt.setDouble(4, percent);
            pstmt.executeUpdate();
            pstmt.close();
            
            System.out.println("✅ Testa rezultāts saglabāts");
            
        } catch (SQLException e) {
            System.err.println("Kļūda saglabājot rezultātus: " + e.getMessage());
        }
    }
    
    private void jButtonEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEndActionPerformed
    closeDatabaseConnection();
    closeAllWindows();  // <-- PIEVIENOT ŠO RINDU
    System.exit(0);     // <-- ŠAI JĀBŪT PĒDĒJAI
}

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
    if (currentUser == null || connection == null) {
        JOptionPane.showMessageDialog(this, "Nav pieslēgts lietotājs!");
        return;
    }
    
    try {
        String sql = "SELECT tr.score, tr.max_score, tr.percentage, tr.completed_at, t.title " +
                     "FROM test_results tr " +
                     "JOIN tests t ON tr.test_id = t.id " +
                     "JOIN users u ON tr.user_id = u.id " +
                     "WHERE u.login = ? " +
                     "ORDER BY tr.completed_at DESC";
        
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, currentUser.getLogin());
        ResultSet rs = pstmt.executeQuery();
        
        StringBuilder results = new StringBuilder();
        results.append("=== ").append(currentUser.getName()).append(" TESTA REZULTĀTI ===\n\n");
        
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

private Student getCurrentStudent() {
    if (currentUser instanceof Student) {
        return (Student) currentUser;
    }
    return null;
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
