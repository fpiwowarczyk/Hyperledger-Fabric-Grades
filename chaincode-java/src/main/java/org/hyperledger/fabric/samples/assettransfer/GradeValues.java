package org.hyperledger.fabric.samples.assettransfer;

enum GradeValue {
    TWO(2.0),
    TWOPLUS(2.5),
    THREE(3.0),
    THREEPLUS(3.5),
    FOUR(4.0),
    FOURPLUS(4.5),
    FIVE(5.0);

    public final Double value;

    private GradeValue(Double value) {
        this.value = value;
    }
}
