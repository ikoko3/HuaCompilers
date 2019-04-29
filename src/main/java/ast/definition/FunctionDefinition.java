/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.definition;

import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.ParameterDeclaration;
import ast.statement.Statement;
import ast.TypeSpecifier;
import java.lang.reflect.Parameter;
import java.util.List;

/**
 *
 * @author john_
 */
public class FunctionDefinition extends Definition{

    private TypeSpecifier ReturnType;
    private String Name;
    private List<ParameterDeclaration> Parameters;
    private List<Statement> Statements;

    public TypeSpecifier getReturnType() {
        return ReturnType;
    }

    public void setReturnType(TypeSpecifier ReturnType) {
        this.ReturnType = ReturnType;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public List<ParameterDeclaration> getParameters() {
        return Parameters;
    }

    public void setParameters(List<ParameterDeclaration> Parameters) {
        this.Parameters = Parameters;
    }

    public List<Statement> getStatements() {
        return Statements;
    }

    public void setStatements(List<Statement> Statements) {
        this.Statements = Statements;
    }

    public FunctionDefinition(TypeSpecifier ReturnType, String Name, List<ParameterDeclaration> Parameters, List<Statement> Statements) {
        this.ReturnType = ReturnType;
        this.Name = Name;
        this.Parameters = Parameters;
        this.Statements = Statements;
    }

    public FunctionDefinition() {
    } 
    
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
