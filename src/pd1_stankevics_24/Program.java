package pd1_stankevics_24;

import java.util.ArrayList;
import java.util.List;

/**
 * Studiju programmas klase, kas apvieno studentus, pasniedzējus un testus
 * vienā akadēmiskā vienībā. Katrai programmai ir savs nosaukums, kods,
 * apraksts, ilgums un tai piesaistītie lietotāji.
 *
 * @author armins.stankevics_24
 * @see Student
 * @see Teacher
 * @see Test
 */
public class Program {
    private String programId;
    private String name;
    private String code;
    private String description;
    private int durationYears;
    private List<Student> students;
    private List<Teacher> teachers;
    private List<Test> programTests;

    /**
     * Izveido jaunu Programmas objektu ar minimālo informāciju.
     * Programmas ID tiek ģenerēts automātiski.
     *
     * @param name programmas nosaukums
     * @param code programmas kods (piemēram, "IT21")
     */
    public Program(String name, String code) {
        this.programId = "PRG" + System.currentTimeMillis();
        this.name = name;
        this.code = code;
        this.description = "";
        this.durationYears = 3;
        this.students = new ArrayList<>();
        this.teachers = new ArrayList<>();
        this.programTests = new ArrayList<>();
    }

    /**
     * Izveido jaunu Programmas objektu ar visiem parametriem.
     *
     * @param programId     unikāls programmas identifikators
     * @param name          programmas nosaukums
     * @param code          programmas kods
     * @param description   programmas apraksts
     * @param durationYears programmas ilgums gados (1-6)
     * @throws IllegalArgumentException ja durationYears nav diapazonā 1-6
     */
    public Program(String programId, String name, String code, String description, int durationYears) {
        this.programId = programId;
        this.name = name;
        this.code = code;
        setDescription(description);
        setDurationYears(durationYears);
        this.students = new ArrayList<>();
        this.teachers = new ArrayList<>();
        this.programTests = new ArrayList<>();
    }

    /**
     * Iestata programmas aprakstu.
     *
     * @param description programmas apraksts (null tiks konvertēts uz tukšu String)
     */
    public final void setDescription(String description) {
        this.description = (description != null) ? description : "";
    }

    /**
     * Iestata programmas ilgumu gados.
     *
     * @param durationYears ilgums gados (1-6)
     * @throws IllegalArgumentException ja ilgums nav diapazonā 1-6
     */
    public final void setDurationYears(int durationYears) {
        if (durationYears < 1 || durationYears > 6) {
            throw new IllegalArgumentException("Programmas ilgumam jābūt 1-6 gadi!");
        }
        this.durationYears = durationYears;
    }

    /**
     * Pievieno studentu programmai.
     *
     * @param student pievienojamais students (nedrīkst būt null)
     */
    public void addStudent(Student student) {
        if (student != null && !students.contains(student)) {
            students.add(student);
        }
    }

    /**
     * Noņem studentu no programmas pēc lietotājvārda.
     *
     * @param studentLogin studenta lietotājvārds
     * @return {@code true} ja students tika atrasts un noņemts
     */
    public boolean removeStudent(String studentLogin) {
        return students.removeIf(s -> s.getLogin().equals(studentLogin));
    }

    /**
     * Pievieno pasniedzēju programmai.
     *
     * @param teacher pievienojamais pasniedzējs (nedrīkst būt null)
     */
    public void addTeacher(Teacher teacher) {
        if (teacher != null && !teachers.contains(teacher)) {
            teachers.add(teacher);
        }
    }

    /**
     * Noņem pasniedzēju no programmas pēc lietotājvārda.
     *
     * @param teacherLogin pasniedzēja lietotājvārds
     * @return {@code true} ja pasniedzējs tika atrasts un noņemts
     */
    public boolean removeTeacher(String teacherLogin) {
        return teachers.removeIf(t -> t.getLogin().equals(teacherLogin));
    }

    /**
     * Pievieno testu programmai.
     *
     * @param test pievienojamais tests (nedrīkst būt null)
     */
    public void addTest(Test test) {
        if (test != null && !programTests.contains(test)) {
            programTests.add(test);
        }
    }

    /**
     * Noņem testu no programmas pēc testa ID.
     *
     * @param testId testa identifikators
     * @return {@code true} ja tests tika atrasts un noņemts
     */
    public boolean removeTest(String testId) {
        return programTests.removeIf(t -> t.getTestId().equals(testId));
    }

    /**
     * Atgriež programmas kopsavilkumu formatētā veidā ar apmalēm.
     *
     * @return formatēta programmas informācija
     */
    public String getProgramSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════╗\n");
        sb.append("║              STUDIJU PROGRAMA                     ║\n");
        sb.append("╠════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ Nosaukums: %-39s ║\n", name));
        sb.append(String.format("║ Kods:      %-39s ║\n", code));
        sb.append(String.format("║ Ilgums:    %-39d gadi ║\n", durationYears));
        sb.append(String.format("║ Studenti:  %-39d ║\n", students.size()));
        sb.append(String.format("║ Pasniedzēji: %-36d ║\n", teachers.size()));
        sb.append(String.format("║ Testi:     %-39d ║\n", programTests.size()));
        
        if (!description.isEmpty()) {
            sb.append("╠════════════════════════════════════════════════════╣\n");
            sb.append("║ APRAKSTS:                                         ║\n");
            sb.append(String.format("║ %-53s ║\n", description));
        }
        
        sb.append("╚════════════════════════════════════════════════════╝");
        return sb.toString();
    }

    // Getter metodes
    /**
     * Atgriež programmas unikālo identifikatoru.
     *
     * @return programmas ID
     */
    public String getProgramId() { return programId; }

    /**
     * Atgriež programmas nosaukumu.
     *
     * @return programmas nosaukums
     */
    public String getName() { return name; }

    /**
     * Atgriež programmas kodu.
     *
     * @return programmas kods
     */
    public String getCode() { return code; }

    /**
     * Atgriež programmas aprakstu.
     *
     * @return programmas apraksts
     */
    public String getDescription() { return description; }

    /**
     * Atgriež programmas ilgumu gados.
     *
     * @return ilgums gados
     */
    public int getDurationYears() { return durationYears; }

    /**
     * Atgriež programmai piesaistīto studentu saraksta kopiju.
     *
     * @return studentu saraksts
     */
    public List<Student> getStudents() { return new ArrayList<>(students); }

    /**
     * Atgriež programmai piesaistīto pasniedzēju saraksta kopiju.
     *
     * @return pasniedzēju saraksts
     */
    public List<Teacher> getTeachers() { return new ArrayList<>(teachers); }

    /**
     * Atgriež programmai piesaistīto testu saraksta kopiju.
     *
     * @return testu saraksts
     */
    public List<Test> getProgramTests() { return new ArrayList<>(programTests); }

    /**
     * Atgriež programmai piesaistīto studentu skaitu.
     *
     * @return studentu skaits
     */
    public int getStudentCount() { return students.size(); }

    /**
     * Atgriež programmai piesaistīto pasniedzēju skaitu.
     *
     * @return pasniedzēju skaits
     */
    public int getTeacherCount() { return teachers.size(); }

    /**
     * Atgriež programmai piesaistīto testu skaitu.
     *
     * @return testu skaits
     */
    public int getTestCount() { return programTests.size(); }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}