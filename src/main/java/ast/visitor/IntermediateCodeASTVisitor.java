/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package ast.visitor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import threeaddr.*;
import ast.*;
import ast.definition.*;
import ast.expression.*;
import ast.statement.*;
import core.Operator;

import org.apache.commons.lang3.StringEscapeUtils;


public class IntermediateCodeASTVisitor implements ASTVisitor {

    private final Program program;
    private final Deque<String> stack;
    private int temp;

    public IntermediateCodeASTVisitor() {
        program = new Program();
        stack = new ArrayDeque<String>();
        temp = 0;
    }

    private String createTemp() {
        return "t" + Integer.toString(temp++);
    }

    public Program getProgram() {
        return program;
    }

    @Override
    public void visit(CompUnit node) throws ASTVisitorException {

        Definition d = null, pd;
        Iterator<Definition> it = node.getDefinitions().iterator();

       while (it.hasNext()){
           pd = d;
           d = it.next();
          
           d.accept(this);
           backpatchNextList(pd);

       }
       backpatchNextList(d);
    
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        node.getTarget().accept(this);
        String t = stack.pop();
        node.getResult().accept(this);
        String res = stack.pop();

        program.add(new AssignInstr(t, res));
        

    }

    
    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        //Expression1
        InheritBooleanAttributes(node,node.getExpression1());
        node.getExpression1().accept(this);
        String t1 = stack.pop();

        LabelInstr intrmLbl = null;
        if(node.getOperator().isLogical())
        intrmLbl = program.addNewLabel();

        //Expression2
        InheritBooleanAttributes(node,node.getExpression2());
        node.getExpression2().accept(this);
        String t2 = stack.pop();

