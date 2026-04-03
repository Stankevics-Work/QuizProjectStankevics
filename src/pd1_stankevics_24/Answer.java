package pd1_stankevics_24;

/**
 * Atbildes klase, kas satur informāciju par konkrētu atbildes variantu testa jautājumam.
 * 
 * @author armins.stankevics_24
 */
public class Answer {
    private String answerId;
    private String questionId;
    private String answerText;
    private boolean isCorrect;
    private int orderNumber;
    private String explanation;

    /**
     * Konstruktors atbildes izveidei ar pamatinformāciju.
     *
     * @param answerId   atbildes unikālais identifikators
     * @param questionId jautājuma identifikators, kuram pieder šī atbilde
     * @param answerText atbildes teksts
     * @param isCorrect  norāde, vai atbilde ir pareiza
     */
    public Answer(String answerId, String questionId, String answerText, boolean isCorrect) {
        this.answerId = answerId;
        this.questionId = questionId;
        setAnswerText(answerText);
        this.isCorrect = isCorrect;
        this.orderNumber = 0;
        this.explanation = "";
    }

    /**
     * Konstruktors atbildes izveidei ar secības numuru.
     *
     * @param answerId     atbildes unikālais identifikators
     * @param questionId   jautājuma identifikators
     * @param answerText   atbildes teksts
     * @param isCorrect    norāde, vai atbilde ir pareiza
     * @param orderNumber  secības numurs (0=A, 1=B, 2=C)
     */
    public Answer(String answerId, String questionId, String answerText, boolean isCorrect, int orderNumber) {
        this(answerId, questionId, answerText, isCorrect);
        setOrderNumber(orderNumber);
    }

    /**
     * Konstruktors atbildes izveidei ar secības numuru un paskaidrojumu.
     *
     * @param answerId     atbildes unikālais identifikators
     * @param questionId   jautājuma identifikators
     * @param answerText   atbildes teksts
     * @param isCorrect    norāde, vai atbilde ir pareiza
     * @param orderNumber  secības numurs (0=A, 1=B, 2=C)
     * @param explanation  atbildes paskaidrojums
     */
    public Answer(String answerId, String questionId, String answerText, boolean isCorrect, int orderNumber, String explanation) {
        this(answerId, questionId, answerText, isCorrect, orderNumber);
        setExplanation(explanation);
    }

    /**
     * Iestata atbildes tekstu.
     *
     * @param answerText atbildes teksts
     * @throws IllegalArgumentException ja teksts ir null vai tukšs
     */
    public final void setAnswerText(String answerText) {
        if (answerText == null || answerText.trim().isEmpty()) throw new IllegalArgumentException("Atbildes teksts nevar būt tukšs!");
        this.answerText = answerText.trim();
    }

    /**
     * Iestata atbildes secības numuru.
     *
     * @param orderNumber secības numurs (0=A, 1=B, 2=C)
     * @throws IllegalArgumentException ja secības numurs nav diapazonā 0-2
     */
    public final void setOrderNumber(int orderNumber) {
        if (orderNumber < 0 || orderNumber > 2) throw new IllegalArgumentException("Secības numuram jābūt 0-2!");
        this.orderNumber = orderNumber;
    }

    /**
     * Iestata atbildes paskaidrojumu.
     *
     * @param explanation paskaidrojuma teksts
     */
    public final void setExplanation(String explanation) { this.explanation = (explanation != null) ? explanation : ""; }

    /**
     * Pārbauda, vai lietotāja atbilde sakrīt ar šo atbildi.
     *
     * @param userAnswer lietotāja ievadītā atbilde
     * @return true, ja atbildes sakrīt (ignorējot lielo/mazo burtu atšķirības)
     */
    public boolean checkAnswer(String userAnswer) { return userAnswer != null && answerText.equalsIgnoreCase(userAnswer.trim()); }

    /**
     * Pārbauda, vai lietotāja izvēlētais burts atbilst šīs atbildes secības numuram.
     *
     * @param letter lietotāja izvēlētais burts (A, B vai C)
     * @return true, ja burts atbilst atbildes secības numuram
     */
    public boolean checkAnswer(char letter) {
        if (orderNumber == 0 && (letter == 'A' || letter == 'a')) return true;
        if (orderNumber == 1 && (letter == 'B' || letter == 'b')) return true;
        if (orderNumber == 2 && (letter == 'C' || letter == 'c')) return true;
        return false;
    }

    /**
     * Atgriež formatētu atbildes attēlojumu ar burtu un atzīmi par pareizību.
     *
     * @return formatēts atbildes teksts (piem., "A) [✓] Atbildes teksts")
     */
    public String getDisplayText() {
        char letter = (char)('A' + orderNumber);
        String mark = isCorrect ? "✓" : " ";
        return String.format("%c) [%s] %s", letter, mark, answerText);
    }

    // Getter metodes
    public String getAnswerId() { return answerId; }
    public String getQuestionId() { return questionId; }
    public String getAnswerText() { return answerText; }
    public boolean isCorrect() { return isCorrect; }
    public int getOrderNumber() { return orderNumber; }
    public String getExplanation() { return explanation; }
    public char getLetter() { return (char)('A' + orderNumber); }
    
    @Override
    public String toString() { return getDisplayText(); }
}