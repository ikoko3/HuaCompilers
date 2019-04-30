/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast;

import ast.visitor.ASTVisitor;
import ast.visitor.ASTVisitorException;
import ast.definition.Variable;

/**
 *
 * @author john_
 */
public class ParameterDeclaration extends ASTNode{

    private Variable variable;

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }

    public ParameterDeclaration() {
    }

    public ParameterDeclaration(Variable variable) {
        this.variable = variable;
    }

        
    
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
