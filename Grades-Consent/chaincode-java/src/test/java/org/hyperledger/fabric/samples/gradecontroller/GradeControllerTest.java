package org.hyperledger.fabric.samples.gradecontroller;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.anyString;

public final class GradeControllerTest {

    private final class MockKeyValue implements KeyValue {

        private final String key;
        private final String value;

        MockKeyValue(final String key, final String value) {
            super();
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getStringValue() {
            return this.value;
        }

        @Override
        public byte[] getValue() {
            return this.value.getBytes();
        }

    }

    private final class MockGradeResultsIteratorDifferentStudents implements QueryResultsIterator<KeyValue> {

        private final List<KeyValue> gradesList;

        MockGradeResultsIteratorDifferentStudents() {
            super();

            gradesList = new ArrayList<KeyValue>();

            gradesList.add(new MockKeyValue("grade1",
                    "{ \"gradeId\": \"Filip Piwowarczyk0\", \"grade\": 2.0,\"visitors\":[\"Adam Mickiewicz\"], \"subject\": \"Math\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Filip Piwowarczyk\"}"));
            gradesList.add(new MockKeyValue("grade1",
                    "{ \"gradeId\": \"Filip Piwowarczyk1\", \"grade\": 3.0,\"visitors\":[\"Adam Mickiewicz\"], \"subject\": \"PE\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Filip Piwowarczyk\"}"));
            gradesList.add(new MockKeyValue("grade1",
                    "{ \"gradeId\": \"Filip Piwowarczyk2\", \"grade\": 5.0,\"visitors\":[\"Adam Mickiewicz\"], \"subject\": \"History\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Filip Piwowarczyk\"}"));
            gradesList.add(new MockKeyValue("grade1",
                    "{ \"gradeId\": \"Filip Piwowarczyk3\", \"grade\": 4.5,\"visitors\":[\"Adam Mickiewicz\"], \"subject\": \"Math\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Filip Piwowarczyk\"}"));
            gradesList.add(new MockKeyValue("grade1",
                    "{ \"gradeId\": \"Filip Piwowarczyk4\", \"grade\": 4.0,\"visitors\":[\"Adam Mickiewicz\"], \"subject\": \"Math\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Filip Piwowarczyk\"}"));
            gradesList.add(new MockKeyValue("grade1",
                    "{ \"gradeId\": \"Ola Piwowarczyk0\", \"grade\": 4.0,\"visitors\":[\"Adam Mickiewicz\"], \"subject\": \"Math\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Ola Piwowarczyk\"}"));
        }

        @Override
        public Iterator<KeyValue> iterator() {
            return gradesList.iterator();
        }

        @Override
        public void close() throws Exception {
            // do nothing
        }
    }

    private final class MockGradeResultsIteratorOneStudent implements QueryResultsIterator<KeyValue> {

        private final List<KeyValue> gradesList;

        MockGradeResultsIteratorOneStudent() {
            super();

            gradesList = new ArrayList<KeyValue>();

            gradesList.add(new MockKeyValue("grade1",
                    "{ \"gradeId\": \"Filip Piwowarczyk0\", \"grade\": 2.0, \"visitors\":[\"Adam Mickiewicz\"], \"subject\": \"Math\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Filip Piwowarczyk\"}"));
            gradesList.add(new MockKeyValue("grade1",
                    "{ \"gradeId\": \"Filip Piwowarczyk1\", \"grade\": 3.0, \"visitors\":[\"Adam Mickiewicz\"], \"subject\": \"PE\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Filip Piwowarczyk\"}"));
            gradesList.add(new MockKeyValue("grade1",
                    "{ \"gradeId\": \"Filip Piwowarczyk2\", \"grade\": 5.0, \"visitors\":[\"Adam Mickiewicz\"], \"subject\": \"History\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Filip Piwowarczyk\"}"));
            gradesList.add(new MockKeyValue("grade1",
                    "{ \"gradeId\": \"Filip Piwowarczyk3\", \"grade\": 4.5, \"visitors\":[\"Adam Mickiewicz\"], \"subject\": \"Math\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Filip Piwowarczyk\"}"));
            gradesList.add(new MockKeyValue("grade1",
                    "{ \"gradeId\": \"Filip Piwowarczyk4\", \"grade\": 4.0,\"visitors\":[\"Adam Mickiewicz\"], \"subject\": \"Math\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Filip Piwowarczyk\"}"));
        }


