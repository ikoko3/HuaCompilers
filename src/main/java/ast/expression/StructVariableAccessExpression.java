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
public class StructVariableAccessExpression extends StructAccessExpression{

    public StructVariableAccessExpression(Expression Struct, String Identifier) {
        super(Struct, Identifier);
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
