package pd1_stankevics_24;

/**
 * Testa jautājuma klase, kas satur jautājuma tekstu, trīs atbilžu variantus,
 * norādi uz pareizo atbildi, punktu skaitu un grūtības pakāpi.
 * 
 * @author armins.stankevics_24
 */
public class Question {
    private String questionId;
    private String testId;
    private String text;
    private String[] options;
    private int correctIndex;
    private String explanation;
    private String category;
    private int points;
    private String difficulty;
    private String createdBy;
    private boolean isActive;

    public static final String DIFFICULTY_EASY = "Viegla";
    public static final String DIFFICULTY_MEDIUM = "Vidēja";
    public static final String DIFFICULTY_HARD = "Grūta";

    /**
     * Noklusējuma konstruktors, izveido tukšu jautājumu ar automātiski ģenerētu ID.
     */
    public Question() {
        this.questionId = generateQuestionId();
        this.text = "";
        this.options = new String[]{"", "", ""};
        this.correctIndex = 0;
        this.explanation = "";
        this.category = "Vispārīgi";
        this.points = 1;
        this.difficulty = DIFFICULTY_MEDIUM;
        this.testId = "0";
        this.createdBy = "Sistēma";
        this.isActive = true;
    }

    /**
     * Konstruktors jautājuma izveidei ar pamatinformāciju.
     *
     * @param text         jautājuma teksts
     * @param option1      pirmais atbildes variants (A)
     * @param option2      otrais atbildes variants (B)
     * @param option3      trešais atbildes variants (C)
     * @param correctIndex pareizās atbildes indekss (0=A, 1=B, 2=C)
     */
    public Question(String text, String option1, String option2, String option3, int correctIndex) {
        this(text, option1, option2, option3, correctIndex, "", "Vispārīgi", 1, DIFFICULTY_MEDIUM);
    }

    /**
     * Konstruktors jautājuma izveidei ar visiem parametriem.
     *
     * @param text         jautājuma teksts
     * @param option1      pirmais atbildes variants (A)
     * @param option2      otrais atbildes variants (B)
     * @param option3      trešais atbildes variants (C)
     * @param correctIndex pareizās atbildes indekss (0=A, 1=B, 2=C)
     * @param explanation  jautājuma paskaidrojums
     * @param category     jautājuma kategorija
     * @param points       punktu skaits par pareizu atbildi
     * @param difficulty   grūtības pakāpe (Viegla, Vidēja, Grūta)
     */
    public Question(String text, String option1, String option2, String option3, 
                   int correctIndex, String explanation, String category, 
                   int points, String difficulty) {
        this.questionId = generateQuestionId();
        setText(text);
        setOptions(option1, option2, option3);
        setCorrectIndex(correctIndex);
        setExplanation(explanation);
        setCategory(category);
        setPoints(points);
        setDifficulty(difficulty);
        this.testId = "0";
        this.createdBy = "Sistēma";
        this.isActive = true;
    }

    /**
     * Ģenerē unikālu jautājuma identifikatoru.
     *
     * @return unikāls jautājuma ID
     */
    private String generateQuestionId() { return "Q" + System.currentTimeMillis() + (int)(Math.random() * 1000); }

    /**
     * Iestata jautājuma tekstu.
     *
     * @param text jautājuma teksts
     * @throws IllegalArgumentException ja teksts ir null vai tukšs
     */
    public final void setText(String text) {
        if (text == null || text.trim().isEmpty()) throw new IllegalArgumentException("Jautājuma teksts nevar būt tukšs!");
        this.text = text.trim();
    }

