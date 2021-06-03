package org.hyperledger.fabric.samples.assettransfer;


import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.Objects;

@DataType()
public final class Grade {

    @Property()
    private final String gradeId;

    @Property()
    private final Double grade;

    @Property()
    private final String subject;

    @Property()
    private final String teacher;

    @Property()
    private final String student;

    public String getGradeId() {
        return gradeId;
    }

    public Double getGrade() {
        return grade;
    }

    public String getSubject() {
        return subject;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getStudent() {
        return student;
    }

    public Grade(@JsonProperty("gradeId") final String gradeId, @JsonProperty("grade") final Double grade,
                 @JsonProperty("subject") final String subject, @JsonProperty("teacher") final String teacher,
                 @JsonProperty("student") final String student) {
        this.gradeId = gradeId;
        this.grade = grade;
        this.subject = subject;
        this.teacher = teacher;
        this.student = student;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Grade that = (Grade) obj;

        return Objects.deepEquals(
                new String[]{getGradeId(), getSubject(), getTeacher(), getStudent()},
                new String[]{that.getGradeId(), that.getSubject(), that.getTeacher(), that.getStudent()})
                &&
                Objects.deepEquals(
                        new Double[]{getGrade()},
                        new Double[]{that.getGrade()}
                );
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGradeId(), getGrade(), getSubject(), getTeacher(), getStudent());
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