        @Override
        public Iterator<KeyValue> iterator() {
            return gradesList.iterator();
        }

        @Override
        public void close() throws Exception {
            // do nothing
        }
    }

    @Test
    public void invokeUnknownTransaction() {
        GradeController contract = new GradeController();
        Context ctx = mock(Context.class);

        Throwable thrown = catchThrowable(() -> {
            contract.unknownTransaction(ctx);
        });

        assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                .hasMessage("Undefined contract method called");
    }

    @Nested
    class InvokeReadGradeTransaction {

        @Test
        public void whenGradeExists() {
            GradeController contract = new GradeController();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("Filip Piwowarczyk0"))
                    .thenReturn("{ \"gradeId\": \"Filip Piwowarczyk0\", \"grade\": 2.0,\"visitors\":[\"Adam Mickiewicz\"], \"subject\": \"Math\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Filip Piwowarczyk\"}");

            Grade grade = contract.ReadGrade(ctx, "Filip Piwowarczyk", "Professor", "Filip Piwowarczyk0");

            assertThat(grade).isEqualTo(new Grade("Filip Piwowarczyk0", 2.0, List.of("Adam Mickiewicz"), "Math", "Adam Mickiewicz", "Filip Piwowarczyk"));
        }

        @Test
        public void whenGradeDoesNotExist() {
            GradeController contract = new GradeController();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("Filip Piwowarczyk0")).thenReturn("");

            Throwable thrown = catchThrowable(() -> {
                contract.ReadGrade(ctx, "Filip Piwowarczyk", "Student", "Filip Piwowarczyk0");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Grade Filip Piwowarczyk0 does not exist");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("GRADE_NOT_FOUND".getBytes());
        }

        @Test
        void whenWrongRoleAssigned() {
            GradeController contract = new GradeController();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("Filip Piwowarczyk0"))
                    .thenReturn("{ \"gradeId\": \"Filip Piwowarczyk0\", \"grade\": 2.0, \"subject\": \"Math\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Filip Piwowarczyk\"}");

            Throwable thrown = catchThrowable(() -> {
                contract.ReadGrade(ctx, "John Doe", "Student", "Filip Piwowarczyk0");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Insufficient privileges of John Doe");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("INSUFFICIENT_PERMISSIONS".getBytes());
        }

    }

    @Test
    void invokeInitGradesTransaction() {
        GradeController contract = new GradeController();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);
        when(ctx.getStub()).thenReturn(stub);

        contract.initGrades(ctx);

        InOrder inOrder = inOrder(stub);
        inOrder.verify(stub).putStringState("Filip Piwowarczyk0", "{\"grade\":2.0,\"gradeId\":\"Filip Piwowarczyk0\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"Math\",\"teacher\":\"Adam Mickiewicz\",\"visitors\":[\"admin\"]}");
        inOrder.verify(stub).putStringState("Filip Piwowarczyk1", "{\"grade\":5.0,\"gradeId\":\"Filip Piwowarczyk1\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"WF\",\"teacher\":\"Adam Mickiewicz\",\"visitors\":[\"admin\"]}");
        inOrder.verify(stub).putStringState("Filip Piwowarczyk2", "{\"grade\":4.0,\"gradeId\":\"Filip Piwowarczyk2\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"IT\",\"teacher\":\"Adam Mickiewicz\",\"visitors\":[\"admin\"]}");
        inOrder.verify(stub).putStringState("Filip Piwowarczyk3", "{\"grade\":3.5,\"gradeId\":\"Filip Piwowarczyk3\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"Math\",\"teacher\":\"Adam Mickiewicz\",\"visitors\":[\"admin\"]}");
    }

