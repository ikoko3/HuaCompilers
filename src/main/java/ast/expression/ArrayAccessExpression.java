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
public class ArrayAccessExpression extends Expression{

    private String Identifier;
    private Expression Index;

    public String getIdentifier() {
        return Identifier;
    }

    public void setIdentifier(String Identifier) {
        this.Identifier = Identifier;
    }

    public Expression getIndex() {
        return Index;
    }

    public void setIndex(Expression Index) {
        this.Index = Index;
    }

    public ArrayAccessExpression() {
    }

    public ArrayAccessExpression(String Identifier, Expression Index) {
        this.Identifier = Identifier;
        this.Index = Index;
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
