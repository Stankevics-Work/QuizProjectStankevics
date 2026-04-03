package pd1_stankevics_24;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class TestFunctions {
    
    @BeforeEach
    public void setUp() {
        System.out.println("TestFunctions: setUp() method");
    }
    
    @AfterEach
    public void tearDown() {
        System.out.println("TestFunctions: tearDown() method");
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testMarkCalculation passed")
    public void testMarkCalculation() {
        System.out.println("TestFunctions: test method 1 - testMarkCalculation()");
        
        Test test = new Test("T1", "Tests", "Topic", null, null);
        
        assertEquals(10, test.calculateMark(100));
        assertEquals(10, test.calculateMark(97));
        assertEquals(9, test.calculateMark(95));
        assertEquals(9, test.calculateMark(92));
        assertEquals(8, test.calculateMark(90));
        assertEquals(8, test.calculateMark(84));
        assertEquals(7, test.calculateMark(80));
        assertEquals(7, test.calculateMark(76));
        assertEquals(6, test.calculateMark(70));
        assertEquals(6, test.calculateMark(68));
        assertEquals(5, test.calculateMark(65));
        assertEquals(5, test.calculateMark(60));
        assertEquals(4, test.calculateMark(55));
        assertEquals(4, test.calculateMark(50));
        assertEquals(3, test.calculateMark(45));
        assertEquals(3, test.calculateMark(40));
        assertEquals(2, test.calculateMark(35));
        assertEquals(2, test.calculateMark(30));
        assertEquals(1, test.calculateMark(25));
        assertEquals(1, test.calculateMark(0));
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testGradeDescription passed")
    public void testGradeDescription() {
        System.out.println("TestFunctions: test method 2 - testGradeDescription()");
        
        Test test = new Test("T1", "Tests", "Topic", null, null);
        
        assertEquals("Izcili (10)", test.getGradeDescription(10));
        assertEquals("Teicami (9)", test.getGradeDescription(9));
        assertEquals("Ļoti labi (8)", test.getGradeDescription(8));
        assertEquals("Labi (7)", test.getGradeDescription(7));
        assertEquals("Gandrīz labi (6)", test.getGradeDescription(6));
        assertEquals("Viduvēji (5)", test.getGradeDescription(5));
        assertEquals("Gandrīz viduvēji (4)", test.getGradeDescription(4));
        assertEquals("Vāji (3)", test.getGradeDescription(3));
        assertEquals("Ļoti vāji (2)", test.getGradeDescription(2));
        assertEquals("Nepietiekami (1)", test.getGradeDescription(1));
        assertEquals("Nav vērtējuma", test.getGradeDescription(0));
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testQuestionAnswerChecking passed")
    public void testQuestionAnswerChecking() {
        System.out.println("TestFunctions: test method 3 - testQuestionAnswerChecking()");
        
        Question q = new Question("Kas ir Java?", "Programmēšanas valoda", "Operētājsistēma", "Datu bāze", 0);
        
        assertTrue(q.isCorrect(0));
        assertFalse(q.isCorrect(1));
        assertFalse(q.isCorrect(2));
        
        assertTrue(q.isCorrect('A'));
        assertFalse(q.isCorrect('B'));
        assertFalse(q.isCorrect('C'));
        
        assertTrue(q.isCorrect("Programmēšanas valoda"));
        assertFalse(q.isCorrect("Nepareiza atbilde"));
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testGetOptionByLetter passed")
    public void testGetOptionByLetter() {
        System.out.println("TestFunctions: test method 4 - testGetOptionByLetter()");
        
        Question q = new Question("Jautājums", "Pirmais", "Otrais", "Trešais", 1);
        
        assertEquals("Pirmais", q.getOptionByLetter('A'));
        assertEquals("Otrais", q.getOptionByLetter('B'));
        assertEquals("Trešais", q.getOptionByLetter('C'));
        assertNull(q.getOptionByLetter('D'));
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testTestEvaluation passed")
    public void testTestEvaluation() {
        System.out.println("TestFunctions: test method 5 - testTestEvaluation()");
        
        Test test = new Test("T1", "Tests", "Topic", null, null);
        test.addQuestion(new Question("J1", "A", "B", "C", 0));
        test.addQuestion(new Question("J2", "A", "B", "C", 1));
        test.addQuestion(new Question("J3", "A", "B", "C", 2));
        
        Result r1 = test.evaluateTest(Arrays.asList(0, 1, 2), "U1");
        assertEquals(3, r1.getScore());
        
        Result r2 = test.evaluateTest(Arrays.asList(0, 0, 0), "U1");
        assertEquals(1, r2.getScore());
        
        Result r3 = test.evaluateTest(Arrays.asList(1, 2, 0), "U1");
        assertEquals(0, r3.getScore());
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testPasswordStrength passed")
    public void testPasswordStrength() {
        System.out.println("TestFunctions: test method 6 - testPasswordStrength()");
        
        assertFalse(User.isPasswordStrong("a"));
        assertFalse(User.isPasswordStrong("abcde"));
        assertTrue(User.isPasswordStrong("abcdef"));
        
        assertFalse(User.isPasswordStrong("abcdef", true));
        assertTrue(User.isPasswordStrong("abc123", true));
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testLoginFunction passed")
    public void testLoginFunction() {
        System.out.println("TestFunctions: test method 7 - testLoginFunction()");
        
        Student s = new Student("Jānis", "janis", "parole123");
        
        assertTrue(s.enter("janis", "parole123"));
        assertFalse(s.enter("janis", "nepareiza"));
        assertFalse(s.enter("nepareizs", "parole123"));
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testResultPassed passed")
    public void testResultPassed() {
        System.out.println("TestFunctions: test method 8 - testResultPassed()");
        
        Result r1 = new Result("R1", "U1", "T1", 80, 100, "8", null);
        assertTrue(r1.isPassed());
        assertTrue(r1.isPassed(70));
        assertFalse(r1.isPassed(85));
        
        Result r2 = new Result("R2", "U1", "T1", 40, 100, "4", null);
        assertFalse(r2.isPassed());
        assertTrue(r2.isPassed(30));
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testProgramFunctions passed")
    public void testProgramFunctions() {
        System.out.println("TestFunctions: test method 9 - testProgramFunctions()");
        
        Program p = new Program("Programmēšana", "IT21");
        
        p.setDurationYears(4);
        assertEquals(4, p.getDurationYears());
        
        assertThrows(IllegalArgumentException.class, () -> {
            p.setDurationYears(7);
        });
        
        Student s = new Student("Jānis", "janis", "pass123");
        p.addStudent(s);
        assertEquals(1, p.getStudentCount());
        
        Teacher t = new Teacher("Skolotājs", "skol", "pass123");
        p.addTeacher(t);
        assertEquals(1, p.getTeacherCount());
    }
}