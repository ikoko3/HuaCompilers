/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.expression;

import ast.ASTVisitor;
import ast.ASTVisitorException;

/**
 *
 * @author john_
 */
public abstract class StructAccessExpression extends Expression{

    private Expression Struct;
    private String Identifier;

    public Expression getStruct() {
        return Struct;
    }

    public void setStruct(Expression Struct) {
        this.Struct = Struct;
    }

    public String getIdentifier() {
        return Identifier;
    }

    public void setIdentifier(String Identifier) {
        this.Identifier = Identifier;
    }

    public StructAccessExpression() {
    }

    public StructAccessExpression(Expression Struct, String Identifier) {
        this.Struct = Struct;
        this.Identifier = Identifier;
    }
    
    
            
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
