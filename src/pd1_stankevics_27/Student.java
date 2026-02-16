package pd1_stankevics_27;

public class Student extends User {

    private int questionsCount = 0;
    private int rightAnswers = 0;

    public Student(String name, String login, String password) {
        super(name, login, password);
    }

    public void addQuestion(Question question, int selectedIndex) {
        questionsCount++;
        if (question.isCorrect(selectedIndex)) {
            rightAnswers++;
        }
    }

    public int getAnswersCount() {
        return questionsCount;
    }

    public int getRightAnswersCount() {
        return rightAnswers;
    }
}
