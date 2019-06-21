/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.definition;

import visitor.ast.ASTVisitor;
import visitor.ast.ASTVisitorException;
import visitor.statement.StatementVisitor;

/**
 *
 * @author john_
 */
public class VariableDefinition extends Definition{

    private Variable variable;

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }

    public VariableDefinition() {
    }

    public VariableDefinition(Variable variable) {
        this.variable = variable;
    }


    
    
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
    @Override
    public void accept(StatementVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
}
