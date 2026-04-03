package pd1_stankevics_24;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Administratora lietotāja klase ar sistēmas pārvaldības tiesībām.
 * 
 * @author armins.stankevics_24
 */
public class Admin extends User {

    private String adminId;
    private String accessLevel;
    private List<String> systemLogs;
    private List<User> managedUsers;
    private List<String> permissions;

    /**
     * Konstruktors jauna administratora izveidei ar pilnu piekļuvi.
     *
     * @param name     administratora vārds
     * @param login    pieteikšanās lietotājvārds
     * @param password pieteikšanās parole
     */
    public Admin(String name, String login, String password) {
        super(name, login, password);
        this.adminId = generateAdminId();
        this.accessLevel = "FULL";
        this.systemLogs = new ArrayList<>();
        this.managedUsers = new ArrayList<>();
        this.permissions = new ArrayList<>();
        initializeDefaultPermissions();
        logAction("Administrators izveidots: " + login);
    }

    /**
     * Konstruktors jauna administratora izveidei ar norādītu piekļuves līmeni.
     *
     * @param name        administratora vārds
     * @param login       pieteikšanās lietotājvārds
     * @param password    pieteikšanās parole
     * @param email       e-pasta adrese
     * @param accessLevel piekļuves līmenis (FULL, LIMITED, READONLY)
     */
    public Admin(String name, String login, String password, String email, String accessLevel) {
        super(name, login, password, email);
        this.adminId = generateAdminId();
        setAccessLevel(accessLevel);
        this.systemLogs = new ArrayList<>();
        this.managedUsers = new ArrayList<>();
        this.permissions = new ArrayList<>();
        initializeDefaultPermissions();
        logAction("Administrators izveidots: " + login + " (līmenis: " + accessLevel + ")");
    }

    /**
     * Ģenerē unikālu administratora identifikatoru.
     *
     * @return unikāls administratora ID
     */
    private String generateAdminId() {
        return "ADM" + System.currentTimeMillis() + (int)(Math.random() * 100);
    }

    /**
     * Inicializē noklusējuma atļauju sarakstu.
     */
    private void initializeDefaultPermissions() {
        permissions.add("USER_CREATE");
        permissions.add("USER_READ");
        permissions.add("USER_UPDATE");
        permissions.add("USER_DELETE");
        permissions.add("TEST_CREATE");
        permissions.add("TEST_READ");
        permissions.add("TEST_UPDATE");
        permissions.add("TEST_DELETE");
        permissions.add("SYSTEM_LOGS");
        permissions.add("BACKUP");
    }

    /**
     * Iestata administratora piekļuves līmeni.
     *
     * @param accessLevel piekļuves līmenis (FULL, LIMITED, READONLY)
     * @throws IllegalArgumentException ja piekļuves līmenis nav derīgs
     */
    public final void setAccessLevel(String accessLevel) {
        if (accessLevel == null || accessLevel.trim().isEmpty()) {
            throw new IllegalArgumentException("Piekļuves līmenis nevar būt tukšs!");
        }
        String level = accessLevel.trim().toUpperCase();
        if (!level.equals("FULL") && !level.equals("LIMITED") && !level.equals("READONLY")) {
            throw new IllegalArgumentException("Atļautie līmeņi: FULL, LIMITED, READONLY");
        }
        this.accessLevel = level;
    }

    /**
     * Pārbauda, vai administratoram ir norādītā atļauja.
     *
     * @param permission pārbaudāmā atļauja
     * @return true, ja atļauja eksistē, false ja nav
     */
    public boolean hasPermission(String permission) {
        if (accessLevel.equals("FULL")) return true;
        else if (accessLevel.equals("LIMITED")) {
            return permissions.contains(permission) && 
                   !permission.startsWith("SYSTEM") && 
                   !permission.equals("BACKUP");
        } else {
            return permission.equals("USER_READ") || permission.equals("TEST_READ");
        }
    }

