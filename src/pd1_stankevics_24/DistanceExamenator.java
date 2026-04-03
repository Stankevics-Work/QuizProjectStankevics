package pd1_stankevics_24;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Sistēmas galvenā pārvaldības klase, kas kalpo kā centrālais mezgls
 * lietotāju, testu, rezultātu un programmu pārvaldībai. Nodrošina
 * metodes datu pievienošanai, meklēšanai un statistikas ģenerēšanai.
 *
 * @author armins.stankevics_24
 * @see User
 * @see Test
 * @see Result
 * @see Program
 */
public class DistanceExamenator {
    
    private List<User> users;
    private List<Test> tests;
    private List<Result> results;
    private List<Program> programs;
    private Connection databaseConnection;
    private AuthService authService;

    /**
     * Izveido jaunu DistanceExamenator objektu ar tukšām kolekcijām.
     */
    public DistanceExamenator() {
        this.users = new ArrayList<>();
        this.tests = new ArrayList<>();
        this.results = new ArrayList<>();
        this.programs = new ArrayList<>();
        this.databaseConnection = null;
        this.authService = null;
    }

    /**
     * Izveido jaunu DistanceExamenator objektu ar norādīto datubāzes savienojumu.
     *
     * @param connection aktīvs savienojums ar datubāzi
     */
    public DistanceExamenator(Connection connection) {
        this();
        this.databaseConnection = connection;
        if (connection != null) {
            this.authService = new AuthService(connection);
        }
    }

    /**
     * Pievieno jaunu studentu sistēmai (atmiņas kolekcijai).
     *
     * @param name     studenta vārds un uzvārds
     * @param login    studenta lietotājvārds
     * @param password studenta parole
     * @param email    studenta e-pasta adrese
     * @param group    studenta grupa
     * @param course   studenta kurss (1-4)
     */
    public void addStudent(String name, String login, String password, String email, String group, int course) {
        try {
            Student student = new Student(name, login, password, email, group, course);
            users.add(student);
            System.out.println("✅ Pievienots students: " + name);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Kļūda pievienojot studentu: " + e.getMessage());
        }
    }

    /**
     * Pievieno jaunu pasniedzēju sistēmai (atmiņas kolekcijai).
     *
     * @param name       pasniedzēja vārds un uzvārds
     * @param login      pasniedzēja lietotājvārds
     * @param password   pasniedzēja parole
     * @param email      pasniedzēja e-pasta adrese
     * @param department pasniedzēja katedra
     * @param position   pasniedzēja amats
     */
    public void addTeacher(String name, String login, String password, String email, 
                           String department, String position) {
        try {
            Teacher teacher = new Teacher(name, login, password, email, department, position);
            users.add(teacher);
            System.out.println("✅ Pievienots pasniedzējs: " + name);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Kļūda pievienojot pasniedzēju: " + e.getMessage());
        }
    }

    /**
     * Pievieno jaunu administratoru sistēmai (atmiņas kolekcijai).
     *
     * @param name        administratora vārds un uzvārds
     * @param login       administratora lietotājvārds
     * @param password    administratora parole
     * @param email       administratora e-pasta adrese
     * @param accessLevel piekļuves līmenis (FULL, LIMITED, READONLY)
     */
    public void addAdmin(String name, String login, String password, String email, String accessLevel) {
        try {
            Admin admin = new Admin(name, login, password, email, accessLevel);
            users.add(admin);
            System.out.println("✅ Pievienots administrators: " + name);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Kļūda pievienojot administratoru: " + e.getMessage());
        }
    }

    /**
     * Pievieno jaunu programmu sistēmai.
     *
     * @param program pievienojamā programma (nedrīkst būt null)
     */
    public void addProgram(Program program) {
        if (program != null && !programs.contains(program)) {
            programs.add(program);
            System.out.println("✅ Pievienota programma: " + program.getName());
        }
    }

    /**
     * Pievieno jaunu testu sistēmai.
     *
     * @param test pievienojamais tests (nedrīkst būt null)
     */
    public void addTest(Test test) {
        if (test != null && !tests.contains(test)) {
            tests.add(test);
            System.out.println("✅ Pievienots tests: " + test.getTitle());
        }
    }

