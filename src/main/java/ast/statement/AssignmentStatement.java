/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package ast.statement;

import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.expression.Expression;

public class AssignmentStatement extends Statement {

    private Expression Expression1;
    private Expression Expression2;

    public Expression getExpression1() {
        return Expression1;
    }

    public void setExpression1(Expression Expression1) {
        this.Expression1 = Expression1;
    }

    public Expression getExpression2() {
        return Expression2;
    }

    public void setExpression2(Expression Expression2) {
        this.Expression2 = Expression2;
    }

    public AssignmentStatement() {
    }

    public AssignmentStatement(Expression Expression1, Expression Expression2) {
        this.Expression1 = Expression1;
        this.Expression2 = Expression2;
    }
    
    
    
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
