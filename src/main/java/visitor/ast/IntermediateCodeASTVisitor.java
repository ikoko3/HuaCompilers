/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package visitor.ast;

import ast.definition.ParameterDeclaration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import threeaddr.*;
import types.TypeUtils;
import ast.*;
import ast.definition.*;
import ast.expression.*;
import ast.statement.*;
import core.*;

import org.apache.commons.lang3.StringEscapeUtils;
import org.objectweb.asm.Type;

public class IntermediateCodeASTVisitor implements ASTVisitor {

    private final Program program;
    private final Deque<String> stack;
    private int struct_var;

    public IntermediateCodeASTVisitor() {
        program = new Program();
        stack = new ArrayDeque<String>();
        struct_var = 0;
    }

    private String createTemp() {
        return "t" + Integer.toString(struct_var++);
    }

    public Program getProgram() {
        return program;
    }

    @Override
    public void visit(CompUnit node) throws ASTVisitorException {

        Definition d = null, pd;
        Iterator<Definition> it = node.getDefinitions().iterator();

        while (it.hasNext()) {
            pd = d;
            d = it.next();

            backpatchNextList(pd);
            d.accept(this);

            //IF statement is while -> add label backpatch break, continue
        }
        backpatchNextList(d);

    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        node.getResult().accept(this);
        String res = stack.pop();
        node.getTarget().accept(this);
        String t = stack.pop();

        program.add(new AssignInstr(t, res));

    }

    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        //Expression1
        InheritBooleanAttributes(node, node.getExpression1());
        node.getExpression1().accept(this);
        String t1 = stack.pop();

        LabelInstr intrmLbl = null;
        if (node.getOperator().isLogical()) {
            intrmLbl = program.addNewLabel();
        }

        //Expression2
        InheritBooleanAttributes(node, node.getExpression2());
        node.getExpression2().accept(this);
        String t2 = stack.pop();

        if (ASTUtils.isBooleanExpression(node)) {
            if (!node.getOperator().isRelational() && !node.getOperator().isLogical()) {
                ASTUtils.error(node, "A not boolean expression used as boolean.");
            }
            switch (node.getOperator()) {
                case AND:
                    ThreeAddrUtils.getFalseList(node).addAll(ThreeAddrUtils.getFalseList(node.getExpression1()));
                    Program.backpatch(ThreeAddrUtils.getTrueList(node.getExpression1()), intrmLbl);

                    ThreeAddrUtils.getFalseList(node).addAll(ThreeAddrUtils.getFalseList(node.getExpression2()));
                    ThreeAddrUtils.getTrueList(node).addAll(ThreeAddrUtils.getTrueList(node.getExpression2()));
                    break;
                case OR:
                    ThreeAddrUtils.getTrueList(node).addAll(ThreeAddrUtils.getTrueList(node.getExpression1()));
                    Program.backpatch(ThreeAddrUtils.getFalseList(node.getExpression1()), intrmLbl);

                    ThreeAddrUtils.getFalseList(node).addAll(ThreeAddrUtils.getFalseList(node.getExpression2()));
                    ThreeAddrUtils.getTrueList(node).addAll(ThreeAddrUtils.getTrueList(node.getExpression2()));
                    break;
                default:
                    break;
            }

            CondJumpInstr condJumpInstr = new CondJumpInstr(node.getOperator(), t1, t2);
            program.add(condJumpInstr);
            ThreeAddrUtils.getTrueList(node).add(condJumpInstr);

            GotoInstr gotoInstr = new GotoInstr();
            program.add(gotoInstr);
            ThreeAddrUtils.getFalseList(node).add(gotoInstr);

        } else {

            String t = createTemp();
            program.add(new BinaryOpInstr(node.getOperator(), t1, t2, t));
            stack.push(t);
        }
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        InheritBooleanAttributes(node, node.getExpression());
        node.getExpression().accept(this);
        String t1 = stack.pop();
        String t = createTemp();
        stack.push(t);

        if (node.getOperator().equals(Operator.NOT)) {
            List<GotoInstr> struct_varList = ThreeAddrUtils.getFalseList(node);
            ThreeAddrUtils.setFalseList(node, ThreeAddrUtils.getTrueList(node));
            ThreeAddrUtils.setTrueList(node, struct_varList);
        }

