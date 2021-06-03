package org.hyperledger.fabric.samples.assettransfer;

import org.hyperledger.fabric.contract.Context;
import com.owlike.genson.Genson;
import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.Locale;

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

        addGrade(ctx, "grade1", GradeValue.TWOPLUS.value, "Math", "Adam Mickiewicz", "Filip Piwowarczyk");
        addGrade(ctx, "grade2", GradeValue.FIVE.value, "WF", "Adam Mickiewicz", "Filip Piwowarczyk");
        addGrade(ctx, "grade3", GradeValue.FOUR.value, "IT", "Adam Mickiewicz", "Filip Piwowarczyk");
        addGrade(ctx, "grade4", GradeValue.THREE.value, "Math", "Adam Mickiewicz", "Filip Piwowarczyk");

    }


    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Grade addGrade(final Context ctx, final String gradeId, final Double gradeValue,
                          final String subject, final String teacher, final String student) {
        ChaincodeStub stub = ctx.getStub();

        if (gradeExists(ctx, gradeId)) {
            String errorMessage = String.format("Grade %s already exists", gradeId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, GradeControllerErrors.GRADE_ALREADY_EXISTS.toString());
        }

        if (checkGradeValue(gradeValue)) {
            String errorMessage = String.format("Bad grade value %s", gradeValue);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, GradeControllerErrors.WRONG_GRADE_VALUE.toString());
        }

        Grade grade = new Grade(gradeId, gradeValue, subject, teacher, student);
        String assetJSON = genson.serialize(grade);
        stub.putStringState(gradeId, assetJSON);

        return grade;
    }

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

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Grade UpdateGrade(final Context ctx, final String gradeId, final Double gradeValue, final String subject, final String teacher, final String student) {
        ChaincodeStub stub = ctx.getStub();

        if (!gradeExists(ctx, gradeId)) {
            String errorMessage = String.format("Grade %s does not exist", gradeId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, GradeControllerErrors.GRADE_NOT_FOUND.toString());
        }

        if (checkGradeValue(gradeValue)) {
            String errorMessage = String.format("Bad grade value %s", gradeValue);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, GradeControllerErrors.WRONG_GRADE_VALUE.toString());
        }

        Grade newGrade = new Grade(gradeId, gradeValue, subject, teacher, student);
        String newAssetJSON = genson.serialize(newGrade);
        stub.putStringState(gradeId, newAssetJSON);

        return newGrade;
    }

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


    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public boolean gradeExists(final Context ctx, final String gradeId) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(gradeId);

        return (assetJSON != null && !assetJSON.isEmpty());
    }

    private void checkIfGradeExist() {

    }

    private boolean checkGradeValue(Double value) {
        return value.equals(2.0) ||
                value.equals(2.5) ||
                value.equals(3.0) ||
                value.equals(3.5) ||
                value.equals(4.0) ||
                value.equals(4.5) ||
                value.equals(5.0);
    }
}
