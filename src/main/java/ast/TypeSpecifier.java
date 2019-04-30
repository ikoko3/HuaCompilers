/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast;

import ast.visitor.ASTVisitor;
import ast.visitor.ASTVisitorException;
import org.objectweb.asm.Type;

public class TypeSpecifier extends ASTNode{

    private Type type;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public TypeSpecifier() {
    }

    public TypeSpecifier(Type type) {
        this.type = type;
    }
    
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
