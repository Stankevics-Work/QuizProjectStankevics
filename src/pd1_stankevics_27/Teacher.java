package pd1_stankevics_27;

import java.util.ArrayList;
import java.util.List;

/**
 * Pasniedzēja lietotāja klase.
 * 
 * @author stankevics_27
 * @version 1.0
 */
public class Teacher extends User {

    private List<Question> questionsCreated = new ArrayList<>();

    /**
     * Izveido jaunu Teacher objektu.
     * 
     * @param name pasniedzēja vārds un uzvārds
     * @param login pasniedzēja lietotājvārds
     * @param password pasniedzēja parole
     */
    public Teacher(String name, String login, String password) {
        super(name, login, password);
    }

    /**
     * Izveido jaunu testa jautājumu.
     * 
     * @param text jautājuma teksts
     * @param answer pareizais atbildes variants
     * @return izveidotais Question objekts
     */
    public Question createQuestion(String text, String answer) {
        Question q = new Question(text, answer, "Operētājsistēma", "Datu bāze", 0);
        questionsCreated.add(q);
        return q;
    }

    /**
     * Pievieno pasniedzēju programmai.
     * 
     * @param program studiju programma
     */
    public void assignToProgram(Program program) {
        program.addTeacher(this);
    }
}