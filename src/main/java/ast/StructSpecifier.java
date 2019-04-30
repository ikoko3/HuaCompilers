/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast;

import ast.visitor.ASTVisitor;
import ast.visitor.ASTVisitorException;
import org.objectweb.asm.Type;

/**
 *
 * @author john_
 */
public class StructSpecifier extends TypeSpecifier{

    private String StuctId;

    public String getStuctId() {
        return StuctId;
    }

    public void setStuctId(String StuctId) {
        this.StuctId = StuctId;
    }

    public StructSpecifier(String StuctId) {
        this.StuctId = StuctId;
    }

    public StructSpecifier(String StuctId, Type type) {
        super(type);
        this.StuctId = StuctId;
    }
    
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
