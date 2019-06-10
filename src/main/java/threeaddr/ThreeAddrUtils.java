/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threeaddr;

import static ast.ASTUtils.BREAK_LIST_PROPERTY;
import static ast.ASTUtils.CONTINUE_LIST_PROPERTY;
import static ast.ASTUtils.FALSE_LIST_PROPERTY;
import static ast.ASTUtils.NEXT_LIST_PROPERTY;
import static ast.ASTUtils.TRUE_LIST_PROPERTY;
import ast.expression.Expression;
import ast.statement.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author john_
 */
public class ThreeAddrUtils {
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
}
