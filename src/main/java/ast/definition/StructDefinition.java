/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.definition;

import visitor.ast.ASTVisitor;
import visitor.ast.ASTVisitorException;
import java.util.List;
import visitor.statement.StatementVisitor;

/**
 *
 * @author john_
 */
public class StructDefinition extends Definition{

    private String Name;
    private List<VariableDefinition> Variables;

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public List<VariableDefinition> getVariables() {
        return Variables;
    }

    public void setVariables(List<VariableDefinition> Variables) {
        this.Variables = Variables;
    }

    public StructDefinition() {
    }

    public StructDefinition(String Name, List<VariableDefinition> Variables) {
        this.Name = Name;
        this.Variables = Variables;
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
