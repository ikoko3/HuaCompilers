/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.statement;

import ast.visitor.ASTVisitor;
import ast.visitor.ASTVisitorException;
import ast.expression.Expression;

/**
 *
 * @author john_
 */
public class IfStatement extends Statement {

    private Expression expression;
    private Statement statement;

    public IfStatement(Expression expression, Statement statement) {
        this.expression = expression;
        this.statement = statement;
    }

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}