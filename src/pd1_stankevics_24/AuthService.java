package pd1_stankevics_24;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Autentifikācijas serviss, kas nodrošina lietotāju reģistrāciju un ielogošanos.
 * Apstrādā lietotāja ievadīto informāciju, pārbauda tās derīgumu, veic
 * pārbaudes pret datubāzi un izveido atbilstošo {@link User} apakšklases objektu.
 *
 * @author armins.stankevics_24
 * @see User
 * @see Student
 * @see Teacher
 * @see Admin
 */
public class AuthService {
    
    private Connection connection;
    private User currentUser;
    
    /**
     * Izveido jaunu AuthService objektu ar norādīto datubāzes savienojumu.
     *
     * @param connection aktīvs savienojums ar datubāzi
     */
    public AuthService(Connection connection) {
        this.connection = connection;
        this.currentUser = null;
    }
    
    /**
     * Reģistrē jaunu studentu sistēmā. Pārbauda vai lietotājvārds un e-pasts
     * jau neeksistē datubāzē.
     *
     * @param firstName studenta vārds
     * @param lastName  studenta uzvārds
     * @param username  studenta lietotājvārds (unikāls)
     * @param password  studenta parole (jābūt vismaz 6 simbolus garai un jāsatur cipars)
     * @param email     studenta e-pasta adrese (unikāla)
     * @return izveidotais {@link Student} objekts
     * @throws IllegalArgumentException ja dati ir nederīgi vai jau aizņemti
     * @throws SQLException           ja rodas kļūda datubāzes darbībā
     */
    public Student registerStudent(String firstName, String lastName, String username, 
                                   String password, String email) 
            throws IllegalArgumentException, SQLException {
        
        validateRegistrationData(firstName, lastName, username, password, email);
        
        if (isUsernameTaken(username)) {
            throw new IllegalArgumentException("Lietotājvārds '" + username + "' jau ir aizņemts!");
        }
        
        if (isEmailTaken(email)) {
            throw new IllegalArgumentException("E-pasts '" + email + "' jau ir reģistrēts!");
        }
        
        int userId = saveUserToDatabase(firstName, lastName, username, password, email, "STUDENT");
        
        String fullName = firstName + " " + lastName;
        Student student = new Student(fullName, username, password, email, "IT-21", 1);
        
        System.out.println("✅ Reģistrēts jauns students: " + fullName + " (" + username + ")");
        return student;
    }
    
    /**
     * Reģistrē jaunu pasniedzēju sistēmā. Šo metodi parasti izmanto administrators.
     *
     * @param firstName  pasniedzēja vārds
     * @param lastName   pasniedzēja uzvārds
     * @param username   pasniedzēja lietotājvārds (unikāls)
     * @param password   pasniedzēja parole
     * @param email      pasniedzēja e-pasta adrese (unikāla)
     * @param department pasniedzēja katedra
     * @param position   pasniedzēja amats
     * @return izveidotais {@link Teacher} objekts
     * @throws IllegalArgumentException ja dati ir nederīgi vai jau aizņemti
     * @throws SQLException           ja rodas kļūda datubāzes darbībā
     */
    public Teacher registerTeacher(String firstName, String lastName, String username, 
                                   String password, String email, String department, String position) 
            throws IllegalArgumentException, SQLException {
        
        validateRegistrationData(firstName, lastName, username, password, email);
        
        if (isUsernameTaken(username)) {
            throw new IllegalArgumentException("Lietotājvārds '" + username + "' jau ir aizņemts!");
        }
        
        if (isEmailTaken(email)) {
            throw new IllegalArgumentException("E-pasts '" + email + "' jau ir reģistrēts!");
        }
        
        int userId = saveUserToDatabase(firstName, lastName, username, password, email, "TEACHER");
        
        String fullName = firstName + " " + lastName;
        Teacher teacher = new Teacher(fullName, username, password, email, department, position);
        
        System.out.println("✅ Reģistrēts jauns pasniedzējs: " + fullName + " (" + username + ")");
        return teacher;
    }
    
