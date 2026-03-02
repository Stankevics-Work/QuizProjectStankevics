package pd1_stankevics_27;
import java.util.ArrayList;
import java.util.*;

/**
 * Studiju programmas klase.
 * 
 * @author stankevics_27
 * @version 1.0
 */
public class Program {
    private String name;
    private String code;
    private List<Student> students = new ArrayList<>();
    private List<Teacher> teachers = new ArrayList<>();

    /**
     * Izveido jaunu Programmas objektu.
     * 
     * @param name programmas nosaukums
     * @param code programmas kods
     */
    public Program(String name, String code) {
        this.name = name;
        this.code = code;
    }

    /**
     * Pievieno studentu programmai.
     */
    public void addStudent(Student student) {
        students.add(student);
    }

    /**
     * Pievieno pasniedzēju programmai.
     */
    public void addTeacher(Teacher teacher) {
        teachers.add(teacher);
    }

    /**
     * Atgriež visus studentus.
     */
    public List<Student> getStudents() {
        return students;
    }

    /**
     * Atgriež visus pasniedzējus.
     */
    public List<Teacher> getTeachers() {
        return teachers;
    }
}