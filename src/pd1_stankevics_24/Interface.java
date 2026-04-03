/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package pd1_stankevics_24;

import java.time.LocalDateTime;
import javax.swing.*;
import java.sql.*;
import java.util.Properties;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

/**
 *
 * @author armins.stankevics_24
 */
public class Interface extends javax.swing.JFrame {
    
    private static final String DRIVER = "org.apache.derby.jdbc.ClientDriver";
    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    private User currentUser;
    private AuthService authService;
    
    private Connection connection = null;
    private Test currentTest;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int selectedAnswer = -1;
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Interface.class.getName());

    /**
     * Creates new form NewJFrame
     */
    public Interface() {
    initComponents();
    addActionListeners();
    loadConfig();
    
    // PIEVIENOT ŠO RINDU - galvenā loga nosaukums
    this.setTitle("E-Testi - Testēšanas sistēma");
    
    boolean connected = connectToDatabase();
    
    if (!connected) {
        int choice = JOptionPane.showConfirmDialog(this,
            "Nevar izveidot savienojumu ar datu bāzi!\n" +
            "Vai vēlaties turpināt bez datubāzes (tikai testa režīms)?",
            "Savienojuma kļūda",  // Šo var atstāt
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (choice != JOptionPane.YES_OPTION) {
            System.exit(1);
        }
    }
    
    // PIEVIENOT ŠO RINDU - StartChoice loga nosaukums
    StartChoice.setTitle("E-Testi - Izvēlne");
    StartChoice.setSize(500, 500);
    StartChoice.setLocationRelativeTo(null);
    StartChoice.setModal(true);
    StartChoice.setVisible(true);
}
    
    /**
     * Pievieno notikumu apstrādātājus visām pogām.
     */
    private void addActionListeners() {
        // Notikumu apstrādātāji jau ir pievienoti caur initComponents ar lambda izteiksmēm
        // Papildus ActionListener pievienošana nav nepieciešama, jo tie jau ir definēti
    }
    
    /**
     * Ielādē konfigurācijas parametrus no config.properties faila.
     * Nolasa datubāzes URL, lietotājvārdu un paroli.
     */
    private void loadConfig() {
        try {
            Properties p = new Properties();
            InputStream in = getClass().getResourceAsStream("/pd1_stankevics_24/config.properties");
            
            if (in == null) {
                in = getClass().getResourceAsStream("config.properties");
                if (in == null) {
                    JOptionPane.showMessageDialog(this,
                        "config.properties NAV ATRASTS!\n" +
                        "Pārliecinieties, ka fails atrodas resources mapē.",
                        "Konfigurācijas kļūda",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            p.load(in);
            dbUrl = p.getProperty("db.url");
            dbUser = p.getProperty("db.user");
            dbPassword = p.getProperty("db.password");
            
            System.out.println("Config ielādēts:");
            System.out.println("  URL: " + dbUrl);
            System.out.println("  USER: " + dbUser);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Kļūda ielādējot config.properties:\n" + e.getMessage(),
                "Konfigurācijas kļūda",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Izveido savienojumu ar Derby datubāzi un pārbauda tās saturu.
     * 
     * @return true ja savienojums veiksmīgi izveidots, false pretējā gadījumā
     */
    private boolean connectToDatabase() {
        System.out.println("=== SAVIENOJUMA IZVEIDE ===");
        System.out.println("URL: " + dbUrl);
        System.out.println("USER: " + dbUser);
        
        if (dbUrl == null || dbUser == null) {
            System.err.println("❌ Trūkst datubāzes konfigurācijas!");
            return false;
        }
        
        try {
            Class.forName(DRIVER);
            System.out.println("✅ Draiveris ielādēts: " + DRIVER);
            
            Properties props = new Properties();
            props.setProperty("user", dbUser);
            props.setProperty("password", dbPassword);
            
            System.out.println("⏳ Mēģinu savienoties ar datubāzi...");
            connection = DriverManager.getConnection(dbUrl, props);
            System.out.println("✅ Savienojums izveidots!");
            
            if (connection != null && !connection.isClosed()) {
                System.out.println("✅ Savienojums ir aktīvs");
                
                DatabaseMetaData metaData = connection.getMetaData();
                System.out.println("Datubāze: " + metaData.getDatabaseProductName() + 
                                   " " + metaData.getDatabaseProductVersion());
                System.out.println("Draiveris: " + metaData.getDriverName() + 
                                   " " + metaData.getDriverVersion());
                
                boolean tablesExist = checkDatabaseTables();
                
                if (tablesExist) {
                    displayDatabaseContent();
                    authService = new AuthService(connection);
                    System.out.println("✅ AuthService izveidots");
                    return true;
                } else {
                    System.err.println("❌ Datubāzē trūkst nepieciešamo tabulu!");
                    System.err.println("⚠ Lūdzu, izpildiet SQL skriptu, lai izveidotu tabulas.");
                    return false;
                }
            } else {
                System.err.println("❌ Savienojums nav aktīvs!");
                return false;
            }
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Draiveris nav atrasts: " + e.getMessage());
            showError("Derby draiveris nav atrasts!\n" +
                     "Pievienojiet derbyclient.jar projekta bibliotēkām.\n\n" +
                     "Ja izmantojat Maven, pārliecinieties, ka dependencies ir pievienotas.");
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
                                   "startNetworkServer.bat vai java -jar derbyrun.jar server start";
                        break;
                    case "28000":
                        errorMsg += "Nepareizs lietotājvārds/parole!\n" +
                                   "Lietotājs: " + dbUser + "\n" +
                                   "Pārbaudiet config.properties failu!";
                        break;
                    case "XJ004":
                        errorMsg += "Datubāze nav atrasta!\n" +
                                   "Meklēju: " + dbUrl + "\n" +
                                   "Pārliecinieties, ka datubāze eksistē šajā ceļā!\n" +
                                   "Lai izveidotu datubāzi, palaidiet:\n" +
                                   "ij> CONNECT '" + dbUrl + "';\n" +
                                   "ij> CREATE DATABASE QuizDB;";
                        break;
                    default:
                        errorMsg += "Kļūda: " + e.getMessage();
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
    
    /**
     * Pārbauda vai datubāzē eksistē nepieciešamās tabulas.
     */
    private boolean checkDatabaseTables() {
        String[] requiredTables = {"USERS", "TESTS", "QUESTIONS", "ANSWER_OPTIONS"};
        boolean allExist = true;
        
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            System.out.println("=== TABULU PĀRBAUDE ===");
            
            for (String tableName : requiredTables) {
                boolean exists = false;
                try (ResultSet tables = metaData.getTables(null, null, tableName, null)) {
                    exists = tables.next();
                }
                
                if (exists) {
                    System.out.println("✅ Tabula '" + tableName + "' eksistē");
                } else {
                    System.err.println("❌ Tabula '" + tableName + "' NAV atrasta!");
                    allExist = false;
                }
            }
            System.out.println("=========================");
            return allExist;
            
        } catch (SQLException e) {
            System.err.println("Kļūda pārbaudot tabulas: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Parāda datubāzes saturu - ierakstu skaitu tabulās.
     */
    private void displayDatabaseContent() {
        try (Statement stmt = connection.createStatement()) {
            System.out.println("=== DATUBĀZES SATURS ===");
            
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users");
            if (rs.next()) {
                System.out.println("Lietotāji (users): " + rs.getInt("count") + " ieraksti");
            }
            rs.close();
            
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM tests");
            if (rs.next()) {
                System.out.println("Testi (tests): " + rs.getInt("count") + " ieraksti");
            }
            rs.close();
            
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM questions");
            if (rs.next()) {
                System.out.println("Jautājumi (questions): " + rs.getInt("count") + " ieraksti");
            }
            rs.close();
            
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM answer_options");
            if (rs.next()) {
                System.out.println("Atbilžu varianti (answer_options): " + rs.getInt("count") + " ieraksti");
            }
            rs.close();
            
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM test_results");
            if (rs.next()) {
                System.out.println("Testu rezultāti (test_results): " + rs.getInt("count") + " ieraksti");
            }
            rs.close();
            
            System.out.println("==========================");
            
        } catch (SQLException e) {
            System.err.println("Kļūda attēlojot datubāzes saturu: " + e.getMessage());
        }
    }
    
    /**
     * Parāda kļūdas paziņojumu lietotājam.
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Kļūda", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Aizver savienojumu ar datubāzi.
     */
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
    
    /**
     * Aizver visus atvērtos dialoga logus un atbrīvo resursus.
     */
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
    
    // ==================== NOTIKUMU APSTRĀDĀTĀJI ====================
    
    /**
     * Apstrādā lietotāja ielogošanos atbilstoši lomai.
     */
    private void handleUserLogin() {
        if (currentUser instanceof Student) {
            openStudentTestWindow();
            Student student = (Student) currentUser;
            List<Result> results = student.loadResultsFromDatabase(connection);
            if (!results.isEmpty()) {
                System.out.println("\n" + student.getResultsReport());
            }
        } else if (currentUser instanceof Teacher) {
            JOptionPane.showMessageDialog(this, 
                "Sveicināts, pasniedzēj!\nPasniedzēja funkcionalitāte tiks pievienota drīzumā.",
                "Informācija", JOptionPane.INFORMATION_MESSAGE);
        } else if (currentUser instanceof Admin) {
            JOptionPane.showMessageDialog(this, 
                "Sveicināts, administrator!\nAdministratora funkcionalitāte tiks pievienota drīzumā.",
                "Informācija", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Nezināms lietotāja tips!", "Kļūda", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Atver studenta testa logu.
     */
    private void openStudentTestWindow() {
        StartTest.setTitle("E-Testi - Testa izvēle");  // PIEVIENOT ŠO
        StartTest.setSize(500, 500);
        StartTest.setModal(true);
        StartTest.setLocationRelativeTo(null);
        StartTest.setVisible(true);
    }
    
    /**
     * Ielādē un attēlo nākamo jautājumu testā.
     */
    private void loadQuestion() {
        if (currentTest == null || currentTest.getQuestions().isEmpty()) return;
        if (currentQuestionIndex >= currentTest.getQuestions().size()) return;
        
        Question q = currentTest.getQuestions().get(currentQuestionIndex);
        QuestionName.setText("<html>" + (currentQuestionIndex + 1) + ". " + q.getText() + "</html>");
        
        String[] opts = q.getOptions();
        jCheckBox1.setText(opts[0]);
        jCheckBox2.setText(opts[1]);
        jCheckBox3.setText(opts[2]);
        
        selectedAnswer = -1;
        jCheckBox1.setSelected(false);
        jCheckBox2.setSelected(false);
        jCheckBox3.setSelected(false);
        
        System.out.println("Jautājums " + (currentQuestionIndex + 1) + " no " + currentTest.getQuestions().size());
    }
    
    /**
     * Atgriež lietotāja tipa tekstu.
     * 
     * @return lietotāja tipa apraksts
     */
    private String getUserTypeString() {
        if (currentUser instanceof Student) return "Students";
        if (currentUser instanceof Teacher) return "Skolotājs";
        if (currentUser instanceof Admin) return "Administrators";
        return "Lietotājs";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ResultOutput = new javax.swing.JDialog();
        jTextFieldProcents = new javax.swing.JTextField();
        jTextFieldMark = new javax.swing.JTextField();
        jButtonEnd = new javax.swing.JButton();
        jLabelProcents = new javax.swing.JLabel();
        jLabelMark = new javax.swing.JLabel();
        jLabelResults = new javax.swing.JLabel();
        StartTest = new javax.swing.JDialog();
        jButtonStartTest2 = new javax.swing.JButton();
        jLabelTestName = new javax.swing.JLabel();
        ChoiceBetweenAnswers = new javax.swing.JDialog();
        jCheckBox1 = new javax.swing.JCheckBox();
        QuestionName = new javax.swing.JLabel();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jButtonNextQuestion = new javax.swing.JButton();
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
        LoginOrRegister = new javax.swing.JDialog();
        jLoginButton1 = new javax.swing.JButton();
        jRegisterButton1 = new javax.swing.JButton();
        jUsernameLabel1 = new javax.swing.JLabel();
        jUsernameField1 = new javax.swing.JTextField();
        jPasswordField1 = new javax.swing.JTextField();
        jPasswordLabel1 = new javax.swing.JLabel();
        StartChoice = new javax.swing.JDialog();
        jRegisterButton = new javax.swing.JButton();
        jLoginButton = new javax.swing.JButton();
        jProgramNameLabel = new javax.swing.JLabel();

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

    private void jTextFieldProcentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldProcentsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldProcentsActionPerformed

    private void jTextFieldMarkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldMarkActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMarkActionPerformed

    private void jButtonEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEndActionPerformed
    closeDatabaseConnection();
    closeAllWindows(); 
    System.exit(0);
    }//GEN-LAST:event_jButtonEndActionPerformed

    private void jButtonStartTest2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartTest2ActionPerformed
    if (!(currentUser instanceof Student)) {
        JOptionPane.showMessageDialog(this, "Tikai studenti var pildīt testus!", "Kļūda", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    int testId = 1;
    String testTitle = "Java pamatu tests";
    
    currentTest = new Test(String.valueOf(testId), testTitle, "Vispārīgi", null, LocalDateTime.now());
    
    // Izmanto jauno metodi ar minimālo jautājumu skaitu (10 jautājumi)
    // Ja datubāze nav pieejama vai nav jautājumu, automātiski izmantos noklusējuma jautājumus
    currentTest.loadQuestionsFromDatabase(connection, testId, 10);
    
    if (currentTest.getQuestions().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Nav jautājumu testam!", "Kļūda", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    Student student = (Student) currentUser;
    try {
        student.startTest(currentTest);
    } catch (IllegalArgumentException | IllegalStateException ex) {
        JOptionPane.showMessageDialog(this, "Kļūda sākot testu:\n" + ex.getMessage(), "Kļūda", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    currentQuestionIndex = 0;
    score = 0;
    selectedAnswer = -1;
    loadQuestion();
    
    StartTest.dispose();
    ChoiceBetweenAnswers.setTitle("E-Testi - Tests"); 
    ChoiceBetweenAnswers.setSize(600, 500);
    ChoiceBetweenAnswers.setModal(true);
    ChoiceBetweenAnswers.setLocationRelativeTo(null);
    ChoiceBetweenAnswers.setVisible(true); 
    }//GEN-LAST:event_jButtonStartTest2ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        selectedAnswer = 0;
        jCheckBox2.setSelected(false);
        jCheckBox3.setSelected(false);
    }//GEN-LAST:event_jCheckBox1ActionPerformed

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

    private void jButtonNextQuestionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNextQuestionActionPerformed
      ChoiceBetweenAnswers.dispose();
        if (selectedAnswer == -1) {
            JOptionPane.showMessageDialog(this, "Lūdzu, izvēlieties atbildi!");
            ChoiceBetweenAnswers.setVisible(true);
            return;
        }
        
        if (currentTest == null || !(currentUser instanceof Student)) return;
        
        Student student = (Student) currentUser;
        Question q = currentTest.getQuestions().get(currentQuestionIndex);
        
        boolean testContinues = student.answerQuestion(selectedAnswer);
        
        if (q.isCorrect(selectedAnswer)) {
            score += q.getPoints();
        }
        
        currentQuestionIndex++;
        
        if (testContinues) {
            loadQuestion();
            ChoiceBetweenAnswers.setVisible(true);
        } else {
            try {
                Result result = student.finishTest();
                student.saveTestResultToDatabase(connection);
                
                jTextFieldProcents.setText(String.format("%.1f%%", result.getPercentage()));
                jTextFieldMark.setText(result.getGrade());
                System.out.println(result.getDetailedInfo());
                
                ResultOutput.setTitle("E-Testi - Rezultāti");
                ResultOutput.setSize(500, 400);
                ResultOutput.setModal(true);
                ResultOutput.setLocationRelativeTo(null);
                ResultOutput.setVisible(true);
                
            } catch (IllegalStateException e) {
                JOptionPane.showMessageDialog(this, "Kļūda pabeidzot testu:\n" + e.getMessage(), "Kļūda", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jButtonNextQuestionActionPerformed

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

        try {
            String email = JOptionPane.showInputDialog(this, 
                "Lūdzu, ievadiet savu e-pasta adresi:", 
                "E-pasta adrese", 
                JOptionPane.QUESTION_MESSAGE);
            
            if (email == null || email.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "E-pasta adrese ir obligāta!", 
                    "Kļūda", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            currentUser = authService.registerStudent(vards, uzvards, lietotajvards, parole, email);
            
            JOptionPane.showMessageDialog(this, 
                "Reģistrācija veiksmīga!\nSveicināts, " + currentUser.getName() + "!",
                "Veiksme",
                JOptionPane.INFORMATION_MESSAGE);
            
            Register.dispose();
            LoginOrRegister.setTitle("E-Testi - Autorizācija");
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
            JOptionPane.showMessageDialog(this, "Lūdzu, aizpildiet visus laukus!");
            return;
        }

        try {
            currentUser = authService.login(login, password);
            
            String userType = getUserTypeString();
            
            JOptionPane.showMessageDialog(this, 
                "Ieeja veiksmīga!\nSveicināts, " + currentUser.getName() + " (" + userType + ")",
                "Veiksme",
                JOptionPane.INFORMATION_MESSAGE);
            
            LoginOrRegister.dispose();
            
            handleUserLogin();
            
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Kļūda", JOptionPane.ERROR_MESSAGE);
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Kļūda", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Datubāzes kļūda:\n" + e.getMessage(), 
                "Kļūda", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_jLoginButton1ActionPerformed

    private void jRegisterButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRegisterButton1ActionPerformed
        LoginOrRegister.dispose();
        Register.setModal(true);
        Register.setLocationRelativeTo(this);
        Register.setSize(500, 500);
        Register.setVisible(true);
        
        Register.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                Register.dispose();
                LoginOrRegister.setVisible(true);
            }
        });
    }//GEN-LAST:event_jRegisterButton1ActionPerformed

    private void jUsernameField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jUsernameField1ActionPerformed
        jPasswordField1.requestFocus();
    }//GEN-LAST:event_jUsernameField1ActionPerformed

    private void jPasswordField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPasswordField1ActionPerformed
        jLoginButton1ActionPerformed(evt);
    }//GEN-LAST:event_jPasswordField1ActionPerformed

    private void jRegisterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRegisterButtonActionPerformed
    StartChoice.dispose();
    Register.setTitle("E-Testi - Reģistrācija");  // PIEVIENOT ŠO
    Register.setSize(500, 500);
    Register.setModal(true);
    Register.setLocationRelativeTo(null);
    Register.setVisible(true);
    }//GEN-LAST:event_jRegisterButtonActionPerformed

    private void jLoginButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLoginButtonActionPerformed
    StartChoice.dispose();
    LoginOrRegister.setTitle("E-Testi - Autorizācija");  // PIEVIENOT ŠO
    LoginOrRegister.setSize(500, 500);
    LoginOrRegister.setModal(true);
    LoginOrRegister.setLocationRelativeTo(null);
    LoginOrRegister.setVisible(true);
    }//GEN-LAST:event_jLoginButtonActionPerformed

/**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Interface().setVisible(true));
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