    /**
     * Atrod lietotāju pēc lietotājvārda.
     *
     * @param login meklējamais lietotājvārds
     * @return atrastais {@link User} objekts vai {@code null} ja nav atrasts
     */
    public User findUserByLogin(String login) {
        for (User user : users) {
            if (user.getLogin().equals(login)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Atrod testu pēc tā ID.
     *
     * @param testId meklējamā testa ID
     * @return atrastais {@link Test} objekts vai {@code null} ja nav atrasts
     */
    public Test findTestById(String testId) {
        for (Test test : tests) {
            if (test.getTestId().equals(testId)) {
                return test;
            }
        }
        return null;
    }

    /**
     * Ielādē visus lietotājus no datubāzes, izmantojot {@link AuthService}.
     * Aizstāj esošo lietotāju sarakstu ar ielādēto.
     */
    public void loadUsersFromDatabase() {
        if (authService == null || databaseConnection == null) {
            System.err.println("❌ Nav pieejams datubāzes savienojums!");
            return;
        }
        
        try {
            users = authService.getAllUsers();
            System.out.println("✅ Ielādēti " + users.size() + " lietotāji no datubāzes");
        } catch (SQLException e) {
            System.err.println("❌ Kļūda ielādējot lietotājus: " + e.getMessage());
        }
    }

    /**
     * Ģenerē un atgriež sistēmas statistiku, tostarp lietotāju, testu,
     * jautājumu un rezultātu skaitu.
     *
     * @return formatēta statistikas informācija
     */
    public String getSystemStatistics() {
        int studentCount = 0;
        int teacherCount = 0;
        int adminCount = 0;
        
        for (User u : users) {
            if (u instanceof Student) studentCount++;
            else if (u instanceof Teacher) teacherCount++;
            else if (u instanceof Admin) adminCount++;
        }
        
        int totalTests = tests.size();
        int totalQuestions = tests.stream().mapToInt(Test::getQuestionCount).sum();
        int totalResults = results.size();
        
        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════╗\n");
        sb.append("║              SISTĒMAS STATISTIKA                  ║\n");
        sb.append("╠════════════════════════════════════════════════════╣\n");
        sb.append("║ LIETOTĀJI:                                        ║\n");
        sb.append(String.format("║   Studenti:      %-35d ║\n", studentCount));
        sb.append(String.format("║   Pasniedzēji:   %-35d ║\n", teacherCount));
        sb.append(String.format("║   Administratori: %-33d ║\n", adminCount));
        sb.append(String.format("║   Kopā:          %-35d ║\n", users.size()));
        sb.append("╠════════════════════════════════════════════════════╣\n");
        sb.append("║ TESTI:                                            ║\n");
        sb.append(String.format("║   Testu skaits:  %-35d ║\n", totalTests));
        sb.append(String.format("║   Jautājumi:     %-35d ║\n", totalQuestions));
        sb.append(String.format("║   Rezultāti:     %-35d ║\n", totalResults));
        sb.append("╚════════════════════════════════════════════════════╝");
        
        return sb.toString();
    }

    // Getter metodes
    /**
     * Atgriež visu sistēmas lietotāju kopiju.
     *
     * @return lietotāju saraksts
     */
    public List<User> getUsers() { return new ArrayList<>(users); }

    /**
     * Atgriež visu sistēmas testu kopiju.
     *
     * @return testu saraksts
     */
    public List<Test> getTests() { return new ArrayList<>(tests); }

    /**
     * Atgriež visu sistēmas rezultātu kopiju.
     *
     * @return rezultātu saraksts
     */
    public List<Result> getResults() { return new ArrayList<>(results); }

    /**
     * Atgriež visu sistēmas programmu kopiju.
     *
     * @return programmu saraksts
     */
    public List<Program> getPrograms() { return new ArrayList<>(programs); }

    /**
     * Atgriež datubāzes savienojumu.
     *
     * @return datubāzes savienojums vai {@code null} ja nav izveidots
     */
    public Connection getDatabaseConnection() { return databaseConnection; }

    /**
     * Atgriež autentifikācijas servisu.
     *
     * @return autentifikācijas servisa objekts
     */
    public AuthService getAuthService() { return authService; }

    /**
     * Pārbauda vai ir izveidots datubāzes savienojums.
     *
     * @return {@code true} ja savienojums pastāv
     */
    public boolean isDatabaseConnected() { return databaseConnection != null; }

    /**
     * Notīra visus sistēmas datus no atmiņas kolekcijām.
     */
    public void clearAllData() {
        users.clear();
        tests.clear();
        results.clear();
        programs.clear();
        System.out.println("🧹 Visi sistēmas dati notīrīti");
    }
}