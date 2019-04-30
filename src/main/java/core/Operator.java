/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package core;

public enum Operator {

    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVISION("/"),
    MOD("%"),
    AND("&&"),
    OR("||"),
    NOT("!"),
    EQUAL("=="),
    NOT_EQUAL("!="),
    LESS("<"),
    LESS_EQ("<="),
    GREATER(">"),
    GREATER_EQ(">=");

    private String type;

    private Operator(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }

    public boolean isUnary() {
        return this.equals(Operator.MINUS) || this.equals(Operator.NOT);
    }

    public boolean isRelational() {
        return this.equals(Operator.EQUAL) || this.equals(Operator.NOT_EQUAL)
                || this.equals(Operator.GREATER) || this.equals(Operator.GREATER_EQ)
                || this.equals(Operator.LESS) || this.equals(Operator.LESS_EQ);
    }

    public boolean isArithmetic(){
        return this.equals(Operator.PLUS) || this.equals(Operator.MINUS)
                || this.equals(Operator.MULTIPLY) || this.equals(Operator.DIVISION)
                || this.equals(Operator.MOD);
    }

    public boolean isLogical(){
        return this.equals(Operator.AND) || this.equals(Operator.OR);
    }
}
