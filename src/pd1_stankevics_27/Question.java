package pd1_stankevics_27;

public class Question {
    private String text;
    private String[] options;
    private int correctIndex;

    public Question(String text, String option1, String option2, String option3, int correctIndex) {
        this.text = text;
        this.options = new String[]{ option1, option2, option3 };
        this.correctIndex = correctIndex;
    }

    public String getText() {
        return text;
    }

    public String[] getOptions() {
        return options;
    }

    public boolean isCorrect(int selectedIndex) {
        return selectedIndex == correctIndex;
    }
}