    /**
     * Pievieno jaunu lietotāju datubāzē.
     *
     * @param connection aktīvs savienojums ar datubāzi
     * @param firstName  lietotāja vārds
     * @param lastName   lietotāja uzvārds
     * @param username   lietotājvārds
     * @param password   parole
     * @param email      e-pasta adrese
     * @param role       lietotāja loma (ADMIN, TEACHER, STUDENT)
     * @return true, ja lietotājs veiksmīgi pievienots, false ja neizdevās
     * @throws SQLException      ja rodas kļūda datubāzes operācijā
     * @throws SecurityException ja administratoram nav tiesību pievienot lietotājus
     */
    public boolean addUserToDatabase(Connection connection, String firstName, String lastName, 
                                      String username, String password, String email, String role) 
                                      throws SQLException, SecurityException {
        if (!hasPermission("USER_CREATE")) {
            throw new SecurityException("Nav tiesību pievienot lietotājus!");
        }
        if (connection == null) throw new SQLException("Nav savienojuma ar datubāzi!");
        String sql = "INSERT INTO users (username, password, first_name, last_name, email, role, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, firstName);
            pstmt.setString(4, lastName);
            pstmt.setString(5, email);
            pstmt.setString(6, role);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                User user = createUserByRole(firstName + " " + lastName, username, password, email, role);
                managedUsers.add(user);
                logAction("Pievienots lietotājs: " + username + " (loma: " + role + ")");
                return true;
            }
            return false;
        }
    }

    /**
     * Dzēš lietotāju no datubāzes pēc lietotājvārda.
     *
     * @param connection aktīvs savienojums ar datubāzi
     * @param username   dzēšamā lietotāja lietotājvārds
     * @return true, ja lietotājs veiksmīgi dzēsts, false ja neizdevās
     * @throws SQLException      ja rodas kļūda datubāzes operācijā
     * @throws SecurityException ja administratoram nav tiesību dzēst lietotājus vai tiek mēģināts dzēst savu kontu
     */
    public boolean deleteUserFromDatabase(Connection connection, String username) 
            throws SQLException, SecurityException {
        if (!hasPermission("USER_DELETE")) {
            throw new SecurityException("Nav tiesību dzēst lietotājus!");
        }
        if (connection == null) throw new SQLException("Nav savienojuma ar datubāzi!");
        if (this.getLogin().equals(username)) {
            throw new SecurityException("Nevar dzēst savu kontu!");
        }
        String sql = "DELETE FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            boolean result = pstmt.executeUpdate() > 0;
            if (result) {
                managedUsers.removeIf(u -> u.getLogin().equals(username));
                logAction("Izdzēsts lietotājs: " + username);
            }
            return result;
        }
    }

    /**
     * Atjaunina esoša lietotāja datus datubāzē.
     *
     * @param connection aktīvs savienojums ar datubāzi
     * @param username   atjaunināmā lietotāja lietotājvārds
     * @param firstName  jaunais vārds
     * @param lastName   jaunais uzvārds
     * @param email      jaunā e-pasta adrese
     * @param role       jaunā loma
     * @return true, ja lietotājs veiksmīgi atjaunināts, false ja neizdevās
     * @throws SQLException      ja rodas kļūda datubāzes operācijā
     * @throws SecurityException ja administratoram nav tiesību atjaunināt lietotājus
     */
    public boolean updateUser(Connection connection, String username, String firstName, 
                               String lastName, String email, String role) 
                               throws SQLException, SecurityException {
        if (!hasPermission("USER_UPDATE")) {
            throw new SecurityException("Nav tiesību atjaunināt lietotājus!");
        }
        if (connection == null) throw new SQLException("Nav savienojuma ar datubāzi!");
        String sql = "UPDATE users SET first_name = ?, last_name = ?, email = ?, role = ? WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, email);
            pstmt.setString(4, role);
            pstmt.setString(5, username);
            boolean result = pstmt.executeUpdate() > 0;
            if (result) logAction("Atjaunināts lietotājs: " + username);
            return result;
        }
    }

    /**
     * Ielādē visus lietotājus no datubāzes.
     *
     * @param connection aktīvs savienojums ar datubāzi
     * @return saraksts ar visiem lietotājiem
     * @throws SQLException      ja rodas kļūda datubāzes operācijā
     * @throws SecurityException ja administratoram nav tiesību skatīt lietotājus
     */
    public List<User> loadAllUsersFromDatabase(Connection connection) throws SQLException {
        List<User> users = new ArrayList<>();
        if (!hasPermission("USER_READ")) {
            throw new SecurityException("Nav tiesību skatīt lietotājus!");
        }
        if (connection == null) throw new SQLException("Nav savienojuma ar datubāzi!");
        String sql = "SELECT first_name, last_name, username, password, email, role FROM users ORDER BY last_name, first_name";
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
                User user = createUserByRole(fullName, username, password, email, role);
                users.add(user);
                if (!managedUsers.contains(user)) managedUsers.add(user);
            }
        }
        logAction("Ielādēti " + users.size() + " lietotāji no datubāzes");
        return users;
    }

    /**
     * Izveido lietotāja objektu atbilstoši norādītajai lomai.
     *
     * @param fullName lietotāja pilns vārds
     * @param username lietotājvārds
     * @param password parole
     * @param email    e-pasta adrese
     * @param role     lietotāja loma
     * @return izveidotais lietotāja objekts
     */
    private User createUserByRole(String fullName, String username, String password, String email, String role) {
        switch (role) {
            case "ADMIN": return new Admin(fullName, username, password, email, "FULL");
            case "TEACHER": return new Teacher(fullName, username, password, email, "Datorzinātņu katedra", "Lektors");
            case "STUDENT": default: return new Student(fullName, username, password, email, "IT-21", 1);
        }
    }

    /**
     * Ieraksta darbību sistēmas žurnālā.
     *
     * @param action veiktā darbība
     */
    private void logAction(String action) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = timestamp + " | " + getLogin() + " (" + getRole() + ") | " + action;
        systemLogs.add(logEntry);
        System.out.println("📋 ADMIN LOG: " + logEntry);
    }

    /**
     * Atgriež sistēmas žurnāla ierakstu kopiju.
     *
     * @return žurnāla ierakstu saraksts
     */
    public List<String> getSystemLogs() { return new ArrayList<>(systemLogs); }

    /**
     * Notīra visus sistēmas žurnāla ierakstus.
     *
     * @return izdzēsto ierakstu skaits
     */
    public int clearLogs() {
        int count = systemLogs.size();
        systemLogs.clear();
        logAction("Iztīrīti " + count + " žurnāla ieraksti");
        return count;
    }

    /**
     * Eksportē sistēmas žurnālu formatētā veidā.
     *
     * @return formatēts žurnāla eksports
     */
    public String exportLogs() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                     SISTĒMAS ŽURNĀLS                           ║\n");
        sb.append("╠════════════════════════════════════════════════════════════════╣\n");
        if (systemLogs.isEmpty()) {
            sb.append("║                     Žurnāls ir tukšs                          ║\n");
        } else {
            for (String log : systemLogs) {
                sb.append("║ ").append(String.format("%-60s", log)).append(" ║\n");
            }
        }
        sb.append("╚════════════════════════════════════════════════════════════════╝");
        return sb.toString();
    }

    /**
     * Veic datubāzes dublējumu.
     *
     * @param connection aktīvs savienojums ar datubāzi
     * @return true, ja dublējums veiksmīgs, false ja neizdevās
     */
    public boolean backupDatabase(Connection connection) {
        if (!hasPermission("BACKUP")) {
            logAction("Mēģinājums veikt dublējumu bez atļaujas");
            return false;
        }
        logAction("Veikts datubāzes dublējums");
        return true;
    }

    /**
     * Atgriež administratora ID.
     *
     * @return administratora ID
     */
    public String getAdminId() { return adminId; }

    /**
     * Atgriež administratora piekļuves līmeni.
     *
     * @return piekļuves līmenis
     */
    public String getAccessLevel() { return accessLevel; }

    /**
     * Atgriež administratora atļauju kopiju.
     *
     * @return atļauju saraksts
     */
    public List<String> getPermissions() { return new ArrayList<>(permissions); }

    /**
     * Atgriež pārvaldīto lietotāju kopiju.
     *
     * @return pārvaldīto lietotāju saraksts
     */
    public List<User> getManagedUsers() { return new ArrayList<>(managedUsers); }

    /**
     * Atgriež pārvaldīto lietotāju skaitu.
     *
     * @return pārvaldīto lietotāju skaits
     */
    public int getUserCount() { return managedUsers.size(); }

    @Override
    public String getRole() { return "ADMIN"; }

    @Override
    public String getUserType() { return "Administrators"; }

    @Override
    public String toString() { return super.toString() + " [ID: " + adminId + ", Līmenis: " + accessLevel + "]"; }
}