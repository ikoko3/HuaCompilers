package ast.visitor;

/**
 * This code is part of the lab exercises for the Compilers course at Harokopio

 * University of Athens, Dept. of Informatics and Telematics.
 */

import core.Registry;
import ast.expression.*;
import ast.statement.*;
import ast.definition.*;


import ast.*;
import org.objectweb.asm.Type;
import symbol.SymTable;
import symbol.SymTableEntry;
import types.TypeUtils;

/**
 * Build symbol tables for each node of the AST.
 */
public class CollectSymbolsASTVisitor implements ASTVisitor {

    public CollectSymbolsASTVisitor() {
        
    }

    @Override
    public void visit(CompUnit node) throws ASTVisitorException {
        for (Definition d : node.getDefinitions()) {
            d.accept(this);
        }
        setProperties(node);
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);
        setProperties(node);
    }


    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);
        setProperties(node);
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
        setProperties(node);
    }


    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        setProperties(node);
    }


    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        setProperties(node);
    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        setProperties(node);
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {

        node.getExpression().accept(this);
        setProperties(node);
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        setWhileLoopState(true);
        node.getStatement().accept(this);
        setWhileLoopState(true);
        setProperties(node);
    }
    
    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        for (Statement s : node.getStatements()) {
            s.accept(this);
        }
        setProperties(node);
    }

    

    @Override
    public void visit(Array node) throws ASTVisitorException {
        node.getType().accept(this);
        setProperties(node);
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        node.getVariable().accept(this);
        setProperties(node);
    }

    @Override
    public void visit(Variable node) throws ASTVisitorException {
        node.getType().accept(this);
        setProperties(node);
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        
        Type[] types = TypeUtils.getParameterTypesFor(node.getParameters());
        Type functionType = Type.getMethodType(node.getReturnType().getType(),types);
        ASTNode root = Registry.getInstance().getRoot();
        
        SymTable<SymTableEntry> st = ASTUtils.getSafeSymbolTable(root);
        if(st.lookup(node.getName()) != null)
            ASTUtils.error(node, "Dublicate function declaration: "+node.getName());
        
        st.put(node.getName(), new SymTableEntry(node.getName(),functionType));
        
        setFunctionState(node.getName());
        for(Statement s: node.getStatements()){
            s.accept(this);
        }   
        stopFunctionState();
        setProperties(node);
    }

    @Override
    public void visit(StructDefinition node) throws ASTVisitorException {
        for(VariableDefinition v: node.getVariables()){
            v.accept(this);
       }
        if(Registry.getInstance().getStructs().get(node.getName()) != null)
            ASTUtils.error(node, "Dublicate Struct declaration: "+node.getName());
        
       Registry.getInstance().getStructs().put(node.getName(), ASTUtils.getSafeSymbolTable(node));
       setProperties(node);
    }

    @Override
    public void visit(ArrayAccessExpression node) throws ASTVisitorException {
        node.getIndex().accept(this);
        setProperties(node);
    }

    @Override
    public void visit(BooleanLiteralExpression node) throws ASTVisitorException {
        setProperties(node);
    }

    @Override
    public void visit(CharLiteralExpression node) throws ASTVisitorException {
        setProperties(node);
    }

    @Override
    public void visit(FloatLiteralExpression node) throws ASTVisitorException {
        setProperties(node);
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {
        for(Expression e: node.getExpressions()){
            e.accept(this);
        }
        setProperties(node);
    }


    @Override
    public void visit(StructArrayAccessExpression node) throws ASTVisitorException {
        node.getStruct().accept(this);
        node.getIndex().accept(this);
        setProperties(node);
    }

    @Override
    public void visit(StructVariableAccessExpression node) throws ASTVisitorException {
        node.getStruct().accept(this);
        setProperties(node);
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        setProperties(node);
    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        setProperties(node);
    }

    @Override
    public void visit(EmptyStatement node) throws ASTVisitorException {
        node.getExpression().accept(this); 
        setProperties(node);
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        node.getStatement().accept(this);
        node.getElseStatement().accept(this);
        setProperties(node);
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        node.getStatement().accept(this);
        setProperties(node);
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        if(node.getExpression()!=null)
            node.getExpression().accept(this);
        setProperties(node);
    }

    @Override
    public void visit(TypeSpecifier node) throws ASTVisitorException {
         setProperties(node);
    }
    
     @Override
    public void visit(StructSpecifier node) throws ASTVisitorException {
         setProperties(node);
    }

    @Override
    public void visit(VariableDefinition node) throws ASTVisitorException {
        String varName = node.getVariable().getName();
        Type varType = node.getVariable().getType().getType();
        
        SymTable<SymTableEntry> st = ASTUtils.getSafeSymbolTable(node);
        if(st.lookupOnlyInTop(varName) != null)
            ASTUtils.error(node, "Dublicate variable declaration: "+varName);
        
        st.put(varName, new SymTableEntry(varName,varType));
        
        node.getVariable().accept(this);
    }

    private void setFunctionState(String functionName){
        Registry.getInstance().setFunctionState(functionName);
    }

    private void stopFunctionState(){
        Registry.getInstance().stopFunctionState();
    }
    
    private void setWhileLoopState(boolean state){
        Registry.getInstance().setInWhileLoop(state);
    }
    
    
    private void setProperties(ASTNode node){
        ASTUtils.setWhileLoopState(node, Registry.getInstance().isInWhileLoop());
        
        boolean isInFunction = Registry.getInstance().isInFunctionDefinition();
        String functionName = Registry.getInstance().getCurrentFunctionName();
        
        ASTUtils.setFunctionProperties(node, isInFunction, functionName);

        
    }
}
