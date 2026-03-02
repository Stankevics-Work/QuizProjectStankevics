package pd1_stankevics_27;

/**
 * Testa jautājuma klase.
 * 
 * @author stankevics_27
 * @version 2.0
 */
public class Question {
    private String text;
    private String[] options;
    private int correctIndex;

    /**
     * Izveido jaunu jautājuma objektu.
     * 
     * @param text jautājuma teksts
     * @param option1 pirmais atbildes variants
     * @param option2 otrais atbildes variants
     * @param option3 trešais atbildes variants
     * @param correctIndex pareizās atbildes indekss
     */
    public Question(String text, String option1, String option2, String option3, int correctIndex) {
        this.text = text;
        this.options = new String[]{ option1, option2, option3 };
        this.correctIndex = correctIndex;
    }

    /**
     * Atgriež jautājuma tekstu.
     */
    public String getText() {
        return text;
    }

    /**
     * Atgriež visus atbilžu variantus.
     */
    public String[] getOptions() {
        return options;
    }

    /**
     * Atgriež konkrētu atbildes variantu.
     * 
     * @param index atbildes indekss
     */
    public String getOptionAt(int index) {
        return options[index];
    }

    /**
     * Pārbauda vai atbilde ir pareiza.
     * 
     * @param selectedIndex izvēlētās atbildes indekss
     */
    public boolean isCorrect(int selectedIndex) {
        return selectedIndex == correctIndex;
    }

    /**
     * Atgriež formatētu jautājuma tekstu ar numuru.
     * 
     * @param questionNumber jautājuma numurs
     */
    public String getDisplayText(int questionNumber) {
        return questionNumber + ". " + text;
    }
}