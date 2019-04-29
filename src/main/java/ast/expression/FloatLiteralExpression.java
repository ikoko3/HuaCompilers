/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package ast.expression;

import ast.ASTVisitor;
import ast.ASTVisitorException;

public class FloatLiteralExpression extends Expression {

    private Float literal;

    public FloatLiteralExpression(Float literal) {
        this.literal = literal;
    }

    public Float getLiteral() {
        return literal;
    }

    public void setLiteral(Float literal) {
        this.literal = literal;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
