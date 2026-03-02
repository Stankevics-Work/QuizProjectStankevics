package pd1_stankevics_27;

import java.util.ArrayList;
import java.util.List;

/**
 * Sistēmas galvenā pārvaldības klase.
 * 
 * @author stankevics_27
 * @version 1.0
 */
public class DistanceExamenator  {

    private List<User> users = new ArrayList<>();
    private List<Question> questions = new ArrayList<>();
    private List<Result> results = new ArrayList<>();

    /**
     * Pievieno jaunu parasto lietotāju sistēmai.
     */
    public void addUser(String name, String login, String password) {
        users.add(new User(name, login, password));
    }

    /**
     * Pievieno jaunu studentu sistēmai.
     */
    public void addStudent(String name, String login, String password) {
        users.add(new Student(name, login, password));
    }

    /**
     * Izveido jaunu jautājumu.
     */
    public void createQuestion(String text, String answer) {
        questions.add(new Question(text, answer, "Operētājsistēma", "Datu bāze", 0));
    }

    /**
     * Pievieno eksaminētāju programmai.
     */
    public void assignToProgram(Program program) {
        // TODO
    }

    /**
     * Atgriež visu sistēmas lietotāju sarakstu.
     */
    public List<User> getUsers() {
           return users;
    }

    /**
     * Atgriež visu testu rezultātu sarakstu.
     */
    public List<Result> getResults() {
        return results;
    }
}