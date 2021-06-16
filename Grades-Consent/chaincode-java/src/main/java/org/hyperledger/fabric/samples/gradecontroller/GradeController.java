package org.hyperledger.fabric.samples.gradecontroller;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.collections.CollectionUtils;
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
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;

import static org.hyperledger.fabric.samples.gradecontroller.GradeValidator.checkRolesForDeletion;
import static org.hyperledger.fabric.samples.gradecontroller.GradeValidator.checkIfGradeExists;
import static org.hyperledger.fabric.samples.gradecontroller.GradeValidator.checkIfGradeValueIsCorrect;
import static org.hyperledger.fabric.samples.gradecontroller.GradeValidator.checkRolesForReading;
import static org.hyperledger.fabric.samples.gradecontroller.GradeValidator.checkRolesForUpdate;


@Contract(
        name = "grades",
        info = @Info(
                title = "Grades controller",
                description = "The hyperledger grades system",
                version = "0.0.2",
                contact = @Contact(
                        email = "filip.piwowarczyk1997@gmail.com",
                        name = "Filip Piwowarczyk"
                )))
@Default
public class GradeController implements ContractInterface {

    private final Genson genson = new Genson();

    public enum GradeControllerErrors {
        GRADE_ALREADY_EXISTS,
        GRADE_NOT_FOUND,
        WRONG_GRADE_VALUE,
        INSUFFICIENT_PERMISSIONS
    }

