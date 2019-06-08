package ast;

import ast.visitor.ASTVisitorException;
import ast.expression.Expression;
import ast.statement.Statement;
import java.util.ArrayList;
import java.util.List;
import symbol.*;

import org.objectweb.asm.Type;
import threeaddr.GotoInstr;

public class ASTUtils {

    public static final String SYMTABLE_PROPERTY = "SYMTABLE_PROPERTY";
    public static final String LOCAL_INDEX_POOL_PROPERTY = "LOCAL_INDEX_POOL_PROPERTY";

    public static final String IS_BOOLEAN_EXPR_PROPERTY = "IS_BOOLEAN_EXPR_PROPERTY";
    public static final String IS_NUMBER_EXPR_PROPERTY = "IS_NUMBER_EXPR_PROPERTY";
    public static final String TYPE_PROPERTY = "TYPE_PROPERTY";
    public static final String IS_IN_WHILE_LOOP = "IS_IN_WHILE_LOOP";
    public static final String IS_IN_FUNCTION_DEFINITION = "IS_IN_FUNCTION_DEFINITION";
    public static final String CURRENT_FUNCTION_NAME = "CURRENT_FUNCTION_NAME";

    public static final String NEXT_LIST_PROPERTY = "NEXT_LIST_PROPERTY";
    public static final String BREAK_LIST_PROPERTY = "BREAK_LIST_PROPERTY";
    public static final String CONTINUE_LIST_PROPERTY = "CONTINUE_LIST_PROPERTY";
    public static final String TRUE_LIST_PROPERTY = "TRUE_LIST_PROPERTY";
    public static final String FALSE_LIST_PROPERTY = "FALSE_LIST_PROPERTY";

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

    public static void setLocalIndexPool(ASTNode node, LocalIndexPool pool) {
        node.setProperty(LOCAL_INDEX_POOL_PROPERTY, pool);
    }

    @SuppressWarnings("unchecked")
    public static LocalIndexPool getSafeLocalIndexPool(ASTNode node)
            throws ASTVisitorException {
        LocalIndexPool lip = (LocalIndexPool) node.getProperty(LOCAL_INDEX_POOL_PROPERTY);
        if (lip == null) {
            ASTUtils.error(node, "Local index pool not found.");
        }
        return lip;
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

    @SuppressWarnings("unchecked")
    public static List<GotoInstr> getTrueList(Expression node) {
        List<GotoInstr> l = (List<GotoInstr>) node.getProperty(TRUE_LIST_PROPERTY);
        if (l == null) {
            l = new ArrayList<GotoInstr>();
            node.setProperty(TRUE_LIST_PROPERTY, l);
        }
        return l;
    }

    public static void setTrueList(Expression node, List<GotoInstr> list) {
        node.setProperty(TRUE_LIST_PROPERTY, list);
    }

    @SuppressWarnings("unchecked")
    public static List<GotoInstr> getFalseList(Expression node) {
        List<GotoInstr> l = (List<GotoInstr>) node.getProperty(FALSE_LIST_PROPERTY);
        if (l == null) {
            l = new ArrayList<GotoInstr>();
            node.setProperty(FALSE_LIST_PROPERTY, l);
        }
        return l;
    }

    public static void setFalseList(Expression node, List<GotoInstr> list) {
        node.setProperty(FALSE_LIST_PROPERTY, list);
    }

    @SuppressWarnings("unchecked")
    public static List<GotoInstr> getNextList(Statement node) {
        List<GotoInstr> l = (List<GotoInstr>) node.getProperty(NEXT_LIST_PROPERTY);
        if (l == null) {
            l = new ArrayList<GotoInstr>();
            node.setProperty(NEXT_LIST_PROPERTY, l);
        }
        return l;
    }

    public static void setNextList(Statement node, List<GotoInstr> list) {
        node.setProperty(NEXT_LIST_PROPERTY, list);
    }

    @SuppressWarnings("unchecked")
    public static List<GotoInstr> getBreakList(Statement node) {
        List<GotoInstr> l = (List<GotoInstr>) node.getProperty(BREAK_LIST_PROPERTY);
        if (l == null) {
            l = new ArrayList<GotoInstr>();
            node.setProperty(BREAK_LIST_PROPERTY, l);
        }
        return l;
    }

    public static void setBreakList(Statement node, List<GotoInstr> list) {
        node.setProperty(BREAK_LIST_PROPERTY, list);
    }

    @SuppressWarnings("unchecked")
    public static List<GotoInstr> getContinueList(Statement node) {
        List<GotoInstr> l = (List<GotoInstr>) node.getProperty(CONTINUE_LIST_PROPERTY);
        if (l == null) {
            l = new ArrayList<GotoInstr>();
            node.setProperty(CONTINUE_LIST_PROPERTY, l);
        }
        return l;
    }

    public static void setContinueList(Statement node, List<GotoInstr> list) {
        node.setProperty(CONTINUE_LIST_PROPERTY, list);
    }

    public static void error(ASTNode node, String message)
            throws ASTVisitorException {
        throw new ASTVisitorException(node.getLine() + ":" + node.getColumn()
                + ": " + message);
    }

}