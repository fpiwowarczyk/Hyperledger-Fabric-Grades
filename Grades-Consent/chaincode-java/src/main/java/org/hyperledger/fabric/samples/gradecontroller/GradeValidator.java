package org.hyperledger.fabric.samples.gradecontroller;

import org.apache.commons.collections.CollectionUtils;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeException;

import java.util.Set;

import static org.hyperledger.fabric.samples.gradecontroller.GradeController.gradeExists;

public class GradeValidator {

    public static void checkIfGradeDoesNotExists(final Context ctx, final String gradeId) {
        if (!gradeExists(ctx, gradeId)) {
            String errorMessage = String.format("Grade %s does not exist", gradeId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, GradeController.GradeControllerErrors.GRADE_NOT_FOUND.toString());
        }
    }

    public static void checkIfGradeExists(final Context ctx, final String gradeId) {
        if (gradeExists(ctx, gradeId)) {
            String errorMessage = String.format("Grade %s already exist", gradeId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, GradeController.GradeControllerErrors.GRADE_ALREADY_EXISTS.toString());
        }
    }

    public static void checkIfGradeValueIsCorrect(final Double gradeValue) {
        if (!checkGradeValue(gradeValue)) {
            String errorMessage = String.format("Bad grade value %s", gradeValue);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, GradeController.GradeControllerErrors.WRONG_GRADE_VALUE.toString());
        }
    }

    public static void checkRolesForDeletion(final Set<String> roles, final String author) {
        if (!CollectionUtils.containsAny(roles, Set.of("Admin", "Professor"))) {
            String errorMessage = String.format("Insufficient privileges of %s", author);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, GradeController.GradeControllerErrors.INSUFFICIENT_PERMISSIONS.toString());
        }
    }

    public static void checkRolesForUpdate(final Set<String> roles, final String author) {
        if (!CollectionUtils.containsAny(roles, Set.of("Admin", "Professor"))) {
            String errorMessage = String.format("Insufficient privileges of %s", author);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, GradeController.GradeControllerErrors.INSUFFICIENT_PERMISSIONS.toString());
        }
    }

    public static void checkRolesForReading(final Set<String> roles, final String author) {
        if (!CollectionUtils.containsAny(Set.of(roles), Set.of("Admin", "Professor"))) {
            String errorMessage = String.format("Insufficient privileges of %s", author);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, GradeController.GradeControllerErrors.INSUFFICIENT_PERMISSIONS.toString());
        }
    }

    private static boolean checkGradeValue(final Double value) {
        return value.equals(2.0)
                || value.equals(2.5)
                || value.equals(3.0)
                || value.equals(3.5)
                || value.equals(4.0)
                || value.equals(4.5)
                || value.equals(5.0);
    }
}
