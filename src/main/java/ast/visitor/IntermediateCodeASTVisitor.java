/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package ast.visitor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import threeaddr.*;
import ast.*;
import ast.definition.Array;
import ast.definition.Definition;
import ast.definition.FunctionDefinition;
import ast.definition.StructDefinition;
import ast.definition.Variable;
import ast.definition.VariableDefinition;
import ast.expression.*;
import ast.statement.*;
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
        for (Definition d : node.getDefinitions()) {
            d.accept(this);
        }

        /*
         Statement s = null, ps;
        Iterator<Statement> it = node.getStatements().iterator();
        while (it.hasNext()) {
            ps = s;
            s = it.next();

            if (ps != null && !ASTUtils.getNextList(ps).isEmpty()) {
                Program.backpatch(ASTUtils.getNextList(ps), program.addNewLabel());
            }

            s.accept(this);

            if (!ASTUtils.getBreakList(s).isEmpty()) {
                ASTUtils.error(s, "Break detected without a loop.");
            }

            if (!ASTUtils.getContinueList(s).isEmpty()) {
                ASTUtils.error(s, "Continue detected without a loop.");
            }
        }
        if (s != null && !ASTUtils.getNextList(s).isEmpty()) {
            Program.backpatch(ASTUtils.getNextList(s), program.addNewLabel());
        }
        */
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);
        /*
        node.getExpression().accept(this);
        String t = stack.pop();
        program.add(new AssignInstr(t, node.getIdentifier()));
        */

    }

    
    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        //If father is boolExpr, set this expression boolean
        node.getExpression1().accept(this);
        String t1 = stack.pop();
        node.getExpression2().accept(this);
        String t2 = stack.pop();

        if (ASTUtils.isBooleanExpression(node)) {
            if (!node.getOperator().isRelational()) {
                ASTUtils.error(node, "A not boolean expression used as boolean.");
            }
            // if op == '&&' or op='||'
            // &&:
            //     create label anamesa se expr1 kai expr2
            
            //      false list tou expr1 -> falselist
            //      true list tou expr1 -> Label(prin to expr2)
            
            //      false list tou expr2 -> falselist
            //      true list tou expr2 -> true list
            
            // ||:
            //    antistoixa me to && alla me prosoxi
            
            
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
        //If father is boolExpr, set this expression boolean
        node.getExpression().accept(this);
        String t1 = stack.pop();
        String t = createTemp();
        stack.push(t);
        
        // if op == !  (NOT)
        //      antimetathesi twn stoixeiwn tou false list me to true list
        
        program.add(new UnaryOpInstr(node.getOperator(), t1, t));
    }
    
    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        stack.push(node.getIdentifier());
    }

    
    @Override
    public void visit(FloatLiteralExpression node) throws ASTVisitorException {
        //TO DO: FLOAT CANNOT BE USED AS BOOLEAN EXPRESSIONS
        if (ASTUtils.isBooleanExpression(node)) {
            if (node.getLiteral() != 0d) {
                GotoInstr i = new GotoInstr();
                program.add(i);
                ASTUtils.getTrueList(node).add(i);
            } else {
                GotoInstr i = new GotoInstr();
                program.add(i);
                ASTUtils.getFalseList(node).add(i);
            }
        } else {
            String t = createTemp();
            stack.push(t);
            program.add(new AssignInstr(node.getLiteral().toString(), t));
        }
    }

    
    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        //TO DO: INTEGERS CANNOT BE USED AS BOOLEAN EXPRESSIONS
        if (ASTUtils.isBooleanExpression(node)) {
            if (node.getLiteral() != 0) {
                GotoInstr i = new GotoInstr();
                program.add(i);
                ASTUtils.getTrueList(node).add(i);
            } else {
                GotoInstr i = new GotoInstr();
                program.add(i);
                ASTUtils.getFalseList(node).add(i);
            }
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
        node.getType().accept(this);
        System.out.print(" "+ node.getName() +"[" +node.getLength()+ "]");
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        node.getVariable().accept(this);
    }

    @Override
    public void visit(Variable node) throws ASTVisitorException {
        node.getType().accept(this);
        System.out.print(" "+node.getName());
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        System.out.println("");
        node.getReturnType().accept(this);
        System.out.print(" "+node.getName() + "(" );
        for(ParameterDeclaration p: node.getParameters()){
            p.accept(this);
            if(node.getParameters().indexOf(p) != node.getParameters().size()-1)
                System.out.print(",");
        }
        System.out.println(") {");
        for(Statement s: node.getStatements()){
            s.accept(this);
        }
        System.out.println("}");
    }

    @Override
    public void visit(StructDefinition node) throws ASTVisitorException {
        System.out.println("");
        System.out.println("struct "+node.getName()+" {");
        for(VariableDefinition v: node.getVariables()){
             v.accept(this);
        }
        System.out.println("};");
    }

    @Override
    public void visit(ArrayAccessExpression node) throws ASTVisitorException {
        System.out.print(node.getIdentifier()+"[");
        node.getIndex().accept(this);
        System.out.print("]");
    }

    @Override
    public void visit(BooleanLiteralExpression node) throws ASTVisitorException {
        System.out.print(node.isExpression());
    }

    @Override
    public void visit(CharLiteralExpression node) throws ASTVisitorException {
        System.out.print("\'");
        System.out.print(node.getExpression());
        System.out.print("\'");
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {
        System.out.print(node.getIdentifier()+"(");
        for(Expression e: node.getExpressions()){
            e.accept(this);
        }
        System.out.print(");");
    }

    @Override
    public void visit(StructArrayAccessExpression node) throws ASTVisitorException {
        System.out.print(" ");
        node.getStruct().accept(this);
        System.out.print("."+node.getIdentifier()+"[");
        node.getIndex().accept(this);
        System.out.print("]");
    }

    @Override
    public void visit(StructVariableAccessExpression node) throws ASTVisitorException {
        System.out.print(" ");
        node.getStruct().accept(this);
        System.out.print("."+node.getIdentifier());
    }

    @Override
    public void visit(EmptyStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        System.out.println(";");
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        System.out.print("return ");
        if(node.getExpression()!=null)
            node.getExpression().accept(this);
        System.out.println(";");
    }

    @Override
    public void visit(TypeSpecifier node) throws ASTVisitorException {
        /*if(node.getType()==TypeEnum.STRUCT){
            System.out.print("struct "+node.getStuctId());
        }else{
            System.out.print(node.getType());
        }*/
        System.out.print(node.getType().getClassName());
    }
    
    @Override
    public void visit(StructSpecifier node) throws ASTVisitorException {
        System.out.print(node.getType());
    }

    @Override
    public void visit(VariableDefinition node) throws ASTVisitorException {
        node.getVariable().accept(this);
        System.out.print(";");
        System.out.println("");
    }


}