    /**
     * Veic lietotāja ielogošanos sistēmā.
     *
     * @param username lietotājvārds
     * @param password parole
     * @return {@link User} objekts atbilstoši lietotāja lomai (Student, Teacher vai Admin)
     * @throws IllegalArgumentException ja lietotājvārds vai parole ir tukši
     * @throws SecurityException       ja lietotājvārds vai parole ir nepareizi
     * @throws SQLException            ja rodas kļūda datubāzes darbībā
     */
    public User login(String username, String password) 
            throws IllegalArgumentException, SecurityException, SQLException {
        
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Lūdzu ievadiet lietotājvārdu!");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Lūdzu ievadiet paroli!");
        }
        
        String sql = "SELECT id, first_name, last_name, email, role FROM users " +
                     "WHERE username = ? AND password = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username.trim());
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    String fullName = firstName + " " + lastName;
                    String email = rs.getString("email");
                    String role = rs.getString("role");
                    
                    User user = createUserByRole(fullName, username, password, email, role);
                    this.currentUser = user;
                    
                    System.out.println("✅ Ielogojies: " + fullName + " (" + role + ")");
                    return user;
                } else {
                    throw new SecurityException("Nepareizs lietotājvārds vai parole!");
                }
            }
        }
    }
    
    /**
     * Pārbauda vai lietotājvārds jau eksistē datubāzē.
     *
     * @param username pārbaudāmais lietotājvārds
     * @return {@code true} ja lietotājvārds jau ir aizņemts
     * @throws SQLException ja rodas kļūda datubāzes darbībā
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
     * Pārbauda vai e-pasta adrese jau eksistē datubāzē.
     *
     * @param email pārbaudāmā e-pasta adrese
     * @return {@code true} ja e-pasta adrese jau ir reģistrēta
     * @throws SQLException ja rodas kļūda datubāzes darbībā
     */
    public boolean isEmailTaken(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    
    /**
     * Validē reģistrācijas datus. Pārbauda vārdu, uzvārdu, lietotājvārdu,
     * paroli un e-pasta adresi.
     *
     * @param firstName vārds
     * @param lastName  uzvārds
     * @param username  lietotājvārds
     * @param password  parole
     * @param email     e-pasta adrese
     * @throws IllegalArgumentException ja kāds no datiem neatbilst prasībām
     */
    private void validateRegistrationData(String firstName, String lastName, String username, 
                                          String password, String email) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("Vārds ir obligāts!");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Uzvārds ir obligāts!");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Lietotājvārds ir obligāts!");
        }
        if (username.length() < 3) {
            throw new IllegalArgumentException("Lietotājvārdam jābūt vismaz 3 simbolus garam!");
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Parole ir obligāta!");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Parolei jābūt vismaz 6 simbolus garai!");
        }
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Parolei jāsatur vismaz viens cipars!");
        }
        if (email != null && !email.trim().isEmpty()) {
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
            if (!email.matches(emailRegex)) {
                throw new IllegalArgumentException("Nederīga e-pasta adrese!");
            }
        }
    }
    
    /**
     * Saglabā jauna lietotāja datus datubāzē.
     *
     * @param firstName vārds
     * @param lastName  uzvārds
     * @param username  lietotājvārds
     * @param password  parole
     * @param email     e-pasta adrese
     * @param role      loma ("STUDENT", "TEACHER", "ADMIN")
     * @return automātiski ģenerētais lietotāja ID datubāzē
     * @throws SQLException ja rodas kļūda datubāzes darbībā
     */
    private int saveUserToDatabase(String firstName, String lastName, String username, 
                                   String password, String email, String role) 
            throws SQLException {
        
        String sql = "INSERT INTO users (username, password, first_name, last_name, email, role, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, firstName);
            pstmt.setString(4, lastName);
            pstmt.setString(5, email);
            pstmt.setString(6, role);
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("✅ Pievienotas rindas: " + rowsAffected);
            
            if (rowsAffected == 0) {
                throw new SQLException("Neizdevās saglabāt lietotāju datubāzē!");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        }
    }
    
    /**
     * Izveido atbilstošo {@link User} apakšklases objektu pēc lomas.
     *
     * @param fullName pilns vārds
     * @param username lietotājvārds
     * @param password parole
     * @param email    e-pasta adrese
     * @param role     lomas nosaukums ("ADMIN", "TEACHER", "STUDENT")
     * @return atbilstošais lietotāja objekts
     */
    private User createUserByRole(String fullName, String username, String password, String email, String role) {
        switch (role) {
            case "ADMIN":
                return new Admin(fullName, username, password, email, "FULL");
            case "TEACHER":
                return new Teacher(fullName, username, password, email, "Datorzinātņu katedra", "Lektors");
            case "STUDENT":
            default:
                return new Student(fullName, username, password, email, "IT-21", 1);
        }
    }
    
    /**
     * Izrakstās no sistēmas, notīrot pašreizējā lietotāja informāciju.
     */
    public void logout() {
        if (currentUser != null) {
            System.out.println("👋 Izrakstījās: " + currentUser.getName());
            currentUser = null;
        }
    }
    
    /**
     * Atgriež pašreizējo ielogojušos lietotāju.
     *
     * @return pašreizējais lietotājs vai {@code null} ja neviens nav ielogojies
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Pārbauda vai kāds lietotājs pašlaik ir ielogojies.
     *
     * @return {@code true} ja lietotājs ir ielogojies
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Ielādē visu sistēmas lietotāju sarakstu no datubāzes.
     *
     * @return {@link List} ar visiem lietotājiem
     * @throws SQLException ja rodas kļūda datubāzes darbībā
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT first_name, last_name, username, password, email, role FROM users";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String fullName = firstName + " " + lastName;
                String username = rs.getString("username");
                String password = rs.getString("password");
                String email = rs.getString("email");
                String role = rs.getString("role");
                
                users.add(createUserByRole(fullName, username, password, email, role));
            }
        }
        return users;
    }
}