/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visitor.statement;

import ast.definition.FunctionDefinition;
import ast.definition.StructDefinition;
import ast.definition.VariableDefinition;
import ast.statement.*;
import visitor.ast.*;


/**
 *
 * @author john_
 */
public interface StatementVisitor {
    void visit(AssignmentStatement node) throws ASTVisitorException;
     
     void visit(BreakStatement node) throws ASTVisitorException;

     void visit(CompoundStatement node) throws ASTVisitorException;
     
     void visit(ContinueStatement node) throws ASTVisitorException;
     
     void visit(EmptyStatement node) throws ASTVisitorException;
     
     void visit(IfElseStatement node) throws ASTVisitorException;
     
     void visit(IfStatement node) throws ASTVisitorException;
     
     void visit(ReturnStatement node) throws ASTVisitorException;
     
     void visit(WhileStatement node) throws ASTVisitorException;

     void visit(VariableDefinition node) throws ASTVisitorException;
     
     void visit(FunctionDefinition node) throws ASTVisitorException;
    
     void visit(StructDefinition node) throws ASTVisitorException;
}