    @Test
    void shouldGenerateId() {
        GradeController controller = new GradeController();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);
        when(ctx.getStub()).thenReturn(stub);
        when(stub.getStringState("Filip Piwowarczyk0"))
                .thenReturn("smth");
        when(stub.getStringState("Filip Piwowarczyk1"))
                .thenReturn("sth");
        when(stub.getStringState("Filip Piwowarczyk2"))
                .thenReturn("cos");

        Grade grade = controller.addGrade(ctx, "John Doe", "Professor", 3.0, "Math", "John Doe", "Filip Piwowarczyk");

        assertThat(grade).isEqualTo(new Grade("Filip Piwowarczyk3", 3.0, List.of("John Doe"), "Math", "John Doe", "Filip Piwowarczyk"));

    }

    @Test
    void shouldDeserializeRoles() {
        String serializedRoles = "Student;Professor;Teacher";
        GradeController gradeController = new GradeController();

        Set<String> output = gradeController.deserializeRoles(serializedRoles);
        assertThat(output).isEqualTo(Set.of("Student", "Professor", "Teacher"));
    }

    @Test
    void shouldDeserializeOneRole() {
        String serializedRole = "Student";
        GradeController gradeController = new GradeController();

        Set<String> output = gradeController.deserializeRoles(serializedRole);
        assertThat(output).isEqualTo(Set.of("Student"));
    }

    @Nested
    class InvokeCreateGradeTransaction {

        @Test
        public void whenGradeExists() {
            GradeController contract = new GradeController();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("Filip Piwowarczyk0"))
                    .thenReturn("{ \"gradeId\": \"Filip Piwowarczyk0\", \"grade\": 2.0, \"subject\": \"Math\", \"teacher\": \"Adam Mickiewicz\", \"stuednt\": \"Filip Piwowarczyk\"}");

