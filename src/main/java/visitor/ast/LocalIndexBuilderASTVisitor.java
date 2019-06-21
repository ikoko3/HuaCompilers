package visitor.ast;

/**
 * This code is part of the lab exercises for the Compilers course at Harokopio

 * University of Athens, Dept. of Informatics and Telematics.
 */

import ast.definition.ParameterDeclaration;
import ast.expression.*;
import ast.statement.*;
import ast.definition.*;
import java.util.ArrayDeque;
import java.util.Deque;

import ast.*;
import symbol.*;

/**
 * Build symbol tables for each node of the AST.
 */
public class LocalIndexBuilderASTVisitor implements ASTVisitor {

    private final Deque<LocalIndexPool> env;

    public LocalIndexBuilderASTVisitor() {
        env = new ArrayDeque<LocalIndexPool>();
    }

    @Override
    public void visit(CompUnit node) throws ASTVisitorException {
        env.push(new LocalIndexPool());
        ASTUtils.setLocalIndexPool(node, env.element());
        for (Definition d : node.getDefinitions()) {
            d.accept(this);
        }
        env.pop();
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getTarget().accept(this);
        node.getResult().accept(this);
    }


    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression().accept(this);
    }


    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
    }


    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression().accept(this);
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression().accept(this);
        node.getStatement().accept(this);
    }
    
    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        for (Statement s : node.getStatements()) {
            s.accept(this);
        }
    }

    

    @Override
    public void visit(Array node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getVariable().accept(this);
    }

    @Override
    public void visit(Variable node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        env.push(new LocalIndexPool());
        ASTUtils.setLocalIndexPool(node, env.element());
        for(ParameterDeclaration p: node.getParameters()){
            p.accept(this);
        }
        for(Statement s: node.getStatements()){
            s.accept(this);
        }
        env.pop();
    }

    @Override
    public void visit(StructDefinition node) throws ASTVisitorException {
        env.push(new LocalIndexPool());
        ASTUtils.setLocalIndexPool(node, env.element());

        for(VariableDefinition v: node.getVariables()){
            v.accept(this);
       }
       env.pop();
    }

    @Override
    public void visit(ArrayAccessExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getIndex().accept(this);
    }

    @Override
    public void visit(BooleanLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());

    }

    @Override
    public void visit(CharLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());

    }

    @Override
    public void visit(FloatLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        for(Expression e: node.getExpressions()){
            e.accept(this);
        }
    }


    @Override
    public void visit(StructArrayAccessExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getStruct().accept(this);
        node.getIndex().accept(this);
    }

    @Override
    public void visit(StructVariableAccessExpression node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getStruct().accept(this);
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());

    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());

    }

    @Override
    public void visit(EmptyStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression().accept(this); 
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression().accept(this);
        node.getStatement().accept(this);
        node.getElseStatement().accept(this);
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getExpression().accept(this);
        node.getStatement().accept(this);
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        if(node.getExpression()!=null)
            node.getExpression().accept(this);
    }

    @Override
    public void visit(VariableDefinition node) throws ASTVisitorException {
        ASTUtils.setLocalIndexPool(node, env.element());
        node.getVariable().accept(this);
    }


    
}
