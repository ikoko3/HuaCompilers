/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.statement;

import visitor.ast.ASTVisitor;
import visitor.ast.ASTVisitorException;
import ast.expression.Expression;
import visitor.statement.StatementVisitor;

/**
 *
 * @author john_
 */
public class ReturnStatement extends Statement{

    private Expression Expression;

    public Expression getExpression() {
        return Expression;
    }

    public void setExpression(Expression Expression) {
        this.Expression = Expression;
    }

    public ReturnStatement() {
    }

    public ReturnStatement(Expression Expression) {
        this.Expression = Expression;
    }
    
    
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
    public void accept(StatementVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
