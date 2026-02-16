package pd1_stankevics_27;
import java.util.ArrayList;
import java.util.*;

public class Program {
    private String name;
    private String code;
    private List<Student> students = new ArrayList<>();
    private List<Teacher> teachers = new ArrayList<>();

    public Program(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public void addStudent(Student student) {
        students.add(student);
    }

    public void addTeacher(Teacher teacher) {
        teachers.add(teacher);
    }

    public List<Student> getStudents() {
        return students;
    }

    public List<Teacher> getTeachers() {
        return teachers;
    }
}