        if (ASTUtils.isBooleanExpression(node)) {
            if (!node.getOperator().isRelational() && !node.getOperator().isLogical()) {
                ASTUtils.error(node, "A not boolean expression used as boolean.");
            }
            switch(node.getOperator()){
                case AND:
                    ASTUtils.getFalseList(node).addAll(ASTUtils.getFalseList(node.getExpression1()));
                    Program.backpatch(ASTUtils.getTrueList(node.getExpression1()),intrmLbl);

                    ASTUtils.getFalseList(node).addAll(ASTUtils.getFalseList(node.getExpression2()));
                    ASTUtils.getTrueList(node).addAll(ASTUtils.getTrueList(node.getExpression2()));
                    break;
                case OR:
                    ASTUtils.getTrueList(node).addAll(ASTUtils.getTrueList(node.getExpression1()));
                    Program.backpatch(ASTUtils.getFalseList(node.getExpression1()),intrmLbl);

                    ASTUtils.getFalseList(node).addAll(ASTUtils.getFalseList(node.getExpression2()));
                    ASTUtils.getTrueList(node).addAll(ASTUtils.getTrueList(node.getExpression2()));
                    break;
                default:
                    break;
            }            
            
            CondJumpInstr condJumpInstr = new CondJumpInstr(node.getOperator(), t1, t2);
            program.add(condJumpInstr);
            ASTUtils.getTrueList(node).add(condJumpInstr);
            
            GotoInstr gotoInstr = new GotoInstr();
            program.add(gotoInstr);
            ASTUtils.getFalseList(node).add(gotoInstr);

        } else {

            String t = createTemp();
            program.add(new BinaryOpInstr(node.getOperator(),t1,t2,t));
            stack.push(t);
        }
    }
    
    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        InheritBooleanAttributes(node,node.getExpression());
        node.getExpression().accept(this);
        String t1 = stack.pop();
        String t = createTemp();
        stack.push(t);

        if(node.getOperator().equals(Operator.NOT)){
            List<GotoInstr> tempList = ASTUtils.getFalseList(node);
            ASTUtils.setFalseList(node, ASTUtils.getTrueList(node));
            ASTUtils.setTrueList(node,tempList);
        }
        
        program.add(new UnaryOpInstr(node.getOperator(), t1, t));
    }
    
    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        stack.push(node.getIdentifier());
    }

    
    @Override
    public void visit(FloatLiteralExpression node) throws ASTVisitorException {
        if (ASTUtils.isBooleanExpression(node)) {
            ASTUtils.error(node, "Floats cannot be used as boolean expressions");
        } else {
            String t = createTemp();
            stack.push(t);
            program.add(new AssignInstr(node.getLiteral().toString(), t));
        }
    }

    
    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        if (ASTUtils.isBooleanExpression(node)) {
            ASTUtils.error(node, "Integers cannot be used as boolean expressions");
        } else {
            String t = createTemp();
            stack.push(t);
            program.add(new AssignInstr(node.getLiteral().toString(), t));
        }
    }
    
    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        if (ASTUtils.isBooleanExpression(node)) {
            ASTUtils.error(node, "Strings cannot be used as boolean expressions");
        } else {
            String t = createTemp();
            stack.push(t);
            program.add(new AssignInstr("\"" + StringEscapeUtils.escapeJava(node.getLiteral()) + "\"", t));
        }
    }
    
    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
        String t1 = stack.pop();
        String t = createTemp();
        stack.push(t);
        program.add(new AssignInstr(t1, t));
    }
    
    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);
        
        LabelInstr beginLabel = program.addNewLabel();
        node.getExpression().accept(this);
        LabelInstr beginStmtLabel = program.addNewLabel();
        Program.backpatch(ASTUtils.getTrueList(node.getExpression()), beginStmtLabel);
        
        node.getStatement().accept(this);
        Program.backpatch(ASTUtils.getNextList(node.getStatement()), beginLabel);
        Program.backpatch(ASTUtils.getContinueList(node.getStatement()), beginLabel);
        
        program.add(new GotoInstr(beginLabel));
        ASTUtils.getNextList(node).addAll(ASTUtils.getFalseList(node.getExpression()));
        ASTUtils.getNextList(node).addAll(ASTUtils.getBreakList(node.getStatement()));
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        node.getExpression().accept(this);
        LabelInstr beginStmtLabel = program.addNewLabel();
        Program.backpatch(ASTUtils.getTrueList(node.getExpression()), beginStmtLabel);
        node.getStatement().accept(this);
        
        ASTUtils.getNextList(node).addAll(ASTUtils.getFalseList(node.getExpression()));
        ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getStatement()));
        
        ASTUtils.setBreakList(node,ASTUtils.getBreakList(node.getStatement()));
        ASTUtils.setContinueList(node,ASTUtils.getContinueList(node.getStatement()));
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        ASTUtils.setBooleanExpression(node.getExpression(), true);

        node.getExpression().accept(this);
        LabelInstr beginStmtLabel = program.addNewLabel();
        Program.backpatch(ASTUtils.getTrueList(node.getExpression()), beginStmtLabel);
        node.getStatement().accept(this);
        GotoInstr gotoInstr = new GotoInstr();
        program.add(gotoInstr);
        
        LabelInstr beginElseLabel = program.addNewLabel();
        Program.backpatch(ASTUtils.getFalseList(node.getExpression()), beginElseLabel);
        
        ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getStatement()));
        ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getElseStatement()));
        ASTUtils.getNextList(node).add(gotoInstr);
        
        ASTUtils.setBreakList(node,ASTUtils.getBreakList(node.getStatement()));
        ASTUtils.setBreakList(node,ASTUtils.getBreakList(node.getElseStatement()));
        
        ASTUtils.setContinueList(node,ASTUtils.getContinueList(node.getStatement()));
        ASTUtils.setContinueList(node,ASTUtils.getContinueList(node.getElseStatement()));
    }
    
    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        
        GotoInstr gotoInstr = new GotoInstr();
        program.add(gotoInstr);
        ASTUtils.getBreakList(node).add(gotoInstr);
       
    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        
        GotoInstr gotoInstr = new GotoInstr();
        program.add(gotoInstr);
        ASTUtils.getContinueList(node).add(gotoInstr);
        
    }
    
    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        List<GotoInstr> breakList = new ArrayList<GotoInstr>();
        List<GotoInstr> continueList = new ArrayList<GotoInstr>();
        Statement s = null, ps;
        Iterator<Statement> it = node.getStatements().iterator();
        while (it.hasNext()) {
            ps = s;
            s = it.next();
            if (ps != null && !ASTUtils.getNextList(ps).isEmpty()) {
                Program.backpatch(ASTUtils.getNextList(ps), program.addNewLabel());
            }
            s.accept(this);
            breakList.addAll(ASTUtils.getBreakList(s));
            continueList.addAll(ASTUtils.getContinueList(s));
        }
        if (s != null) {
            ASTUtils.setNextList(node, ASTUtils.getNextList(s));
        }
        ASTUtils.setBreakList(node, breakList);
        ASTUtils.setContinueList(node, continueList);
    }

    @Override
    public void visit(Array node) throws ASTVisitorException {
        //TODO: Add 3 address code

        node.getType().accept(this);
        System.out.print(" "+ node.getName() +"[" +node.getLength()+ "]");
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        //TODO: Add 3 address code

        node.getVariable().accept(this);
    }

    @Override
    public void visit(Variable node) throws ASTVisitorException {
        //TODO: Add 3 address code

        node.getType().accept(this);
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        //TODO: Add 3 address code

        LabelInstr funcLabel = new LabelInstr(node.getName());
        program.add(funcLabel);



        node.getReturnType().accept(this);
        for(ParameterDeclaration p: node.getParameters()){
            //p.accept(this);
            //String t = stack.pop();
            //String t1 = createTemp();
            //stack.push(t1);
        }
        for(Statement s: node.getStatements()){
            s.accept(this);
        }
    }

    @Override
    public void visit(StructDefinition node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(ArrayAccessExpression node) throws ASTVisitorException {
        //TODO: Add 3 address code

        System.out.print(node.getIdentifier()+"[");
        node.getIndex().accept(this);
        System.out.print("]");
    }

    @Override
    public void visit(BooleanLiteralExpression node) throws ASTVisitorException {
            if (node.isExpression() == true) {
                GotoInstr i = new GotoInstr();
                program.add(i);
                ASTUtils.getTrueList(node).add(i);
            } else {
                GotoInstr i = new GotoInstr();
                program.add(i);
                ASTUtils.getFalseList(node).add(i);
            }
        

            String t = createTemp();
            stack.push(t);
            //program.add(new AssignInstr(node.isExpression(), t));
    }

    @Override
    public void visit(CharLiteralExpression node) throws ASTVisitorException {
        if (ASTUtils.isBooleanExpression(node)) {
            ASTUtils.error(node, "Characters cannot be used as boolean expressions");
        } else {
            
            String t = createTemp();
            stack.push(t);
            program.add(new AssignInstr("\'" + node.getExpression() + "\'", t));
            
        }
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {

        List<String> params = new ArrayList<String>();
        for(Expression e: node.getExpressions()){
            e.accept(this);
            String param = stack.pop();
            params.add(param);
            stack.push(param);
        }

        for (String param : params) {
            program.add(new ParamInstr(param));
        }
        program.add(new FunctionCallInstr(node.getIdentifier(),node.getExpressions().size()));
    }

    @Override
    public void visit(StructArrayAccessExpression node) throws ASTVisitorException {
        //TODO: Add 3 address code

        System.out.print(" ");
        node.getStruct().accept(this);
        System.out.print("."+node.getIdentifier()+"[");
        node.getIndex().accept(this);
        System.out.print("]");
    }

    @Override
    public void visit(StructVariableAccessExpression node) throws ASTVisitorException {
        //TODO: Add 3 address code

        System.out.print(" ");
        node.getStruct().accept(this);
        System.out.print("."+node.getIdentifier());
    }

    @Override
    public void visit(EmptyStatement node) throws ASTVisitorException {
        //TODO: Add 3 address code

        node.getExpression().accept(this);
        System.out.println(";");
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        //TODO: Add 3 address code

        System.out.print("return ");
        if(node.getExpression()!=null)
            node.getExpression().accept(this);
        System.out.println(";");
    }

    @Override
    public void visit(TypeSpecifier node) throws ASTVisitorException {
        //TODO: Add 3 address code
    }
    
    @Override
    public void visit(StructSpecifier node) throws ASTVisitorException {
        //TODO: Add 3 address code
    }

    @Override
    public void visit(VariableDefinition node) throws ASTVisitorException {
        //TODO: Add 3 address code

        node.getVariable().accept(this);
    }

    private void backpatchNextList(Statement s){
        if(s != null && !ASTUtils.getNextList(s).isEmpty())
            Program.backpatch(ASTUtils.getNextList(s), program.addNewLabel());
    }

    private void InheritBooleanAttributes(ASTNode parent, Expression node){
        if(parent instanceof Expression && ASTUtils.isBooleanExpression((Expression)parent))
            ASTUtils.setBooleanExpression(node, true);
        
    }

}
