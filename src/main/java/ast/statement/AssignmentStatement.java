/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package ast.statement;

import visitor.ast.ASTVisitorException;
import ast.expression.Expression;
import visitor.ast.ASTVisitor;
import visitor.statement.StatementVisitor;

public class AssignmentStatement extends Statement {

    private Expression target;
    private Expression result;

    public Expression getTarget() {
        return target;
    }

    public void setTarget(Expression target) {
        this.target = target;
    }

    public Expression getResult() {
        return result;
    }

    public void setResult(Expression result) {
        this.result = result;
    }

    public AssignmentStatement() {
    }

    public AssignmentStatement(Expression Expression1, Expression Expression2) {
        this.target = Expression1;
        this.result = Expression2;
    }
    
    
    
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
    public void accept(StatementVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
