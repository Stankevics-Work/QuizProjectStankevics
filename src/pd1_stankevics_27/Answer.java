package pd1_stankevics_27;

public class Answer {
    private String answer_id;
    private String question_id;
    private String answer_text;
    private boolean is_correct;

    public Answer(String answerId, String questionId, String answerText, boolean isCorrect) {
        this.answer_id = answerId;
        this.question_id = questionId;
        this.answer_text = answerText;
        this.is_correct = isCorrect;
    }

    public boolean checkAnswer(String userAnswer) {
        return answer_text.equalsIgnoreCase(userAnswer);
    }

    public String getAnswerText() {
        return answer_text;
    }
}