    /**
     * Iestata atbilžu variantus.
     *
     * @param option1 pirmais atbildes variants (A)
     * @param option2 otrais atbildes variants (B)
     * @param option3 trešais atbildes variants (C)
     * @throws IllegalArgumentException ja kāds variants ir tukšs vai varianti nav unikāli
     */
    public final void setOptions(String option1, String option2, String option3) {
        if (option1 == null || option1.trim().isEmpty() || option2 == null || option2.trim().isEmpty() || option3 == null || option3.trim().isEmpty()) {
            throw new IllegalArgumentException("Visi atbilžu varianti ir obligāti!");
        }
        String opt1 = option1.trim(), opt2 = option2.trim(), opt3 = option3.trim();
        if (opt1.equals(opt2) || opt1.equals(opt3) || opt2.equals(opt3)) {
            throw new IllegalArgumentException("Atbilžu variantiem jābūt unikāliem!");
        }
        this.options = new String[]{opt1, opt2, opt3};
    }

    /**
     * Iestata pareizās atbildes indeksu.
     *
     * @param correctIndex pareizās atbildes indekss (0=A, 1=B, 2=C)
     * @throws IllegalArgumentException ja indekss nav diapazonā 0-2
     */
    public final void setCorrectIndex(int correctIndex) {
        if (correctIndex < 0 || correctIndex > 2) throw new IllegalArgumentException("Pareizās atbildes indeksam jābūt 0, 1 vai 2!");
        this.correctIndex = correctIndex;
    }

    /**
     * Iestata jautājuma paskaidrojumu.
     *
     * @param explanation paskaidrojuma teksts
     */
    public final void setExplanation(String explanation) { this.explanation = (explanation != null) ? explanation : ""; }

    /**
     * Iestata jautājuma kategoriju.
     *
     * @param category jautājuma kategorija
     */
    public final void setCategory(String category) { this.category = (category != null && !category.trim().isEmpty()) ? category.trim() : "Vispārīgi"; }

    /**
     * Iestata punktu skaitu par pareizu atbildi.
     *
     * @param points punktu skaits
     * @throws IllegalArgumentException ja punktu skaits nav pozitīvs
     */
    public final void setPoints(int points) { if (points <= 0) throw new IllegalArgumentException("Punktu skaitam jābūt pozitīvam!"); this.points = points; }

    /**
     * Iestata jautājuma grūtības pakāpi.
     *
     * @param difficulty grūtības pakāpe (Viegla, Vidēja, Grūta)
     */
    public final void setDifficulty(String difficulty) {
        if (difficulty == null || difficulty.trim().isEmpty()) this.difficulty = DIFFICULTY_MEDIUM;
        else {
            String d = difficulty.trim();
            if (!d.equals(DIFFICULTY_EASY) && !d.equals(DIFFICULTY_MEDIUM) && !d.equals(DIFFICULTY_HARD)) this.difficulty = DIFFICULTY_MEDIUM;
            else this.difficulty = d;
        }
    }

    /**
     * Iestata testa ID, kuram pieder jautājums.
     *
     * @param testId testa identifikators
     */
    public void setTestId(String testId) { this.testId = (testId != null && !testId.trim().isEmpty()) ? testId.trim() : "0"; }

    /**
     * Iestata jautājuma autora lietotājvārdu.
     *
     * @param createdBy autora lietotājvārds
     */
    public void setCreatedBy(String createdBy) { this.createdBy = (createdBy != null && !createdBy.trim().isEmpty()) ? createdBy.trim() : "Sistēma"; }

    /**
     * Iestata jautājuma aktīvā statusu.
     *
     * @param active true, ja jautājums ir aktīvs
     */
    public void setActive(boolean active) { this.isActive = active; }

    /**
     * Pārbauda, vai izvēlētais atbildes indekss ir pareizs.
     *
     * @param selectedIndex izvēlētais atbildes indekss (0=A, 1=B, 2=C)
     * @return true, ja indekss atbilst pareizajai atbildei
     */
    public boolean isCorrect(int selectedIndex) { return selectedIndex == correctIndex; }

    /**
     * Pārbauda, vai izvēlētais atbildes teksts ir pareizs.
     *
     * @param selectedText izvēlētais atbildes teksts
     * @return true, ja teksts atbilst pareizajai atbildei
     */
    public boolean isCorrect(String selectedText) { return selectedText != null && options[correctIndex].equalsIgnoreCase(selectedText.trim()); }

