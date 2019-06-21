/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.expression;

import visitor.ast.ASTVisitor;
import visitor.ast.ASTVisitorException;
import ast.expression.Expression;
import visitor.expression.ExpressionVisitor;

/**
 *
 * @author john_
 */
public class CharLiteralExpression extends Expression{

    private char Expression;

    public char getExpression() {
        return Expression;
    }

    public void setExpression(char Expression) {
        this.Expression = Expression;
    }

    public CharLiteralExpression() {
    }

    public CharLiteralExpression(char Expression) {
        this.Expression = Expression;
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
