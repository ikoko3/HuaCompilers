/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.expression;

import visitor.ast.ASTVisitor;
import visitor.ast.ASTVisitorException;
import visitor.expression.ExpressionVisitor;

/**
 *
 * @author john_
 */
public class BooleanLiteralExpression extends Expression{

    private boolean Expression; 

    public boolean isExpression() {
        return Expression;
    }

    public void setExpression(boolean Expression) {
        this.Expression = Expression;
    }

    public BooleanLiteralExpression() {
    }

    public BooleanLiteralExpression(boolean Expression) {
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
