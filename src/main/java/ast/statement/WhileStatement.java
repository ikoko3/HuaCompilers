/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.statement;

import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.expression.Expression;

/**
 *
 * @author john_
 */
public class WhileStatement extends Statement{

    private Expression Expression;
    private Statement Statement;

    public Expression getExpression() {
        return Expression;
    }

    public void setExpression(Expression Expression) {
        this.Expression = Expression;
    }

    public Statement getStatement() {
        return Statement;
    }

    public void setStatement(Statement Statement) {
        this.Statement = Statement;
    }

    public WhileStatement() {
    }

    public WhileStatement(Expression Expression, Statement Statement) {
        this.Expression = Expression;
        this.Statement = Statement;
    }
    
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