    /**
     * @param ctx Context of app
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void initGrades(final Context ctx) {

        addGradeWithId(ctx, "admin", "Admin", "Filip Piwowarczyk0", GradeValue.TWO.value, "Math", "Adam Mickiewicz", "Filip Piwowarczyk");
        addGradeWithId(ctx, "admin", "Admin", "Filip Piwowarczyk1", GradeValue.FIVE.value, "WF", "Adam Mickiewicz", "Filip Piwowarczyk");
        addGradeWithId(ctx, "admin", "Admin", "Filip Piwowarczyk2", GradeValue.FOUR.value, "IT", "Adam Mickiewicz", "Filip Piwowarczyk");
        addGradeWithId(ctx, "admin", "Admin", "Filip Piwowarczyk3", GradeValue.THREEPLUS.value, "Math", "Adam Mickiewicz", "Filip Piwowarczyk");

    }

    /**
     * @param ctx             App context
     * @param author          Author of query
     * @param serializedRoles Roles of that author
     * @param gradeValue      Value of grade
     * @param subject         Subject taught by teacher
     * @param teacher         Teacher
     * @param student         Student that have new grade
     * @return Full grade value
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Grade addGrade(final Context ctx,
                          final String author,
                          final String serializedRoles,
                          final Double gradeValue,
                          final String subject,
                          final String teacher,
                          final String student) {
        ChaincodeStub stub = ctx.getStub();
        Set<String> roles = deserializeRoles(serializedRoles);
        checkIfGradeValueIsCorrect(gradeValue);

        if (!CollectionUtils.containsAny(roles, Set.of("Admin", "Professor"))) {
            String errorMessage = String.format("Insufficient privileges of %s", author);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, GradeControllerErrors.INSUFFICIENT_PERMISSIONS.toString());
        }
        String gradeId = getGradeId(ctx, student);
        Grade grade = new Grade(gradeId, gradeValue, List.of(author), subject, teacher, student);
        String gradeJSON = genson.serialize(grade);
        stub.putStringState(gradeId, gradeJSON);

        return grade;
    }

    /**
     * @param ctx             Context of app
     * @param author          Author of query
     * @param serializedRoles Serialized roles of author
     * @param gradeId         Id of grade Name of student + index
     * @param gradeValue      Value of grade
     * @param subject         Subject taught by teacher
     * @param teacher         Teacher
     * @param student         Student
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void addGradeWithId(final Context ctx,
                               final String author,
                               final String serializedRoles,
                               final String gradeId,
                               final Double gradeValue,
                               final String subject,
                               final String teacher,
                               final String student) {
        ChaincodeStub stub = ctx.getStub();
        Set<String> roles = deserializeRoles(serializedRoles);
        checkIfGradeValueIsCorrect(gradeValue);

        if (gradeExists(ctx, gradeId)) {
            String errorMessage = String.format("Grade with id %s already exists", gradeId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, GradeControllerErrors.GRADE_ALREADY_EXISTS.toString());
        }
        if (!CollectionUtils.containsAny(roles, Set.of("Admin", "Professor"))) {
            String errorMessage = String.format("Insufficient privileges of %s", author);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, GradeControllerErrors.INSUFFICIENT_PERMISSIONS.toString());
        }
        Grade grade = new Grade(gradeId, gradeValue, List.of(author), subject, teacher, student);
        String gradeJSON = genson.serialize(grade);
        stub.putStringState(gradeId, gradeJSON);
    }

    /**
     * @param ctx             Context of app
     * @param author          Author of query
     * @param serializedRoles Serialized roles of author
     * @param gradeId         Id of grade
     * @return Grade with given gradeId
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Grade ReadGrade(final Context ctx,
                           final String author,
                           final String serializedRoles,
                           final String gradeId) {
        ChaincodeStub stub = ctx.getStub();
        Set<String> roles = deserializeRoles(serializedRoles);
        checkIfGradeExists(ctx, gradeId);
        String gradeJSON = stub.getStringState(gradeId);
        Grade grade = genson.deserialize(gradeJSON, Grade.class);
        UpdateGrade(ctx, author, serializedRoles, gradeId, grade.getGrade(), grade.getSubject(), grade.getTeacher(), grade.getStudent());

        if (grade.getStudent().equals(author) || CollectionUtils.containsAny(roles, Set.of("Admin", "Professor"))) {
            return grade;
        } else {
            String errorMessage = String.format("Insufficient privileges of %s", author);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, GradeControllerErrors.INSUFFICIENT_PERMISSIONS.toString());
        }
    }

    /**
     * @param ctx             Context of app
     * @param author          Author of query
     * @param serializedRoles Serialized roles of author
     * @param studentName     Student's name
     * @return All grades for student
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getGradesForStudent(final Context ctx,
                                      final String author,
                                      final String serializedRoles,
                                      final String studentName) {
        ChaincodeStub stub = ctx.getStub();
        Set<String> roles = deserializeRoles(serializedRoles);
        List<Grade> queryResults = new ArrayList<>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange(studentName + "0", studentName + "999999");

        for (KeyValue result : results) {
            Grade grade = genson.deserialize(result.getStringValue(), Grade.class);
            UpdateGrade(ctx, author, serializedRoles, grade.getGradeId(), grade.getGrade(), grade.getSubject(), grade.getTeacher(), grade.getStudent());
            if (grade.getStudent().equals(author) || CollectionUtils.containsAny(roles, Set.of("Admin", "Professor"))) {
                queryResults.add(grade);
            } else {
                String errorMessage = String.format("Insufficient privileges of %s", author);
                System.out.println(errorMessage);
                throw new ChaincodeException(errorMessage, GradeControllerErrors.INSUFFICIENT_PERMISSIONS.toString());
            }
        }
        return genson.serialize(queryResults);
    }

    /**
     * @param ctx             Context of app
     * @param author          Author of query
     * @param serializedRoles Serialized roles of author
     * @return All grades in system
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getAllGrades(final Context ctx,
                               final String author,
                               final String serializedRoles) {
        ChaincodeStub stub = ctx.getStub();
        Set<String> roles = deserializeRoles(serializedRoles);
        List<Grade> queryResults = new ArrayList<>();
        checkRolesForReading(roles, author);
        //To get all grades we are using getStateByRange with empty strings
        // as arguments. It is interpreted as get all keys from beginning to end.
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");
        for (KeyValue result : results) {
            Grade grade = genson.deserialize(result.getStringValue(), Grade.class);
            UpdateGrade(ctx, author, serializedRoles, grade.getGradeId(), grade.getGrade(), grade.getSubject(), grade.getTeacher(), grade.getStudent());
            queryResults.add(grade);
        }
        return genson.serialize(queryResults);
    }

    /**
     * @param ctx             Context of app
     * @param author          Author of update
     * @param serializedRoles Serialized roles of author
     * @param gradeId         Id of grade to update
     * @param gradeValue      Value of grade
     * @param subject         Subject of grade
     * @param teacher         Teacher that taught tht subject
     * @param student         Students name
     * @return Updated grade
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Grade UpdateGrade(final Context ctx,
                             final String author,
                             final String serializedRoles,
                             final String gradeId,
                             final Double gradeValue,
                             final String subject,
                             final String teacher,
                             final String student) {
        ChaincodeStub stub = ctx.getStub();
        Set<String> roles = deserializeRoles(serializedRoles);
        checkIfGradeExists(ctx, gradeId);
        checkIfGradeValueIsCorrect(gradeValue);
        List<String> visitors = setNewVisitors(gradeId, author, stub);
        Grade newGrade = new Grade(gradeId, gradeValue, visitors, subject, teacher, student);
        String newGradeJSON = genson.serialize(newGrade);
        stub.putStringState(gradeId, newGradeJSON);

        return newGrade;
    }

    /**
     * @param ctx             Context of app
     * @param author          author of query
     * @param serializedRoles serialized roles of query
     * @param gradeId         id of grade to delete
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void DeleteGrade(final Context ctx,
                            final String author,
                            final String serializedRoles,
                            final String gradeId) {
        ChaincodeStub stub = ctx.getStub();
        Set<String> roles = deserializeRoles(serializedRoles);
        checkIfGradeExists(ctx, gradeId);
        String gradeJSON = stub.getStringState(gradeId);
        Grade grade = genson.deserialize(gradeJSON, Grade.class);
        UpdateGrade(ctx, author, serializedRoles, gradeId, grade.getGrade(), grade.getSubject(), grade.getTeacher(), grade.getStudent());
        checkRolesForDeletion(roles, author);
        stub.delState(gradeId);
    }

    /**
     * @param ctx     Context of app
     * @param gradeId Id of grade
     * @return True if grade exists
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public static boolean gradeExists(final Context ctx,
                                      final String gradeId) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(gradeId);
        return (assetJSON != null && !assetJSON.isEmpty());
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


    /**
     * @param gradeId Id of grade
     * @param author  Author of query
     * @param stub    Stub to make call
     * @return List of visitor with author of query
     */
    private List<String> setNewVisitors(final String gradeId, final String author, final ChaincodeStub stub) {
        String gradeJSON = stub.getStringState(gradeId);
        Grade oldGrade = genson.deserialize(gradeJSON, Grade.class);
        List<String> visitors = oldGrade.getVisitors();
        visitors.add(author);
        return visitors;
    }

    /**
     * @param serializedRoles Serialized roles
     * @return Deserialized roles
     */
    @VisibleForTesting
    Set<String> deserializeRoles(final String serializedRoles) {
        return new HashSet<String>(Arrays.asList(serializedRoles.split(";")));
    }
}
