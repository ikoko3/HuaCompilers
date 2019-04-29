/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.definition;

import ast.ASTNode;
import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.TypeSpecifier;

/**
 *
 * @author john_
 */
public class Variable extends ASTNode{

    private TypeSpecifier Type;
    private String Name;

    public TypeSpecifier getType() {
        return Type;
    }

    public void setType(TypeSpecifier Type) {
        this.Type = Type;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }
    
    public Variable() {
    }

    public Variable(TypeSpecifier Type, String Name) {
        this.Type = Type;
        this.Name = Name;
    }


    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
