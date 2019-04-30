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
import types.exception.*;
import types.TypeUtils;

/**
 * Build symbol tables for each node of the AST.
 */
public class CollectTypesASTVisitor implements ASTVisitor {

    public CollectTypesASTVisitor() {
        
    }

    @Override
    public void visit(CompUnit node) throws ASTVisitorException {
        for (Definition d : node.getDefinitions()) {
            d.accept(this);
        }
        
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);
       
        Type type1 = ASTUtils.getSafeType(node.getExpression1());
        Type type2 = ASTUtils.getSafeType(node.getExpression2());
        
        if(!TypeUtils.isAssignable(type1, type2))
            ASTUtils.error(node,"Cannot assign "+type2.getClassName()+" to "+type1.getClassName());
    }


    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        Type type1 = ASTUtils.getType(node.getExpression1());
        node.getExpression2().accept(this);
        Type type2 = ASTUtils.getType(node.getExpression2());
        try{
            Type type = TypeUtils.applyBinary(node.getOperator(), type1, type2);
            ASTUtils.setType(node, type);
            
        }catch(NotNumbersException e){
            ASTUtils.error(node, "The expressions are not Numbers.");
        }catch(TypeException e){
            ASTUtils.error(node, "Type Exception");
        }
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }


    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        SymTable<SymTableEntry> st = ASTUtils.getSafeSymbolTable(node);
        
        SymTableEntry entry = st.lookup(node.getIdentifier());
        if(entry == null)
            ASTUtils.error(node, "variable "+node.getIdentifier()+" has not been declared: ");
        
        ASTUtils.setType(node, entry.getType());
    }


    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.INT_TYPE);
    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.getType(String.class));
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {

        node.getExpression().accept(this);
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        node.getStatement().accept(this);
    }
    
    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        for (Statement s : node.getStatements()) {
            s.accept(this);
        }
    }

    

    @Override
    public void visit(Array node) throws ASTVisitorException {
        node.getType().accept(this);
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        node.getVariable().accept(this);
    }

    @Override
    public void visit(Variable node) throws ASTVisitorException {
        node.getType().accept(this);
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        
        Type[] types = TypeUtils.getParameterTypesFor(node.getParameters());
        Type functionType = Type.getMethodType(node.getReturnType().getType(),types);
        ASTNode root = Registry.getInstance().getRoot();
        
        SymTable<SymTableEntry> st = ASTUtils.getSafeSymbolTable(root);
        
        
        st.put(node.getName(), new SymTableEntry(node.getName(),functionType));
        
        for(Statement s: node.getStatements()){
            s.accept(this);
        }   
    }

    @Override
    public void visit(StructDefinition node) throws ASTVisitorException {
        for(VariableDefinition v: node.getVariables()){
            v.accept(this);
       }
    }

    @Override
    public void visit(ArrayAccessExpression node) throws ASTVisitorException {
        node.getIndex().accept(this);
    }

    @Override
    public void visit(BooleanLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.BOOLEAN_TYPE);
    }

    @Override
    public void visit(CharLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.CHAR_TYPE);
    }

    @Override
    public void visit(FloatLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.FLOAT_TYPE);
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {
        SymTable<SymTableEntry> st = ASTUtils.getSafeSymbolTable(node);
        
        if(st.lookup(node.getIdentifier())==null)
            ASTUtils.error(node,"The function "+node.getIdentifier()+" has not been declared.");
        
        Type functionType = st.lookup(node.getIdentifier()).getType();
        int expectedArguements = functionType.getArgumentTypes().length;
        int givenArguements = node.getExpressions().size();
        
        if( givenArguements != expectedArguements )
            ASTUtils.error(node,"Expected "+expectedArguements+" arguements found "+givenArguements);
        
        for(int i=0;i<expectedArguements;i++){
            Expression e = node.getExpressions().get(i);
            e.accept(this);
            
            Type givenType = ASTUtils.getSafeType(e);
            Type expectedType = functionType.getArgumentTypes()[i];
            if(!TypeUtils.isAssignable(expectedType, givenType))
               ASTUtils.error(node, "Expected "+expectedType.getClassName()+", received "+givenType.getClassName());
            
        }
        
        ASTUtils.setType(node, functionType.getReturnType());
    }


    @Override
    public void visit(StructArrayAccessExpression node) throws ASTVisitorException {
        node.getStruct().accept(this);
        node.getIndex().accept(this);
    }

    @Override
    public void visit(StructVariableAccessExpression node) throws ASTVisitorException {
        node.getStruct().accept(this);
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        if(!ASTUtils.getWhileLoopState(node)){
            ASTUtils.error(node,"Break statement should be inside a while Loop.");
        }
    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        if(!ASTUtils.getWhileLoopState(node)){
            ASTUtils.error(node,"Continue statement should be inside a while Loop.");
        }
    }

    @Override
    public void visit(EmptyStatement node) throws ASTVisitorException {
        node.getExpression().accept(this); 
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        node.getStatement().accept(this);
        node.getElseStatement().accept(this);
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        node.getStatement().accept(this);
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        if(!ASTUtils.getFunctionState(node))
            ASTUtils.error(node,"Return should only be declared inside functions.");
        
        Type exprType;
        SymTable<SymTableEntry> st = ASTUtils.getSafeSymbolTable(node);
        Type returnType = st.lookup(ASTUtils.getCurrentFunctionName(node)).getType().getReturnType();
        
        if(node.getExpression()!=null){
            node.getExpression().accept(this);
            exprType = ASTUtils.getSafeType(node.getExpression());
        }else{
            exprType = Type.VOID_TYPE;
            if(returnType != Type.VOID_TYPE){
                 ASTUtils.error(node,"The function must return "+returnType.getClassName());
            }
        }
           
        if(!TypeUtils.isAssignable(returnType, exprType))
            ASTUtils.error(node,"The return type should be "+returnType.getClassName()+", and cannot be cast from "+exprType.getClassName());
    }

    @Override
    public void visit(TypeSpecifier node) throws ASTVisitorException {

    }
    
     @Override
    public void visit(StructSpecifier node) throws ASTVisitorException {
        
    }

    @Override
    public void visit(VariableDefinition node) throws ASTVisitorException {
        String varName = node.getVariable().getName();
        Type varType = node.getVariable().getType().getType();
        
        SymTable<SymTableEntry> st = ASTUtils.getSafeSymbolTable(node);
        // if(st.lookupOnlyInTop(varName) != null)
        //     ASTUtils.error(node, "Dublicate variable declaration: "+varName);
        
        st.put(varName, new SymTableEntry(varName,varType));
        
        node.getVariable().accept(this);
    }

   

}
