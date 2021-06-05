package application.java;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Grade {
    @JsonProperty("gradeId")
    private String gradeId;
    @JsonProperty("grade")
    private Double grade;
    @JsonProperty("subject")
    private String subject;
    @JsonProperty("teacher")
    private String teacher;
    @JsonProperty("student")
    private String student;

    public String getGradeId() {
        return gradeId;
    }

    public void setGradeId(String gradeId) {
        this.gradeId = gradeId;
    }

    public Double getGradeValue() {
        return grade;
    }

    public void setGradeValue(Double gradeValue) {
        this.grade = gradeValue;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    public Grade() {
    }

    public Grade(String gradeId, Double gradeValue, String subject, String teacher, String student) {
        this.gradeId = gradeId;
        this.grade = gradeValue;
        this.subject = subject;
        this.teacher = teacher;
        this.student = student;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        Grade that = (Grade) o;

        return Objects.deepEquals(
                new String[]{getGradeId(), getSubject(), getTeacher(), getStudent()},
                new String[]{that.getGradeId(), that.getSubject(), that.getTeacher(), that.getStudent()})
                &&
                Objects.deepEquals(
                        new Double[]{getGradeValue()},
                        new Double[]{that.getGradeValue()});
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGradeId(), getGradeValue(), getSubject(), getTeacher(), getStudent());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode())
                + "[gradeId = " + gradeId
                + ", grade = " + grade
                + ", subject = " + subject
                + ", teacher = " + teacher
                + ", student = " + student + "]";
    }

}
