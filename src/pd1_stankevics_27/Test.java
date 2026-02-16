package pd1_stankevics_27;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Test {
    private String test_id;
    private String title;
    private String topic;
    private User created_by;
    private LocalDateTime date_created;
    private List<Question> questions = new ArrayList<>();

    public Test(String testId, String title, String topic, User createdBy, LocalDateTime dateCreated) {
        this.test_id = testId;
        this.title = title;
        this.topic = topic;
        this.created_by = createdBy;
        this.date_created = dateCreated;
    }

    public void addQuestion(Question question) {
        questions.add(question);
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void assignRole(Role role) {
    }
}