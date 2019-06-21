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
public class IfElseStatement extends IfStatement{

    private Statement ElseStatement;

    public Statement getElseStatement() {
        return ElseStatement;
    }

    public void setElseStatement(Statement ElseStatement) {
        this.ElseStatement = ElseStatement;
    }
    
    
    public IfElseStatement(Expression expression, Statement statement) {
        super(expression, statement);
    }

    public IfElseStatement(Statement ElseStatement, Expression expression, Statement statement) {
        super(expression, statement);
        this.ElseStatement = ElseStatement;
    }

    
    
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
        public void accept(StatementVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
}
