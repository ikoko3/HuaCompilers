/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package ast;

import ast.expression.StructArrayAccessExpression;
import ast.expression.FunctionCallExpression;
import ast.expression.UnaryExpression;
import ast.expression.FloatLiteralExpression;
import ast.expression.StringLiteralExpression;
import ast.expression.ParenthesisExpression;
import ast.expression.StructVariableAccessExpression;
import ast.expression.BinaryExpression;
import ast.expression.IdentifierExpression;
import ast.expression.ArrayAccessExpression;
import ast.expression.BooleanLiteralExpression;
import ast.expression.CharLiteralExpression;
import ast.expression.IntegerLiteralExpression;
import ast.statement.ReturnStatement;
import ast.statement.ContinueStatement;
import ast.statement.WhileStatement;
import ast.statement.IfStatement;
import ast.statement.EmptyStatement;
import ast.statement.BreakStatement;
import ast.statement.IfElseStatement;
import ast.statement.CompoundStatement;
import ast.statement.AssignmentStatement;
import ast.definition.FunctionDefinition;
import ast.definition.VariableDefinition;
import ast.definition.StructDefinition;
import ast.definition.Array;
import ast.definition.Variable;


/**
 * Abstract syntax tree visitor.
 */
public interface ASTVisitor {

    void visit(CompUnit node) throws ASTVisitorException;
    
    void visit(Array node) throws ASTVisitorException;
    
    void visit(ParameterDeclaration node) throws ASTVisitorException;
    
    void visit(Variable node) throws ASTVisitorException;
    
    //Definitions
    void visit(FunctionDefinition node) throws ASTVisitorException;
    
    void visit(StructDefinition node) throws ASTVisitorException;

    //Expressions
    
    void visit(ArrayAccessExpression node) throws ASTVisitorException;
    
    void visit(BinaryExpression node) throws ASTVisitorException;

    void visit(BooleanLiteralExpression node) throws ASTVisitorException;
    
    void visit(CharLiteralExpression node) throws ASTVisitorException;

    void visit(FloatLiteralExpression node) throws ASTVisitorException;
    
    void visit(FunctionCallExpression node) throws ASTVisitorException;
    
    void visit(IdentifierExpression node) throws ASTVisitorException;
    
    void visit(IntegerLiteralExpression node) throws ASTVisitorException;

    void visit(ParenthesisExpression node) throws ASTVisitorException;
    
    void visit(StringLiteralExpression node) throws ASTVisitorException;
    
    void visit(StructArrayAccessExpression node) throws ASTVisitorException;
    
    void visit(StructVariableAccessExpression node) throws ASTVisitorException;
    
    void visit(UnaryExpression node) throws ASTVisitorException;
    
    //Statement
    
     void visit(AssignmentStatement node) throws ASTVisitorException;
     
     void visit(BreakStatement node) throws ASTVisitorException;

     void visit(CompoundStatement node) throws ASTVisitorException;
     
     void visit(ContinueStatement node) throws ASTVisitorException;
     
     void visit(EmptyStatement node) throws ASTVisitorException;
     
     void visit(IfElseStatement node) throws ASTVisitorException;
     
     void visit(IfStatement node) throws ASTVisitorException;
     
     void visit(ReturnStatement node) throws ASTVisitorException;
     
     void visit(WhileStatement node) throws ASTVisitorException;

     void visit(TypeSpecifier node) throws ASTVisitorException;
     
     void visit(StructSpecifier node) throws ASTVisitorException;

     void visit(VariableDefinition node) throws ASTVisitorException;

}