    /**
     * Pārbauda, vai izvēlētais burts atbilst pareizajai atbildei.
     *
     * @param letter izvēlētais burts (A, B vai C)
     * @return true, ja burts atbilst pareizajai atbildei
     */
    public boolean isCorrect(char letter) {
        int index = -1;
        if (letter == 'A' || letter == 'a') index = 0;
        else if (letter == 'B' || letter == 'b') index = 1;
        else if (letter == 'C' || letter == 'c') index = 2;
        else return false;
        return index == correctIndex;
    }

    /**
     * Atgriež atbildes variantu pēc burta.
     *
     * @param letter atbildes burts (A, B vai C)
     * @return atbildes teksts vai null, ja burts nav derīgs
     */
    public String getOptionByLetter(char letter) {
        int index = -1;
        if (letter == 'A' || letter == 'a') index = 0;
        else if (letter == 'B' || letter == 'b') index = 1;
        else if (letter == 'C' || letter == 'c') index = 2;
        if (index == -1 || index >= options.length) return null;
        return options[index];
    }

    /**
     * Atgriež formatētu jautājuma attēlojumu testa veikšanai.
     *
     * @param questionNumber jautājuma kārtas numurs testā
     * @return formatēts jautājuma teksts ar atbilžu variantiem
     */
    public String getDisplayText(int questionNumber) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════\n");
        sb.append(String.format("   %d. [%s] %s\n", questionNumber, difficulty, text));
        sb.append("───────────────────────────────────────────────────────\n");
        for (int i = 0; i < options.length; i++) sb.append(String.format("   %c) %s\n", (char)('A' + i), options[i]));
        sb.append("═══════════════════════════════════════════════════════");
        return sb.toString();
    }

    /**
     * Atgriež jautājuma detalizēto informāciju formatētā veidā.
     *
     * @return formatēta jautājuma informācija
     */
    public String getQuestionDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════╗\n");
        sb.append("║                   JAUTĀJUMS                        ║\n");
        sb.append("╠════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ ID: %-43s ║\n", questionId));
        sb.append(String.format("║ Teksts: %-43s ║\n", text.length() > 30 ? text.substring(0, 27) + "..." : text));
        sb.append(String.format("║ Punkti: %-39d ║\n", points));
        sb.append(String.format("║ Grūtība: %-38s ║\n", difficulty));
        sb.append("╚════════════════════════════════════════════════════╝");
        return sb.toString();
    }

    /**
     * Pārbauda, vai jautājums ir pilnībā aizpildīts un derīgs.
     *
     * @return true, ja visi obligātie lauki ir aizpildīti korekti
     */
    public boolean isValid() {
        return text != null && !text.isEmpty() && options != null && options.length == 3 &&
               options[0] != null && !options[0].isEmpty() && options[1] != null && !options[1].isEmpty() &&
               options[2] != null && !options[2].isEmpty() && correctIndex >= 0 && correctIndex <= 2;
    }

    // Getter metodes
    public String getQuestionId() { return questionId; }
    public String getTestId() { return testId; }
    public String getText() { return text; }
    public String[] getOptions() { return options.clone(); }
    public String getOptionAt(int index) { return options[index]; }
    public int getCorrectIndex() { return correctIndex; }
    public String getCorrectAnswer() { return options[correctIndex]; }
    public String getExplanation() { return explanation; }
    public String getCategory() { return category; }
    public int getPoints() { return points; }
    public String getDifficulty() { return difficulty; }
    public String getCreatedBy() { return createdBy; }
    public boolean isActive() { return isActive; }
    
    @Override
    public String toString() { return String.format("Question[%s] %s", questionId.substring(0, Math.min(8, questionId.length())), text.substring(0, Math.min(30, text.length())) + "..."); }
}