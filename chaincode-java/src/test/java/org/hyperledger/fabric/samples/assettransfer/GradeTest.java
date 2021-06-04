package org.hyperledger.fabric.samples.assettransfer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GradeTest {

    @Nested
    class Equality {
        @Test
        public void isReflexive() {
            Grade grade = new Grade("Grade1", 2.0, "Math", "Adam Mickiewicz", "John Doe");

            assertThat(grade).isEqualTo(grade);
        }

        @Test
        public void isSymmetric() {
            Grade gradeA = new Grade("Grade1", 2.0, "Math", "Adam Mickiewicz", "John Doe");
            Grade gradeB = new Grade("Grade1", 2.0, "Math", "Adam Mickiewicz", "John Doe");

            assertThat(gradeA).isEqualTo(gradeB);
            assertThat(gradeB).isEqualTo(gradeA);
        }

        @Test
        public void isTransitive() {
            Grade gradeA = new Grade("Grade1", 2.0, "Math", "Adam Mickiewicz", "John Doe");
            Grade gradeB = new Grade("Grade1", 2.0, "Math", "Adam Mickiewicz", "John Doe");
            Grade gradeC = new Grade("Grade1", 2.0, "Math", "Adam Mickiewicz", "John Doe");

            assertThat(gradeA).isEqualTo(gradeB);
            assertThat(gradeB).isEqualTo(gradeC);
            assertThat(gradeC).isEqualTo(gradeA);
        }

        @Test
        public void handlesInequality() {
            Grade gradeA = new Grade("Grade1", 2.0, "Math", "Adam Mickiewicz", "John Doe");
            Grade gradeB = new Grade("Grade2", 2.5, "Math", "Adam Mickiewicz", "John Doe");

            assertThat(gradeA).isNotEqualTo(gradeB);
        }

        @Test
        public void handlesOtherObjects() {
            Grade gradeA = new Grade("Grade1", 2.0, "Math", "Adam Mickiewicz", "John Doe");
            String gradeB = "not a grade";
            assertThat(gradeA).isNotEqualTo(gradeB);
        }

        @Test
        public void handlesNull() {
            Grade gradeA = new Grade("Grade1", 2.0, "Math", "Adam Mickiewicz", "John Doe");

            assertThat(gradeA).isNotEqualTo(null);
        }

        @Test
        public void shouldToStringGrade() {
            Grade gradeA = new Grade("Grade1", 2.0, "Math", "Adam Mickiewicz", "John Doe");
            assertThat(gradeA.toString()).isEqualTo("Grade@1016e4ec[gradeId = Grade1, grade = 2.0, subject = Math, teacher = Adam Mickiewicz, student = John Doe]");
        }
    }
}
