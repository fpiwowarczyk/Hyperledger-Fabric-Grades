package org.hyperledger.fabric.samples.gradecontroller;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GradeTest {

    @Nested
    class Equality {
        @Test
        public void isReflexive() {
            Grade grade = new Grade("Grade1", 2.0, List.of("Adam Mickiewicz"), "Math", "Adam Mickiewicz", "John Doe");

            assertThat(grade).isEqualTo(grade);
        }

        @Test
        public void isSymmetric() {
            Grade gradeA = new Grade("Grade1", 2.0, List.of("Adam Mickiewicz"), "Math", "Adam Mickiewicz", "John Doe");
            Grade gradeB = new Grade("Grade1", 2.0, List.of("Adam Mickiewicz"), "Math", "Adam Mickiewicz", "John Doe");

            assertThat(gradeA).isEqualTo(gradeB);
            assertThat(gradeB).isEqualTo(gradeA);
        }

        @Test
        public void isTransitive() {
            Grade gradeA = new Grade("Grade1", 2.0, List.of("Adam Mickiewicz"), "Math", "Adam Mickiewicz", "John Doe");
            Grade gradeB = new Grade("Grade1", 2.0, List.of("Adam Mickiewicz"), "Math", "Adam Mickiewicz", "John Doe");
            Grade gradeC = new Grade("Grade1", 2.0, List.of("Adam Mickiewicz"), "Math", "Adam Mickiewicz", "John Doe");

            assertThat(gradeA).isEqualTo(gradeB);
            assertThat(gradeB).isEqualTo(gradeC);
            assertThat(gradeC).isEqualTo(gradeA);
        }

        @Test
        public void handlesInequality() {
            Grade gradeA = new Grade("Grade1", 2.0, List.of("Adam Mickiewicz"), "Math", "Adam Mickiewicz", "John Doe");
            Grade gradeB = new Grade("Grade2", 2.5, List.of("Adam Mickiewicz"), "Math", "Adam Mickiewicz", "John Doe");

            assertThat(gradeA).isNotEqualTo(gradeB);
        }

        @Test
        public void handlesOtherObjects() {
            Grade gradeA = new Grade("Grade1", 2.0, List.of("Adam Mickiewicz"), "Math", "Adam Mickiewicz", "John Doe");
            String gradeB = "not a grade";
            assertThat(gradeA).isNotEqualTo(gradeB);
        }

        @Test
        public void handlesNull() {
            Grade gradeA = new Grade("Grade1", 2.0, List.of("Adam Mickiewicz"), "Math", "Adam Mickiewicz", "John Doe");

            assertThat(gradeA).isNotEqualTo(null);
        }

        @Test
        public void shouldToStringGrade() {
            Grade gradeA = new Grade("Grade1", 2.0, List.of("Adam Mickiewicz"), "Math", "Adam Mickiewicz", "John Doe");
            assertThat(gradeA.toString()).isEqualTo("Grade@61a7d841[gradeId = Grade1, grade = 2.0, visitors = [Adam Mickiewicz], subject = Math, teacher = Adam Mickiewicz, student = John Doe]");
        }
    }
}
