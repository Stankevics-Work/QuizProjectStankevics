package pd1_stankevics_24;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestInterfaceButtons {
    
    @BeforeEach
    public void setUp() {
        System.out.println("TestInterfaceButtons: setUp() method");
    }
    
    @AfterEach
    public void tearDown() {
        System.out.println("TestInterfaceButtons: tearDown() method");
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testTestClassExists passed")
    public void testTestClassExists() {
        System.out.println("TestInterfaceButtons: test method 1 - testTestClassExists()");
        try {
            Class.forName("pd1_stankevics_24.Test");
            assertTrue(true);
        } catch (ClassNotFoundException e) {
            fail("Test klase nav atrasta!");
        }
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testInterfaceClassExists passed")
    public void testInterfaceClassExists() {
        System.out.println("TestInterfaceButtons: test method 2 - testInterfaceClassExists()");
        try {
            Class.forName("pd1_stankevics_24.Interface");
            assertTrue(true);
        } catch (ClassNotFoundException e) {
            fail("Interface klase nav atrasta!");
        }
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testQuestionClassExists passed")
    public void testQuestionClassExists() {
        System.out.println("TestInterfaceButtons: test method 3 - testQuestionClassExists()");
        try {
            Class.forName("pd1_stankevics_24.Question");
            assertTrue(true);
        } catch (ClassNotFoundException e) {
            fail("Question klase nav atrasta!");
        }
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testStudentClassExists passed")
    public void testStudentClassExists() {
        System.out.println("TestInterfaceButtons: test method 4 - testStudentClassExists()");
        try {
            Class.forName("pd1_stankevics_24.Student");
            assertTrue(true);
        } catch (ClassNotFoundException e) {
            fail("Student klase nav atrasta!");
        }
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testTeacherClassExists passed")
    public void testTeacherClassExists() {
        System.out.println("TestInterfaceButtons: test method 5 - testTeacherClassExists()");
        try {
            Class.forName("pd1_stankevics_24.Teacher");
            assertTrue(true);
        } catch (ClassNotFoundException e) {
            fail("Teacher klase nav atrasta!");
        }
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testAdminClassExists passed")
    public void testAdminClassExists() {
        System.out.println("TestInterfaceButtons: test method 6 - testAdminClassExists()");
        try {
            Class.forName("pd1_stankevics_24.Admin");
            assertTrue(true);
        } catch (ClassNotFoundException e) {
            fail("Admin klase nav atrasta!");
        }
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testResultClassExists passed")
    public void testResultClassExists() {
        System.out.println("TestInterfaceButtons: test method 7 - testResultClassExists()");
        try {
            Class.forName("pd1_stankevics_24.Result");
            assertTrue(true);
        } catch (ClassNotFoundException e) {
            fail("Result klase nav atrasta!");
        }
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testProgramClassExists passed")
    public void testProgramClassExists() {
        System.out.println("TestInterfaceButtons: test method 8 - testProgramClassExists()");
        try {
            Class.forName("pd1_stankevics_24.Program");
            assertTrue(true);
        } catch (ClassNotFoundException e) {
            fail("Program klase nav atrasta!");
        }
    }
    
    @org.junit.jupiter.api.Test
    @DisplayName("testAuthServiceClassExists passed")
    public void testAuthServiceClassExists() {
        System.out.println("TestInterfaceButtons: test method 9 - testAuthServiceClassExists()");
        try {
            Class.forName("pd1_stankevics_24.AuthService");
            assertTrue(true);
        } catch (ClassNotFoundException e) {
            fail("AuthService klase nav atrasta!");
        }
    }
}