        program.add(new UnaryOpInstr(node.getOperator(), t1, t));
    }

    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        stack.push(node.getIdentifier());
    }

    @Override
    public void visit(FloatLiteralExpression node) throws ASTVisitorException {

        String t = createTemp();
        stack.push(t);
        program.add(new AssignInstr(t,node.getLiteral().toString()));

    }

    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {

        String t = createTemp();
        stack.push(t);
        program.add(new AssignInstr(t,node.getLiteral().toString()));

    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {

        String t = createTemp();
        stack.push(t);
        program.add(new AssignInstr(t,"\"" + StringEscapeUtils.escapeJava(node.getLiteral()) + "\""));

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
        Program.backpatch(ThreeAddrUtils.getTrueList(node.getExpression()), beginStmtLabel);

        node.getStatement().accept(this);
        Program.backpatch(ThreeAddrUtils.getNextList(node.getStatement()), beginLabel);
        Program.backpatch(ThreeAddrUtils.getContinueList(node.getStatement()), beginLabel);
        
        program.add(new GotoInstr(beginLabel));
        ThreeAddrUtils.getNextList(node).addAll(ThreeAddrUtils.getFalseList(node.getExpression()));
        ThreeAddrUtils.getNextList(node).addAll(ThreeAddrUtils.getBreakList(node.getStatement()));
        
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        node.getExpression().accept(this);
        LabelInstr beginStmtLabel = program.addNewLabel();
        Program.backpatch(ThreeAddrUtils.getTrueList(node.getExpression()), beginStmtLabel);
        node.getStatement().accept(this);

        ThreeAddrUtils.getNextList(node).addAll(ThreeAddrUtils.getFalseList(node.getExpression()));
        ThreeAddrUtils.getNextList(node).addAll(ThreeAddrUtils.getNextList(node.getStatement()));

        ThreeAddrUtils.setBreakList(node, ThreeAddrUtils.getBreakList(node.getStatement()));
        ThreeAddrUtils.setContinueList(node, ThreeAddrUtils.getContinueList(node.getStatement()));
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        ASTUtils.setBooleanExpression(node.getExpression(), true);

        node.getExpression().accept(this);
        LabelInstr beginStmtLabel = program.addNewLabel();
        Program.backpatch(ThreeAddrUtils.getTrueList(node.getExpression()), beginStmtLabel);
        node.getStatement().accept(this);
        GotoInstr gotoInstr = new GotoInstr();
        program.add(gotoInstr);

        LabelInstr beginElseLabel = program.addNewLabel();
        Program.backpatch(ThreeAddrUtils.getFalseList(node.getExpression()), beginElseLabel);

        ThreeAddrUtils.getNextList(node).addAll(ThreeAddrUtils.getNextList(node.getStatement()));
        ThreeAddrUtils.getNextList(node).addAll(ThreeAddrUtils.getNextList(node.getElseStatement()));
        ThreeAddrUtils.getNextList(node).add(gotoInstr);

        ThreeAddrUtils.setBreakList(node, ThreeAddrUtils.getBreakList(node.getStatement()));
        ThreeAddrUtils.setBreakList(node, ThreeAddrUtils.getBreakList(node.getElseStatement()));

        ThreeAddrUtils.setContinueList(node, ThreeAddrUtils.getContinueList(node.getStatement()));
        ThreeAddrUtils.setContinueList(node, ThreeAddrUtils.getContinueList(node.getElseStatement()));
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {

        GotoInstr gotoInstr = new GotoInstr();
        program.add(gotoInstr);
        ThreeAddrUtils.getBreakList(node).add(gotoInstr);

    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {

        GotoInstr gotoInstr = new GotoInstr();
        program.add(gotoInstr);
        ThreeAddrUtils.getContinueList(node).add(gotoInstr);

    }

    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        List<GotoInstr> breakList = new ArrayList<GotoInstr>();
        List<GotoInstr> continueList = new ArrayList<GotoInstr>();
        Statement ps,s=null;
        Iterator<Statement> it = node.getStatements().iterator();

        while (it.hasNext()) {
            ps = s;
            s = it.next();

            backpatchNextList(ps);
            s.accept(this);
            breakList.addAll(ThreeAddrUtils.getBreakList(s));
            continueList.addAll(ThreeAddrUtils.getContinueList(s));
        }
        backpatchNextList(s);

        ThreeAddrUtils.setBreakList(node, breakList);
        ThreeAddrUtils.setContinueList(node, continueList);
    }

    @Override
    public void visit(Array node) throws ASTVisitorException {

    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        node.getVariable().accept(this);       
    }

    @Override
    public void visit(Variable node) throws ASTVisitorException {

    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {

        LabelInstr funcLabel = new LabelInstr(node.getName());
        program.add(funcLabel);

        Statement ps,s=null;
        Iterator<Statement> it = node.getStatements().iterator();

        while (it.hasNext()) {
            ps = s;
            s = it.next();

            backpatchNextList(ps);
            s.accept(this);
            
        }
        backpatchNextList(s);
    }

    @Override
    public void visit(StructDefinition node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(ArrayAccessExpression node) throws ASTVisitorException {
        
        String t = createTemp();
        Type type = ASTUtils.getType(node);
        String type_size = String.valueOf(type.getSize()*4);
        node.getIndex().accept(this);
        String i = stack.pop();
        String arr = Registry.getInstance().getDefinedArrays().get(node.getIdentifier());
        arr = arr + "." + t;
        
        program.add(new BinaryOpInstr(Operator.MULTIPLY,type_size,i,t));
        stack.push(arr);
    }

    @Override
    public void visit(BooleanLiteralExpression node) throws ASTVisitorException {
        
    }

    @Override
    public void visit(CharLiteralExpression node) throws ASTVisitorException {
        String t = createTemp();
        stack.push(t);
        program.add(new AssignInstr(t,"\'" + node.getExpression() + "\'"));
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {

        List<String> params = new ArrayList<String>();
        for (Expression e : node.getExpressions()) {
            e.accept(this);
            String param = stack.pop();
            params.add(param);
        }

        for (String param : params) {
            program.add(new ParamInstr(param));
            stack.push(param);
        }
        program.add(new FunctionCallInstr(node.getIdentifier(), node.getExpressions().size()));
    }

    @Override
    public void visit(StructArrayAccessExpression node) throws ASTVisitorException {
        node.getStruct().accept(this);
       
        String struct_var;
        if(node.getStruct() instanceof IdentifierExpression){
            IdentifierExpression  expr = (IdentifierExpression)node.getStruct();
            struct_var = Registry.getInstance().getDefinedStructs().get(expr.getIdentifier());
        }else{
            struct_var = stack.pop();
        }
        struct_var = struct_var+"."+node.getIdentifier();

        Type type = ASTUtils.getType(node);
        String type_size = String.valueOf(type.getSize()*4);

        

        node.getIndex().accept(this);
        String index = stack.pop();
        String address = createTemp();
        program.add(new BinaryOpInstr(Operator.MULTIPLY,type_size,index,address));

        String t1 = createTemp();
        program.add(new AssignInstr(t1, struct_var));

        String arr = Registry.getInstance().getDefinedArrays().get(node.getIdentifier());
        arr = t1 + "." + address; 

        stack.push(arr); 
        
        
    }

    @Override
    public void visit(StructVariableAccessExpression node) throws ASTVisitorException {
        node.getStruct().accept(this);
       
        IdentifierExpression  expr = (IdentifierExpression)node.getStruct();
        String struct_var = Registry.getInstance().getDefinedStructs().get(expr.getIdentifier());
        struct_var = struct_var+"."+node.getIdentifier();
        //We know from the previous visitor that the struct is valid.

        stack.push(struct_var);

    }

    @Override
    public void visit(EmptyStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        
        ReturnInstr returnInstr = new ReturnInstr();
        
        if (node.getExpression() != null) {
            node.getExpression().accept(this);
            String t = stack.pop();
            returnInstr.setValue(t);
        }
        program.add(returnInstr);
    }

    @Override
    public void visit(VariableDefinition node) throws ASTVisitorException {
              
        Type type = node.getVariable().getType();
        if(TypeUtils.isStructType(type)){
            String t= createTemp();
            program.add(new StructInitInstr(t,TypeUtils.getStructId(type)));
            Registry.getInstance().getDefinedStructs().put(node.getVariable().getName(), t);
        }
        if (node.getVariable() instanceof Array){
            String t = createTemp();
            Registry.getInstance().getDefinedArrays().put(node.getVariable().getName(), t);
        }
    }

    private void backpatchNextList(Statement s) {
        if (s != null && !ThreeAddrUtils.getNextList(s).isEmpty()) {
            Program.backpatch(ThreeAddrUtils.getNextList(s), program.addNewLabel());
        }
    }

    private void InheritBooleanAttributes(ASTNode parent, Expression node) {
        if (parent instanceof Expression && ASTUtils.isBooleanExpression((Expression) parent)) {
            ASTUtils.setBooleanExpression(node, true);
        }

    }

}
