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
public class StructArrayAccessExpression extends StructAccessExpression{

    private Expression Index;

    public Expression getIndex() {
        return Index;
    }

    public void setIndex(Expression Index) {
        this.Index = Index;
    }

    public StructArrayAccessExpression() {
    }

    public StructArrayAccessExpression(Expression Index) {
        this.Index = Index;
    }

    public StructArrayAccessExpression(Expression Index, Expression Struct, String Identifier) {
        super(Struct, Identifier);
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
