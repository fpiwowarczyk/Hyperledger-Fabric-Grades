package org.hyperledger.fabric.samples.gradecontroller;

enum GradeValue {
    TWO(2.0),
    THREE(3.0),
    THREEPLUS(3.5),
    FOUR(4.0),
    FOURPLUS(4.5),
    FIVE(5.0);

    public final Double value;

    GradeValue(final Double value) {
        this.value = value;
    }
}