            Throwable thrown = catchThrowable(() -> {
                contract.addGradeWithId(ctx, "Adam Mickiewicz", "Professor", "Filip Piwowarczyk0", 2.0, "Math", "Adam Mickiewicz", "Filip Piwowarczyk");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Grade Filip Piwowarczyk0 already exist");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("GRADE_ALREADY_EXISTS".getBytes());
        }

        @Test
        public void whenWrongRole() {
            GradeController contract = new GradeController();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("Filip Piwowarczyk0"))
                    .thenReturn("");

            Throwable thrown = catchThrowable(() -> {
                contract.addGradeWithId(ctx, "Filip Piwowarczyk", "Student", "Filip Piwowarczyk0", 2.0, "Math", "Adam Mickiewicz", "Filip Piwowarczyk");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Insufficient privileges of Filip Piwowarczyk");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("INSUFFICIENT_PERMISSIONS".getBytes());
        }

        @Test
        public void whenAssignWrongValue() {
            GradeController contract = new GradeController();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("Filip Piwowarczyk0"))
                    .thenReturn("");

            Throwable thrown = catchThrowable(() -> {
                contract.addGrade(ctx, "Adam Mickiewicz", "Professor", 3.2, "Math", "Adam Mickiewicz", "Filip Piwowarczyk");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Bad grade value 3.2");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("WRONG_GRADE_VALUE".getBytes());
        }

        @Test
        void invokeGetAllGradesTransaction() {
            GradeController contract = new GradeController();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStateByRange("", "")).thenReturn(new MockGradeResultsIteratorDifferentStudents());
            when(stub.getStringState(anyString()))
                    .thenReturn("{\"grade\":4.0,\"visitors\":[\"Adam Mickiewicz\"],\"gradeId\":\"Ola Piwowarczyk0\",\"student\":\"Ola Piwowarczyk\",\"subject\":\"Math\",\"teacher\":\"Adam Mickiewicz\"}");
            String grades = contract.getAllGrades(ctx, "admin", "Admin");

            assertThat(grades).isEqualTo("[{\"grade\":2.0,\"gradeId\":\"Filip Piwowarczyk0\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"Math\",\"teacher\":\"Adam Mickiewicz\",\"visitors\":[\"Adam Mickiewicz\"]},"
                    + "{\"grade\":3.0,\"gradeId\":\"Filip Piwowarczyk1\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"PE\",\"teacher\":\"Adam Mickiewicz\",\"visitors\":[\"Adam Mickiewicz\"]},"
                    + "{\"grade\":5.0,\"gradeId\":\"Filip Piwowarczyk2\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"History\",\"teacher\":\"Adam Mickiewicz\",\"visitors\":[\"Adam Mickiewicz\"]},"
                    + "{\"grade\":4.5,\"gradeId\":\"Filip Piwowarczyk3\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"Math\",\"teacher\":\"Adam Mickiewicz\",\"visitors\":[\"Adam Mickiewicz\"]},"
                    + "{\"grade\":4.0,\"gradeId\":\"Filip Piwowarczyk4\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"Math\",\"teacher\":\"Adam Mickiewicz\",\"visitors\":[\"Adam Mickiewicz\"]},"
                    + "{\"grade\":4.0,\"gradeId\":\"Ola Piwowarczyk0\",\"student\":\"Ola Piwowarczyk\",\"subject\":\"Math\",\"teacher\":\"Adam Mickiewicz\",\"visitors\":[\"Adam Mickiewicz\"]}]");
        }

        @Test
        void whenInvokeGetAllGradesTransactionWithBadRole() {
            GradeController contract = new GradeController();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStateByRange("", "")).thenReturn(new MockGradeResultsIteratorDifferentStudents());


            Throwable thrown = catchThrowable(() -> {
                contract.getAllGrades(ctx, "John Doe", "Student");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Insufficient privileges of John Doe");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("INSUFFICIENT_PERMISSIONS".getBytes());
        }


        @Test
        void invokeGetGradesForStudent() {
            GradeController contract = new GradeController();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStateByRange("Filip Piwowarczyk0", "Filip Piwowarczyk999999")).thenReturn(new MockGradeResultsIteratorOneStudent());
            when(stub.getStringState(anyString()))
                    .thenReturn("{\"grade\":2.0,\"visitors\":[\"Adam Mickiewicz\"],\"gradeId\":\"Filip Piwowarczyk0\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"Math\",\"teacher\":\"Adam Mickiewicz\"}");

            String grades = contract.getGradesForStudent(ctx, "Filip Piwowarczyk", "Student", "Filip Piwowarczyk");

            assertThat(grades).isEqualTo("[{\"grade\":2.0,\"gradeId\":\"Filip Piwowarczyk0\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"Math\",\"teacher\":\"Adam Mickiewicz\",\"visitors\":[\"Adam Mickiewicz\"]},"
                    + "{\"grade\":3.0,\"gradeId\":\"Filip Piwowarczyk1\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"PE\",\"teacher\":\"Adam Mickiewicz\",\"visitors\":[\"Adam Mickiewicz\"]},"
                    + "{\"grade\":5.0,\"gradeId\":\"Filip Piwowarczyk2\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"History\",\"teacher\":\"Adam Mickiewicz\",\"visitors\":[\"Adam Mickiewicz\"]},"
                    + "{\"grade\":4.5,\"gradeId\":\"Filip Piwowarczyk3\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"Math\",\"teacher\":\"Adam Mickiewicz\",\"visitors\":[\"Adam Mickiewicz\"]},"
                    + "{\"grade\":4.0,\"gradeId\":\"Filip Piwowarczyk4\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"Math\",\"teacher\":\"Adam Mickiewicz\",\"visitors\":[\"Adam Mickiewicz\"]}]");
        }

        @Test
        void whenInvokeGradesForStudentWithDifferentName() {
            GradeController contract = new GradeController();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStateByRange("Filip Piwowarczyk0", "Filip Piwowarczyk999999")).thenReturn(new MockGradeResultsIteratorOneStudent());
            when(stub.getStringState(anyString()))
                    .thenReturn("{\"grade\":4.0,\"gradeId\":\"Filip Piwowarczyk4\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"Math\",\"teacher\":\"Adam Mickiewicz\",\"visitors\":[\"Adam Mickiewicz\"]}]");

            Throwable thrown = catchThrowable(() -> {
                contract.getGradesForStudent(ctx, "John Doe", "Student", "Filip Piwowarczyk");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Insufficient privileges of John Doe");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("INSUFFICIENT_PERMISSIONS".getBytes());
        }
    }

    @Nested
    class UpdateGradeTransaction {

        @Test
        public void whenGradeExists() {
            GradeController contract = new GradeController();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("Filip Piwowarczyk0"))
                    .thenReturn("{ \"gradeId\": \"Filip Piwowarczyk0\", \"grade\": 2.0,  \"visitors\":[\"Adam Mickiewicz\"], \"subject\": \"Math\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Filip Piwowarczyk\"}");

            Grade grade = contract.UpdateGrade(ctx, "Adam Mickiewicz", "Professor", "Filip Piwowarczyk0", 4.0, "Math", "Adam Mickiewicz", "Filip Piwowarczyk");

            assertThat(grade).isEqualTo(new Grade("Filip Piwowarczyk0", 4.0, List.of("Adam Mickiewicz", "Adam Mickiewicz"), "Math", "Adam Mickiewicz", "Filip Piwowarczyk"));
        }

        @Test
        public void whenGradeDoesNotExists() {
            GradeController contract = new GradeController();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("Filip Piwowarczyk0")).thenReturn("");

            Throwable thrown = catchThrowable(() -> {
                contract.UpdateGrade(ctx, "Adam Mickiewicz", "Professor", "Filip Piwowarczyk0", 3.0, "Math", "Adam Dabrowski", "Filip Piwowarczyk");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Grade Filip Piwowarczyk0 does not exist");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("GRADE_NOT_FOUND".getBytes());
        }

        @Test
        void whenAssignWrongGradeValue() {
            GradeController contract = new GradeController();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("Filip Piwowarczyk0"))
                    .thenReturn("{ \"gradeId\": \"Filip Piwowarczyk0\", \"grade\": 2.0, \"subject\": \"Math\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Filip Piwowarczyk\"}");

            Throwable thrown = catchThrowable(() -> {
                contract.UpdateGrade(ctx, "Adam Mickiewicz", "Professor", "Filip Piwowarczyk0", 3.2, "Math", "Adam Mickiewicz", "Filip Piwowarczyk");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Bad grade value 3.2");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("WRONG_GRADE_VALUE".getBytes());
        }

        @Test
        void whenUpdatingWithBadRole() {
            GradeController contract = new GradeController();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("John Doe0"))
                    .thenReturn("{ \"gradeId\": \"John Doe0\", \"grade\": 2.0, \"subject\": \"Math\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"John Doe\"}");

            Throwable thrown = catchThrowable(() -> {
                contract.UpdateGrade(ctx, "John Doe", "Student", "John Doe0", 3.0, "Math", "Adam Mickiewicz", "John Doe");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Insufficient privileges of John Doe");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("INSUFFICIENT_PERMISSIONS".getBytes());
        }

    }

    @Nested
    class DeleteGradeTransaction {

        @Test
        public void whenGradeDoesNotExist() {
            GradeController contract = new GradeController();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("Filip Piwowarczyk0")).thenReturn("");

            Throwable thrown = catchThrowable(() -> {
                contract.DeleteGrade(ctx, "Filip Piwowarczyk", "Professor", "Filip Piwowarczyk0");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Grade Filip Piwowarczyk0 does not exist");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("GRADE_NOT_FOUND".getBytes());
        }

    }

}
