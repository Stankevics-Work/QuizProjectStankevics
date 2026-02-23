package pd1_stankevics_27;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Autentifikācijas serviss - nodrošina lietotāju reģistrāciju un ielogošanos.
 * Pielāgots darbam ar Jūsu esošo datubāzes struktūru.
 * 
 * @author A. Stankevičs
 * @version 3.0
 */
public class AuthService {
    
    private Connection connection;
    
    public AuthService(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Reģistrē jaunu studentu sistēmā.
     */
    public Student registerStudent(String firstName, String lastName, String username, String password) 
            throws IllegalArgumentException, SQLException {
        
        // Validācija
        validateRegistrationData(firstName, lastName, username, password);
        
        // Pārbauda vai lietotājvārds jau eksistē
        if (isUsernameTaken(username)) {
            throw new IllegalArgumentException("Lietotājvārds '" + username + "' jau ir aizņemts!");
        }
        
        // Saglabā datubāzē
        saveUserToDatabase(firstName, lastName, username, password, "STUDENT");
        
        // Izveido un atgriež Student objektu (glabā pilno vārdu)
        String fullName = firstName + " " + lastName;
        return new Student(fullName, username, password);
    }
    
    /**
     * Ielogošanās sistēmā.
     */
    public User login(String username, String password) 
            throws IllegalArgumentException, SecurityException, SQLException {
        
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Lūdzu ievadiet lietotājvārdu!");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Lūdzu ievadiet paroli!");
        }
        
        // Meklē lietotāju pēc username un password
        String sql = "SELECT first_name, last_name, role FROM users WHERE username = ? AND password = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username.trim());
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    String fullName = firstName + " " + lastName;
                    String role = rs.getString("role");
                    
                    return createUserByRole(fullName, username, password, role);
                } else {
                    throw new SecurityException("Nepareizs lietotājvārds vai parole!");
                }
            }
        }
    }
    
    /**
     * Pārbauda vai lietotājvārds jau eksistē.
     */
    public boolean isUsernameTaken(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    
    /**
 * Pārbauda vai tabula eksistē un parāda tās struktūru
 */
public void debugDatabaseStructure() {
    try {
        System.out.println("=== DATUBĀZES DEBUG INFO ===");
        
        // Pārbauda vai tabula eksistē
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet tables = meta.getTables(null, null, "USERS", null);
        if (tables.next()) {
            System.out.println("✅ Tabula 'users' eksistē");
        } else {
            System.out.println("❌ Tabula 'users' NAV atrasta!");
            return;
        }
        
        // Parāda tabulas kolonnas
        System.out.println("Tabulas 'users' kolonnas:");
        ResultSet columns = meta.getColumns(null, null, "USERS", null);
        while (columns.next()) {
            String colName = columns.getString("COLUMN_NAME");
            String colType = columns.getString("TYPE_NAME");
            System.out.println("  - " + colName + " (" + colType + ")");
        }
        
        // Parāda dažus ierakstus
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM users FETCH FIRST 3 ROWS ONLY");
        System.out.println("Pirmie 3 lietotāji:");
        while (rs.next()) {
            System.out.println("  ID: " + rs.getInt("id"));
            System.out.println("    username: " + rs.getString("username"));
            System.out.println("    password: " + rs.getString("password"));
            System.out.println("    first_name: " + rs.getString("first_name"));
            System.out.println("    last_name: " + rs.getString("last_name"));
            System.out.println("    role: " + rs.getString("role"));
        }
        
        System.out.println("=============================");
        
    } catch (SQLException e) {
        System.err.println("Kļūda debugojot datubāzi: " + e.getMessage());
        e.printStackTrace();
    }
}
    
    /**
     * Validē reģistrācijas datus.
     */
    private void validateRegistrationData(String firstName, String lastName, String username, String password) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("Vārds ir obligāts!");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Uzvārds ir obligāts!");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Lietotājvārds ir obligāts!");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Parole ir obligāta!");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Parolei jābūt vismaz 6 simbolus garai!");
        }
    }
    
    /**
     * Saglabā lietotāju datubāzē atbilstoši Jūsu tabulas struktūrai.
     */
    private void saveUserToDatabase(String firstName, String lastName, String username, String password, String role) 
            throws SQLException {
        
        String sql = "INSERT INTO users (username, password, first_name, last_name, role, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, firstName);
            pstmt.setString(4, lastName);
            pstmt.setString(5, role);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Neizdevās saglabāt lietotāju datubāzē!");
            }
            
            System.out.println("✅ Lietotājs reģistrēts: " + firstName + " " + lastName + " (" + username + ")");
        }
    }
    
    /**
     * Izveido atbilstošo User objektu pēc lomas.
     */
    private User createUserByRole(String fullName, String username, String password, String role) {
        switch (role) {
            case "ADMIN":
                return new Admin(fullName, username, password);
            case "TEACHER":
                return new Teacher(fullName, username, password);
            case "STUDENT":
            default:
                return new Student(fullName, username, password);
        }
    }
    
    /**
     * Atgriež visu lietotāju sarakstu.
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT first_name, last_name, username, password, role FROM users";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String fullName = firstName + " " + lastName;
                String username = rs.getString("username");
                String password = rs.getString("password");
                String role = rs.getString("role");
                
                users.add(createUserByRole(fullName, username, password, role));
            }
        }
        return users;
    }
    
    /**
     * Dzēš lietotāju.
     */
    public boolean deleteUser(String username) throws SQLException {
        String sql = "DELETE FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;
        }
    }
}