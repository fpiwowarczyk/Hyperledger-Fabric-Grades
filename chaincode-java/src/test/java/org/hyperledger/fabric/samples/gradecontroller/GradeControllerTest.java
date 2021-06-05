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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;

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

    private final class MockGradeResultsIterator implements QueryResultsIterator<KeyValue> {

        private final List<KeyValue> gradesList;

        MockGradeResultsIterator() {
            super();

            gradesList = new ArrayList<KeyValue>();

            gradesList.add(new MockKeyValue("grade1",
                    "{ \"gradeId\": \"grade1\", \"grade\": 2.0, \"subject\": \"Math\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Filip Piwowarczyk\"}"));
            gradesList.add(new MockKeyValue("grade1",
                    "{ \"gradeId\": \"grade2\", \"grade\": 3.0, \"subject\": \"PE\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Filip Piwowarczyk\"}"));
            gradesList.add(new MockKeyValue("grade1",
                    "{ \"gradeId\": \"grade3\", \"grade\": 5.0, \"subject\": \"History\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Filip Piwowarczyk\"}"));
            gradesList.add(new MockKeyValue("grade1",
                    "{ \"gradeId\": \"grade4\", \"grade\": 4.5, \"subject\": \"Math\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Filip Piwowarczyk\"}"));
            gradesList.add(new MockKeyValue("grade1",
                    "{ \"gradeId\": \"grade5\", \"grade\": 4.0, \"subject\": \"Math\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Filip Piwowarczyk\"}"));
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
            when(stub.getStringState("grade1"))
                    .thenReturn("{ \"gradeId\": \"grade1\", \"grade\": 2.0, \"subject\": \"Math\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Filip Piwowarczyk\"}");

            Grade grade = contract.ReadGrade(ctx, "grade1");

            assertThat(grade).isEqualTo(new Grade("grade1", 2.0, "Math", "Adam Mickiewicz", "Filip Piwowarczyk"));
        }

        @Test
        public void whenGradeDoesNotExist() {
            GradeController contract = new GradeController();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("grade1")).thenReturn("");

            Throwable thrown = catchThrowable(() -> {
                contract.ReadGrade(ctx, "grade1");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Grade grade1 does not exist");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("GRADE_NOT_FOUND".getBytes());
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
        inOrder.verify(stub).putStringState("grade1", "{\"grade\":2.0,\"gradeId\":\"grade1\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"Math\",\"teacher\":\"Adam Mickiewicz\"}");
        inOrder.verify(stub).putStringState("grade2", "{\"grade\":5.0,\"gradeId\":\"grade2\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"WF\",\"teacher\":\"Adam Mickiewicz\"}");
        inOrder.verify(stub).putStringState("grade3", "{\"grade\":4.0,\"gradeId\":\"grade3\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"IT\",\"teacher\":\"Adam Mickiewicz\"}");
        inOrder.verify(stub).putStringState("grade4", "{\"grade\":3.5,\"gradeId\":\"grade4\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"Math\",\"teacher\":\"Adam Mickiewicz\"}");
    }

    @Nested
    class InvokeCreateGradeTransaction {

        @Test
        public void whenGradeExists() {
            GradeController contract = new GradeController();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("grade1"))
                    .thenReturn("{ \"gradeId\": \"grade1\", \"grade\": 2.0, \"subject\": \"Math\", \"teacher\": \"Adam Mickiewicz\", \"stuednt\": \"Filip Piwowarczyk\"}");

            Throwable thrown = catchThrowable(() -> {
                contract.addGrade(ctx, "grade1", 2.0, "Math", "Adam Mickiewicz", "Filip Piwowarczyk");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Grade grade1 already exists");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("GRADE_ALREADY_EXISTS".getBytes());
        }

        @Test
        public void whenAssignWrongValue() {
            GradeController contract = new GradeController();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("grade1"))
                    .thenReturn("");

            Throwable thrown = catchThrowable(() -> {
                contract.addGrade(ctx, "grade1", 3.2, "Math", "Adam Mickiewicz", "Filip Piwowarczyk");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Bad grade value 3.2");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("WRONG_GRADE_VALUE".getBytes());
        }

        @Test
        void invokeGetAllAssetsTransaction() {
            GradeController contract = new GradeController();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStateByRange("", "")).thenReturn(new MockGradeResultsIterator());

            String grades = contract.getAllGrades(ctx);

            assertThat(grades).isEqualTo("[{\"grade\":2.0,\"gradeId\":\"grade1\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"Math\",\"teacher\":\"Adam Mickiewicz\"},"
                    + "{\"grade\":3.0,\"gradeId\":\"grade2\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"PE\",\"teacher\":\"Adam Mickiewicz\"},"
                    + "{\"grade\":5.0,\"gradeId\":\"grade3\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"History\",\"teacher\":\"Adam Mickiewicz\"},"
                    + "{\"grade\":4.5,\"gradeId\":\"grade4\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"Math\",\"teacher\":\"Adam Mickiewicz\"},"
                    + "{\"grade\":4.0,\"gradeId\":\"grade5\",\"student\":\"Filip Piwowarczyk\",\"subject\":\"Math\",\"teacher\":\"Adam Mickiewicz\"}]");
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
            when(stub.getStringState("grade1"))
                    .thenReturn("{ \"gradeId\": \"grade1\", \"grade\": 2.0, \"subject\": \"Math\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Filip Piwowarczyk\"}");

            Grade grade = contract.UpdateGrade(ctx, "grade1", 4.0, "Math", "Adam Mickiewicz", "Filip Piwowarczyk");

            assertThat(grade).isEqualTo(new Grade("grade1", 4.0, "Math", "Adam Mickiewicz", "Filip Piwowarczyk"));
        }

        @Test
        public void whenGradeDoesNotExists() {
            GradeController contract = new GradeController();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("grade1")).thenReturn("");

            Throwable thrown = catchThrowable(() -> {
                contract.UpdateGrade(ctx, "grade1", 3.0, "Math", "Adam Dabrowski", "Filip Piwowarczyk");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Grade grade1 does not exist");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("GRADE_NOT_FOUND".getBytes());
        }

        @Test
        void whenAssignWrongGradeValue() {
            GradeController contract = new GradeController();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("grade1"))
                    .thenReturn("{ \"gradeId\": \"grade1\", \"grade\": 2.0, \"subject\": \"Math\", \"teacher\": \"Adam Mickiewicz\", \"student\": \"Filip Piwowarczyk\"}");

            Throwable thrown = catchThrowable(() -> {
                contract.UpdateGrade(ctx, "grade1", 3.2, "Math", "Adam Dabrowski", "Filip Piwowarczyk");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Bad grade value 3.2");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("WRONG_GRADE_VALUE".getBytes());
        }

    }

}
