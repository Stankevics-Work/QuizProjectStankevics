package pd1_stankevics_24;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class TestErrorCheck {
    
    @BeforeEach
    public void setUp() {
        System.out.println("TestErrorCheck: setUp() method");
    }
    
    @AfterEach
    public void tearDown() {
        System.out.println("TestErrorCheck: tearDown() method");
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testTestClassInvalidParameters passed")
    public void testTestClassInvalidParameters() {
        System.out.println("TestErrorCheck: test method 1 - testTestClassInvalidParameters()");
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Test(null, "Title", "Topic", null, null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Test("", "Title", "Topic", null, null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Test("T1", null, "Topic", null, null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Test("T1", "", "Topic", null, null);
        });
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testQuestionClassInvalidParameters passed")
    public void testQuestionClassInvalidParameters() {
        System.out.println("TestErrorCheck: test method 2 - testQuestionClassInvalidParameters()");
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Question("", "A", "B", "C", 0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Question("Teksts", "", "B", "C", 0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Question("Teksts", "A", "B", "C", 5);
        });
        Question q = new Question("Teksts", "A", "B", "C", 0);
        assertThrows(IllegalArgumentException.class, () -> {
            q.setPoints(0);
        });
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testUserClassInvalidParameters passed")
    public void testUserClassInvalidParameters() {
        System.out.println("TestErrorCheck: test method 3 - testUserClassInvalidParameters()");
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Student("", "login", "pass123");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Student("Vārds", "", "pass123");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Student("Vārds", "ab", "pass123");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Student("Vārds", "login", "");
        });
        Student s = new Student("Vārds", "login", "pass123");
        assertThrows(IllegalArgumentException.class, () -> {
            s.setCourse(5);
        });
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testEvaluateErrors passed")
    public void testEvaluateErrors() {
        System.out.println("TestErrorCheck: test method 4 - testEvaluateErrors()");
        
        Test test = new Test("T1", "Tests", "Topic", null, null);
        test.addQuestion(new Question("J1", "A", "B", "C", 0));
        
        assertThrows(IllegalArgumentException.class, () -> {
            test.evaluateTest(null, "user1");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            List<Integer> answers = Arrays.asList(0, 0);
            test.evaluateTest(answers, "user1");
        });
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testStudentTestErrors passed")
    public void testStudentTestErrors() {
        System.out.println("TestErrorCheck: test method 5 - testStudentTestErrors()");
        
        Student student = new Student("Vārds", "login", "pass123");
        Test test = new Test("T1", "Tests", "Topic", null, null);
        test.addQuestion(new Question("J1", "A", "B", "C", 0));
        
        assertThrows(IllegalStateException.class, () -> {
            student.finishTest();
        });
        
        student.startTest(test);
        
        assertThrows(IllegalArgumentException.class, () -> {
            student.answerQuestion(5);
        });
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testDatabaseErrors passed")
    public void testDatabaseErrors() {
        System.out.println("TestErrorCheck: test method 6 - testDatabaseErrors()");
        
        Student student = new Student("Vārds", "login", "pass123");
        
        boolean result = student.saveTestResultToDatabase(null);
        assertFalse(result);
        
        List<Result> results = student.loadResultsFromDatabase(null);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}