package ast;

import ast.visitor.ASTVisitorException;
import ast.expression.Expression;
import symbol.SymTable;
import symbol.SymTableEntry;

import org.objectweb.asm.Type;

public class ASTUtils {

    public static final String SYMTABLE_PROPERTY = "SYMTABLE_PROPERTY";
    public static final String IS_BOOLEAN_EXPR_PROPERTY = "IS_BOOLEAN_EXPR_PROPERTY";
    public static final String IS_NUMBER_EXPR_PROPERTY = "IS_NUMBER_EXPR_PROPERTY";
    public static final String TYPE_PROPERTY = "TYPE_PROPERTY";
    public static final String IS_IN_WHILE_LOOP = "IS_IN_WHILE_LOOP";
    public static final String IS_IN_FUNCTION_DEFINITION = "IS_IN_FUNCTION_DEFINITION";
    public static final String CURRENT_FUNCTION_NAME = "CURRENT_FUNCTION_NAME";

    private ASTUtils() {
    }

    @SuppressWarnings("unchecked")
    public static SymTable<SymTableEntry> getSymbolTable(ASTNode node) {
        return (SymTable<SymTableEntry>) node.getProperty(SYMTABLE_PROPERTY);
    }

    @SuppressWarnings("unchecked")
    public static SymTable<SymTableEntry> getSafeSymbolTable(ASTNode node)
            throws ASTVisitorException {
        SymTable<SymTableEntry> symTable = (SymTable<SymTableEntry>) node
                .getProperty(SYMTABLE_PROPERTY);
        if (symTable == null) {
            ASTUtils.error(node, "Symbol table not found.");
        }
        return symTable;
    }

    public static void setSymbolTable(ASTNode node, SymTable<SymTableEntry> env) {
        node.setProperty(SYMTABLE_PROPERTY, env);
    }

    public static boolean isBooleanExpression(Expression node) {
        Boolean b = (Boolean) node.getProperty(IS_BOOLEAN_EXPR_PROPERTY);
        if (b == null) {
            return false;
        }
        return b;
    }

    public static void setBooleanExpression(Expression node, boolean value) {
        node.setProperty(IS_BOOLEAN_EXPR_PROPERTY, value);
    }

    public static Type getType(ASTNode node) {
        return (Type) node.getProperty(TYPE_PROPERTY);
    }

    public static Type getSafeType(ASTNode node) throws ASTVisitorException {
        Type type = (Type) node.getProperty(TYPE_PROPERTY);
        if (type == null) {
            ASTUtils.error(node, "Type not found.");
        }
        return type;
    }

    public static void setType(ASTNode node, Type type) {
        node.setProperty(TYPE_PROPERTY, type);
    }

    public static void setWhileLoopState(ASTNode node, boolean state){
        node.setProperty(IS_IN_WHILE_LOOP, state);
    }

    public static boolean getWhileLoopState(ASTNode node){
        return  (Boolean)node.getProperty(IS_IN_WHILE_LOOP);
    }

    public static void setFunctionProperties(ASTNode node,boolean state, String functionName){
        node.setProperty(IS_IN_FUNCTION_DEFINITION, state);
        if(functionName != "")
            node.setProperty(CURRENT_FUNCTION_NAME, functionName);
    }

    public static boolean getFunctionState(ASTNode node){
        return  (Boolean)node.getProperty(IS_IN_FUNCTION_DEFINITION);
    }

    public static String getCurrentFunctionName(ASTNode node){
        return (String) node.getProperty(CURRENT_FUNCTION_NAME);
    }

    public static void error(ASTNode node, String message)
            throws ASTVisitorException {
        throw new ASTVisitorException(node.getLine() + ":" + node.getColumn()
                + ": " + message);
    }

}