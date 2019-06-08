/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.definition;

import ast.ASTNode;
import ast.visitor.ASTVisitor;
import ast.visitor.ASTVisitorException;
import org.objectweb.asm.Type;

/**
 *
 * @author john_
 */
public class Variable extends ASTNode{

    private Type type;
    private String name;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Variable() {
    }

    public Variable(Type Type, String Name) {
        this.type = Type;
        this.name = Name;
    }


    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
