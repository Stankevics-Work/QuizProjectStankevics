package pd1_stankevics_27;

import java.util.ArrayList;
import java.util.List;

public class DistanceExamenator  {

    private List<User> users = new ArrayList<>();
    private List<Question> questions = new ArrayList<>();
    private List<Result> results = new ArrayList<>();

    public void addUser(String name, String login, String password) {
        users.add(new User(name, login, password));
    }

    public void addStudent(String name, String login, String password) {
        users.add(new Student(name, login, password));
    }

    public void createQuestion(String text, String answer) {
        questions.add(new Question(text, answer, "Oper\u0113t\u0101jsist\u0113ma", "Datu b\u0101ze", 0));
    }

    public void assignToProgram(Program program) {
    }

    public List<User> getUsers() {
           return users;
    }

    public List<Result> getResults() {
    return results;
    }
}

