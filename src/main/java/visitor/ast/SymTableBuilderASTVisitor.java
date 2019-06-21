package visitor.ast;

/**
 * This code is part of the lab exercises for the Compilers course at Harokopio

 * University of Athens, Dept. of Informatics and Telematics.
 */

import ast.expression.*;
import ast.statement.*;
import ast.definition.*;
import java.util.ArrayDeque;
import java.util.Deque;

import ast.*;
import symbol.HashSymTable;
import symbol.SymTable;
import symbol.SymTableEntry;

/**
 * Build symbol tables for each node of the AST.
 */
public class SymTableBuilderASTVisitor implements ASTVisitor {

    private final Deque<SymTable<SymTableEntry>> stack;

    public SymTableBuilderASTVisitor() {
        stack = new ArrayDeque<SymTable<SymTableEntry>>();
    }

    @Override
    public void visit(CompUnit node) throws ASTVisitorException {
        pushEnvironment();
        ASTUtils.setSymbolTable(node, stack.element());
        for (Definition d : node.getDefinitions()) {
            d.accept(this);
        }
        
        popEnvironment();
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());
        node.getTarget().accept(this);
        node.getResult().accept(this);
    }


    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());
        node.getExpression().accept(this);
    }


    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());
    }


    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());
    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());
        node.getExpression().accept(this);
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());
        node.getExpression().accept(this);
        node.getStatement().accept(this);
    }
    
    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        pushEnvironment();
        ASTUtils.setSymbolTable(node, stack.element());
        for (Statement s : node.getStatements()) {
            s.accept(this);
        }
        popEnvironment();
    }

    
    @Override
    public void visit(Array node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());
        node.getVariable().accept(this);
    }

    @Override
    public void visit(Variable node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        pushEnvironment();
        ASTUtils.setSymbolTable(node, stack.element());
        for(ParameterDeclaration p: node.getParameters()){
            p.accept(this);
        }
        for(Statement s: node.getStatements()){
            s.accept(this);
        }
        popEnvironment();
    }

    @Override
    public void visit(StructDefinition node) throws ASTVisitorException {
        SymTable<SymTableEntry> symTable = new HashSymTable<SymTableEntry>();
        stack.push(symTable);
        ASTUtils.setSymbolTable(node, stack.element());
        
        ASTUtils.setSymbolTable(node, stack.element());
        for(VariableDefinition v: node.getVariables()){
            v.accept(this);
       }
        popEnvironment();
    }

    @Override
    public void visit(ArrayAccessExpression node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());
        node.getIndex().accept(this);
    }

    @Override
    public void visit(BooleanLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());

    }

    @Override
    public void visit(CharLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());

    }

    @Override
    public void visit(FloatLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());

    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());
        for(Expression e: node.getExpressions()){
            e.accept(this);
        }
    }


    @Override
    public void visit(StructArrayAccessExpression node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());
        node.getStruct().accept(this);
        node.getIndex().accept(this);
    }

    @Override
    public void visit(StructVariableAccessExpression node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());
        node.getStruct().accept(this);
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());

    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());

    }

    @Override
    public void visit(EmptyStatement node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());
        node.getExpression().accept(this); 
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());
        node.getExpression().accept(this);
        node.getStatement().accept(this);
        node.getElseStatement().accept(this);
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());
        node.getExpression().accept(this);
        node.getStatement().accept(this);
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());
        if(node.getExpression()!=null)
            node.getExpression().accept(this);
    }

    @Override
    public void visit(VariableDefinition node) throws ASTVisitorException {
        ASTUtils.setSymbolTable(node, stack.element());
        node.getVariable().accept(this);
    }
    

    private void pushEnvironment() {
        SymTable<SymTableEntry> oldSymTable = stack.peek();
        SymTable<SymTableEntry> symTable = new HashSymTable<SymTableEntry>(
                oldSymTable);
        stack.push(symTable);
    }

    private void popEnvironment() {
        stack.pop();
    }

    
}
