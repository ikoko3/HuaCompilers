/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package ast.expression;

import visitor.ast.ASTVisitor;
import visitor.ast.ASTVisitorException;
import core.Operator;
import visitor.expression.ExpressionVisitor;

public class BinaryExpression extends Expression {

    private Operator operator;
    private Expression expression1;
    private Expression expression2;

    public BinaryExpression(Operator operator, Expression expression1, Expression expression2) {
        this.operator = operator;
        this.expression1 = expression1;
        this.expression2 = expression2;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Expression getExpression1() {
        return expression1;
    }

    public void setExpression1(Expression expression1) {
        this.expression1 = expression1;
    }

    public Expression getExpression2() {
        return expression2;
    }

    public void setExpression2(Expression expression2) {
        this.expression2 = expression2;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    @Override
    public void accept(ExpressionVisitor visitor) throws ASTVisitorException{
        visitor.visit(this);
    }

}
