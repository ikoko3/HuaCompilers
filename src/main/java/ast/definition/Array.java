/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.definition;

import ast.visitor.ASTVisitor;
import ast.visitor.ASTVisitorException;
import ast.TypeSpecifier;

/**
 *
 * @author john_
 */
public class Array extends Variable{
    private int Length;

    public Array() {
    }

    public Array(TypeSpecifier Type, String identifier) {
        super(Type, identifier);
    }

    public Array(int Length, TypeSpecifier Type, String identifier) {
        super(Type, identifier);
        this.Length = Length;
    }

    public int getLength() {
        return Length;
    }

    public void setLength(int Length) {
        this.Length = Length;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
