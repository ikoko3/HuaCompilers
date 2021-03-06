/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.expression;

import visitor.ast.ASTVisitor;
import visitor.ast.ASTVisitorException;

import java.util.ArrayList;
import java.util.List;
import visitor.expression.ExpressionVisitor;

/**
 *
 * @author john_
 */
public class FunctionCallExpression extends Expression{

    private String Identifier;
    private List<Expression> Expressions;

    public String getIdentifier() {
        return Identifier;
    }

    public void setIdentifier(String Identifier) {
        this.Identifier = Identifier;
    }

    public List<Expression> getExpressions() {
        return Expressions;
    }

    public void setExpressions(List<Expression> Expressions) {
        this.Expressions = Expressions;
    }

    public FunctionCallExpression() {
        Expressions = new ArrayList<Expression>();
    }

    public FunctionCallExpression(String Identifier) {
        this();
        this.Identifier = Identifier;
    }

    public FunctionCallExpression(String Identifier, List<Expression> Expressions) {
        this();
        this.Identifier = Identifier;
        this.Expressions = Expressions;
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
