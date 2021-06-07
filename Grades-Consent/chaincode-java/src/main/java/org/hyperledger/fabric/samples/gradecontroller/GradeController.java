package org.hyperledger.fabric.samples.gradecontroller;

import org.hyperledger.fabric.contract.Context;
import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.util.ArrayList;
import java.util.List;


@Contract(
        name = "grades",
        info = @Info(
                title = "Grades controller",
                description = "The hyperledger grades system",
                version = "0.0.1",
                contact = @Contact(
                        email = "filip.piwowarczyk1997@gmail.com",
                        name = "Filip Piwowarczyk"
                )))
@Default
public class GradeController implements ContractInterface {

    private final Genson genson = new Genson();

    private enum GradeControllerErrors {
        GRADE_ALREADY_EXISTS,
        GRADE_NOT_FOUND,
        WRONG_GRADE_VALUE
    }

    /**
     * Create some initial grades for students
     *
     * @param ctx the transaction context
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void initGrades(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();


        addGradeWithId(ctx, "Filip Piwowarczyk0", GradeValue.TWO.value, "Math", "Adam Mickiewicz", "Filip Piwowarczyk");
        addGradeWithId(ctx, "Filip Piwowarczyk1", GradeValue.FIVE.value, "WF", "Adam Mickiewicz", "Filip Piwowarczyk");
        addGradeWithId(ctx, "Filip Piwowarczyk2", GradeValue.FOUR.value, "IT", "Adam Mickiewicz", "Filip Piwowarczyk");
        addGradeWithId(ctx, "Filip Piwowarczyk3", GradeValue.THREEPLUS.value, "Math", "Adam Mickiewicz", "Filip Piwowarczyk");

    }


    /**
     * @param ctx
     * @param gradeValue
     * @param subject
     * @param teacher
     * @param student
     * @return
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Grade addGrade(final Context ctx, final Double gradeValue,
                          final String subject, final String teacher, final String student) {
        ChaincodeStub stub = ctx.getStub();

        if (!checkGradeValue(gradeValue)) {
            String errorMessage = String.format("Bad grade value %s", gradeValue);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, GradeControllerErrors.WRONG_GRADE_VALUE.toString());
        }

        String gradeId = getGradeId(ctx, student);


        Grade grade = new Grade(gradeId, gradeValue, subject, teacher, student);
        String gradeJSON = genson.serialize(grade);
        stub.putStringState(gradeId, gradeJSON);

        return grade;
    }

    /**
     * @param ctx
     * @param gradeId
     * @param subject
     * @param teacher
     * @param student
     * @return
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Grade addGradeWithId(final Context ctx, final String gradeId, final Double gradeValue, final String subject, final String teacher, final String student) {
        ChaincodeStub stub = ctx.getStub();

        if (!checkGradeValue(gradeValue)) {
            String errorMessage = String.format("Bad grade value %s", gradeValue);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, GradeControllerErrors.WRONG_GRADE_VALUE.toString());
        }

        if (gradeExists(ctx, gradeId)) {
            String errorMessage = String.format("Grade with id %s already exists", gradeId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, GradeControllerErrors.GRADE_ALREADY_EXISTS.toString());
        }

        Grade grade = new Grade(gradeId, gradeValue, subject, teacher, student);
        String gradeJSON = genson.serialize(grade);
        stub.putStringState(gradeId, gradeJSON);

        return grade;
    }

    /**
     * @param ctx
     * @param gradeId
     * @return
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Grade ReadGrade(final Context ctx, final String gradeId) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(gradeId);

        if (assetJSON == null || assetJSON.isEmpty()) {
            String errorMessage = String.format("Grade %s does not exist", gradeId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, GradeControllerErrors.GRADE_NOT_FOUND.toString());
        }


        Grade grade = genson.deserialize(assetJSON, Grade.class);
        return grade;
    }

    /**
     * @param ctx
     * @param studentName
     * @return
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getGradesForStudent(final Context ctx, final String studentName) {
        ChaincodeStub stub = ctx.getStub();

        List<Grade> queryResults = new ArrayList<Grade>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange(studentName + "0", studentName + "999999");

        for (KeyValue result : results) {
            Grade grade = genson.deserialize(result.getStringValue(), Grade.class);
            queryResults.add(grade);
            System.out.println(grade.toString());
        }

        final String response = genson.serialize(queryResults);

        return response;
    }

    /**
     * @param ctx
     * @param gradeId
     * @param gradeValue
     * @param subject
     * @param teacher
     * @param student
     * @return
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Grade UpdateGrade(final Context ctx, final String gradeId, final Double gradeValue, final String subject, final String teacher, final String student) {
        ChaincodeStub stub = ctx.getStub();

        if (!gradeExists(ctx, gradeId)) {
            String errorMessage = String.format("Grade %s does not exist", gradeId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, GradeControllerErrors.GRADE_NOT_FOUND.toString());
        }

        if (!checkGradeValue(gradeValue)) {
            String errorMessage = String.format("Bad grade value %s", gradeValue);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, GradeControllerErrors.WRONG_GRADE_VALUE.toString());
        }

        Grade newGrade = new Grade(gradeId, gradeValue, subject, teacher, student);
        String newAssetJSON = genson.serialize(newGrade);
        stub.putStringState(gradeId, newAssetJSON);

        return newGrade;
    }

    /**
     * @param ctx
     * @param gradeId
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void DeleteGrade(final Context ctx, final String gradeId) {
        ChaincodeStub stub = ctx.getStub();

        if (!gradeExists(ctx, gradeId)) {
            String errorMessage = String.format("Grade %s does not exist", gradeId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, GradeControllerErrors.GRADE_NOT_FOUND.toString());
        }

        stub.delState(gradeId);
    }

    /**
     * @param ctx
     * @param gradeId
     * @return
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public boolean gradeExists(final Context ctx, final String gradeId) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(gradeId);

        return (assetJSON != null && !assetJSON.isEmpty());
    }

    /**
     * @param ctx
     * @return
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getAllGrades(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Grade> queryResults = new ArrayList<Grade>();

        //To get all grades we are using getStateByRande with empty strings
        // as arguments. It is interpreted as get all keys from beginning to end.
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result : results) {
            Grade grade = genson.deserialize(result.getStringValue(), Grade.class);
            queryResults.add(grade);
            System.out.println(grade.toString());
        }

        final String response = genson.serialize(queryResults);
        return response;
    }

    /**
     *
     * @param ctx
     * @return
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getQueryAuthor(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        String author = new String(stub.getCreator());

        return author;

    }

    private String getGradeId(final Context ctx, final String student) {
        int i = 0;
        String output = student + i;
        while (gradeExists(ctx, output)) {
            i++;
            output = student + i;
        }
        return output;
    }

    private boolean checkGradeValue(final Double value) {
        return value.equals(2.0)
                || value.equals(2.5)
                || value.equals(3.0)
                || value.equals(3.5)
                || value.equals(4.0)
                || value.equals(4.5)
                || value.equals(5.0);
    }
}
