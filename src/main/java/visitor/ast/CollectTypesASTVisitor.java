package visitor.ast;

/**
 * This code is part of the lab exercises for the Compilers course at Harokopio

 * University of Athens, Dept. of Informatics and Telematics.
 */

import core.Registry;
import ast.expression.*;
import ast.statement.*;
import ast.definition.*;
import ast.*;
import core.Environment;

import java.util.List;
import java.util.Map;
import org.objectweb.asm.Type;
import symbol.SymTable;
import symbol.SymTableEntry;
import types.exception.*;
import types.TypeUtils;
import visitor.statement.HasReturnASTVisitor;

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
        findMainFunction();
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        node.getTarget().accept(this);
        node.getResult().accept(this);
       
        Type type1 = ASTUtils.getSafeType(node.getTarget());
        Type type2 = ASTUtils.getSafeType(node.getResult());

        if(!TypeUtils.isAssignable(type1, type2))
            ASTUtils.error(node,"Cannot assign "+type2.getClassName()+" to "+type1.getClassName());
        ASTUtils.setType(node, Type.VOID_TYPE);
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
        }catch(CanNotApplyLogicalOperatorException e){
            ASTUtils.error(node, "The expressions between logical operators should be boolean.");
        }catch(NotComparableException e){
            ASTUtils.error(node, "The expressions are not comparable.");
        }catch(TypeException e){
            ASTUtils.error(node, "Error in Binary Expression "+node.getOperator());
        }
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
        Type type = ASTUtils.getType(node.getExpression());
        
        try{
            Type nodeType = TypeUtils.applyUnary(node.getOperator(), type);
            ASTUtils.setType(node, nodeType);
        }catch(TypeException e){
            ASTUtils.error(node, "Error in Binary Expression "+node.getOperator());
        }
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
        ASTUtils.setType(node, Type.getType("Ljava/lang/String;"));
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {

        node.getExpression().accept(this);
        ASTUtils.setType(node, ASTUtils.getType(node.getExpression()));
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        if(ASTUtils.getSafeType(node.getExpression())!=Type.BOOLEAN_TYPE)
            ASTUtils.error(node,"The Expression inside the if must be logical");
        node.getStatement().accept(this);
        ASTUtils.setType(node, Type.VOID_TYPE);
    }
    
    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        for (Statement s : node.getStatements()) {
            s.accept(this);
        }
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    

    @Override
    public void visit(Array node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        node.getVariable().accept(this);
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(Variable node) throws ASTVisitorException {
        if(TypeUtils.isStructType(node.getType())){
            SymTable st = Registry.getInstance().getStructs().get(node.getType().getDescriptor());
            if(st == null)
                ASTUtils.error(node, "There is no struct "+TypeUtils.getStructId(node.getType()));
        }
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        
        Type[] types = TypeUtils.getParameterTypesFor(node.getParameters());
        Type functionType = Type.getMethodType(node.getReturnType(),types);
        ASTNode root = Registry.getInstance().getRoot();
        
        SymTable<SymTableEntry> st = ASTUtils.getSafeSymbolTable(root);
        st.put(node.getName(), new SymTableEntry(node.getName(),functionType));
        
        for(Statement s: node.getStatements()){
            s.accept(this);
        }   

        
        if(!node.getReturnType().equals(Type.VOID_TYPE)){
            HasReturnASTVisitor returnVisitor = new HasReturnASTVisitor();
            node.accept(returnVisitor);
            if(!returnVisitor.containsReturn())
                ASTUtils.error(node, "The function "+node.getName()+" must return a variable of type "+node.getReturnType().getClassName());
        }
                
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(StructDefinition node) throws ASTVisitorException {
        for(VariableDefinition v: node.getVariables()){
            v.accept(this);
       }
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(ArrayAccessExpression node) throws ASTVisitorException {
        node.getIndex().accept(this);
        SymTable<SymTableEntry> st = ASTUtils.getSafeSymbolTable(node);
        SymTableEntry entry  = st.lookup(node.getIdentifier());
        if(entry == null)
            ASTUtils.error(node,"The array "+node.getIdentifier()+" is not defined.");
        
        ASTUtils.setType(node, entry.getType().getElementType());
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
        Type structType = ASTUtils.getSafeType(node.getStruct());
        
        SymTable<SymTableEntry> st = Registry.getInstance().getStructs().get(structType.toString());
        SymTableEntry entry = st.lookupOnlyInTop(node.getIdentifier());
        if(entry == null)
            ASTUtils.error(node, "The struct "+structType+" doesn't contain variable "+node.getIdentifier());
        
        ASTUtils.setType(node,entry.getType().getElementType());
    }

    @Override
    public void visit(StructVariableAccessExpression node) throws ASTVisitorException {
        node.getStruct().accept(this);
        Type structType = ASTUtils.getSafeType(node.getStruct());
        
        SymTable<SymTableEntry> st = Registry.getInstance().getStructs().get(structType.toString());
        SymTableEntry entry = st.lookupOnlyInTop(node.getIdentifier());
        if(entry == null)
            ASTUtils.error(node, "The struct "+structType+" doesn't contain variable "+node.getIdentifier());
        
        
        ASTUtils.setType(node,entry.getType());
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        if(!ASTUtils.getWhileLoopState(node)){
            ASTUtils.error(node,"Break statement should be inside a while Loop.");
        }
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        if(!ASTUtils.getWhileLoopState(node)){
            ASTUtils.error(node,"Continue statement should be inside a while Loop.");
        }
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(EmptyStatement node) throws ASTVisitorException {
        node.getExpression().accept(this); 
        ASTUtils.setType(node, ASTUtils.getSafeType(node.getExpression()));
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        if(ASTUtils.getSafeType(node.getExpression())!=Type.BOOLEAN_TYPE)
            ASTUtils.error(node,"The Expression must be logical");
        
        node.getStatement().accept(this);
        node.getElseStatement().accept(this);
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        if(ASTUtils.getSafeType(node.getExpression())!=Type.BOOLEAN_TYPE)
            ASTUtils.error(node,"The Expression must be logical");
        
        node.getStatement().accept(this);
        ASTUtils.setType(node, Type.VOID_TYPE);
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
        
        ASTUtils.setType(node,returnType);
    }

    @Override
    public void visit(VariableDefinition node) throws ASTVisitorException {
        node.getVariable().accept(this);
        

        ASTUtils.setType(node,ASTUtils.getType(node.getVariable()));
    }

   private void findMainFunction() throws ASTVisitorException{
       SymTable<SymTableEntry>  rootSymbolTable = ASTUtils.getSafeSymbolTable(Registry.getInstance().getRoot());
       
       SymTableEntry mainFunction = rootSymbolTable.lookup(Environment.MAIN_FUNCTION);
       if(mainFunction == null)
           throw new ASTVisitorException("Cannot find main function.");
       
       Type expectedType = Type.getMethodType(Type.VOID_TYPE);
       if(!mainFunction.getType().equals(expectedType))
           throw new ASTVisitorException("Main function must return void and have 0 parameters.");
   }

}
