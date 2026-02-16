package pd1_stankevics_27;

import java.util.ArrayList;
import java.util.List;

public class Teacher extends User {

    private List<Question> questionsCreated = new ArrayList<>();

    public Teacher(String name, String login, String password) {
        super(name, login, password);
    }

    public Question createQuestion(String text, String answer) {
        Question q = new Question(text, answer, "Oper\u0113t\u0101jsist\u0113ma", "Datu b\u0101ze", 0);
        questionsCreated.add(q);
        return q;
    }

    public void assignToProgram(Program program) {
        program.addTeacher(this);
    }
}