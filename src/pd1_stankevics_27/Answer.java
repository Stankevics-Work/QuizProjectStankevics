package pd1_stankevics_27;

/**
 * Atbildes klase, kas satur informāciju par konkrētu atbildes variantu
 * testa jautājumam.
 * 
 * @author stankevics_27
 * @version 1.0
 */
public class Answer {
    private String answer_id;
    private String question_id;
    private String answer_text;
    private boolean is_correct;

    /**
     * Izveido jaunu Answer objektu ar norādītajiem parametriem.
     * 
     * @param answerId atbildes unikālais identifikators
     * @param questionId jautājuma identifikators
     * @param answerText atbildes teksts
     * @param isCorrect true ja šī ir pareizā atbilde
     */
    public Answer(String answerId, String questionId, String answerText, boolean isCorrect) {
        this.answer_id = answerId;
        this.question_id = questionId;
        this.answer_text = answerText;
        this.is_correct = isCorrect;
    }

    /**
     * Pārbauda vai lietotāja ievadītā atbilde sakrīt ar šo atbildi.
     * 
     * @param userAnswer lietotāja ievadītā atbilde
     * @return true ja atbildes sakrīt (ignorējot reģistru)
     */
    public boolean checkAnswer(String userAnswer) {
        return answer_text.equalsIgnoreCase(userAnswer);
    }

    /**
     * Atgriež atbildes tekstu.
     * 
     * @return atbildes teksts
     */
    public String getAnswerText() {
        return answer_text;
    }
}