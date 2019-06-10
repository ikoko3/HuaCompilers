/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package ast.visitor;

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
import core.Operator;
import core.Registry;
import symbol.SymTableEntry;

import org.apache.commons.lang3.StringEscapeUtils;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;


public class BytecodeGeneratorASTVisitor implements ASTVisitor {

    private final Program program; //to remove
    private final Deque<String> stack;//to remove
    private int struct_var; //to remove

    private ClassNode cn;
    private MethodNode mn;

    public BytecodeGeneratorASTVisitor() {
// create class
        cn = new ClassNode();
        cn.access = Opcodes.ACC_PUBLIC;
        cn.version = Opcodes.V1_5;
        cn.name = "test";
        cn.sourceFile = "test.in";
        cn.superName = "java/lang/Object";

        // create constructor
        //kainourgio method node gia kathe sinartisi
        mn = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V"));
        mn.instructions.add(new InsnNode(Opcodes.RETURN));
        mn.maxLocals = 1;
        mn.maxStack = 1;
        cn.methods.add(mn);

        program = new Program(); //to remove
        stack = new ArrayDeque<String>(); //to remove
        struct_var = 0; //to remove
    }
    
    public ClassNode getClassNode() {
        return cn;
    }


    private String createTemp() { //to remove
        return "t" + Integer.toString(struct_var++);
    }

    public Program getProgram() { //to remove
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

        mn.instructions.add(new InsnNode(Opcodes.RETURN));
        mn.maxLocals = ASTUtils.getSafeLocalIndexPool(node).getMaxLocals() + 1;

        // IMPORTANT: this should be dynamically calculated
        // use COMPUTE_MAXS when computing the ClassWriter,
        // e.g. new ClassWriter(ClassWriter.COMPUTE_MAXS)
        mn.maxStack = 32;

        cn.methods.add(mn);
        //Prepei gia kathe function na exoyme allo method node
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        node.getResult().accept(this);
        Type exprType = ASTUtils.getSafeType(node.getResult());

        Expression target = node.getTarget();
        SymTableEntry symEntry = null;

        if(target instanceof IdentifierExpression){
            IdentifierExpression t =  (IdentifierExpression) target;
            symEntry = ASTUtils.getSafeSymbolTable(node).lookup(t.getIdentifier());
        }else if(target instanceof ArrayAccessExpression){
            ArrayAccessExpression t = (ArrayAccessExpression) target;
            symEntry = null;
        }else if(target instanceof StructVariableAccessExpression){
            StructVariableAccessExpression t = (StructVariableAccessExpression) target;
            symEntry = null;
        }else if(target instanceof StructArrayAccessExpression){
            StructArrayAccessExpression t = (StructArrayAccessExpression) target;
            symEntry = null;
        }else{
            ASTUtils.error(node, "Assingment is not implemented for the type "+target.getClass());
        }

        if(symEntry == null)
            ASTUtils.error(node, "Assingment is not implemented for the type "+target.getClass());
            
        widen(symEntry.getType(),exprType);
        
        mn.instructions.add(new VarInsnNode(symEntry.getType().getOpcode(Opcodes.ISTORE), symEntry.getIndex()));

    }

    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        // //Expression1
        // InheritBooleanAttributes(node, node.getExpression1());
        // node.getExpression1().accept(this);
        // String t1 = stack.pop();

        // LabelInstr intrmLbl = null;
        // if (node.getOperator().isLogical()) {
        //     intrmLbl = program.addNewLabel();
        // }

        // //Expression2
        // InheritBooleanAttributes(node, node.getExpression2());
        // node.getExpression2().accept(this);
        // String t2 = stack.pop();

        // if (ASTUtils.isBooleanExpression(node)) {
        //     if (!node.getOperator().isRelational() && !node.getOperator().isLogical()) {
        //         ASTUtils.error(node, "A not boolean expression used as boolean.");
        //     }
        //     switch (node.getOperator()) {
        //         case AND:
        //             ASTUtils.getFalseList(node).addAll(ASTUtils.getFalseList(node.getExpression1()));
        //             Program.backpatch(ASTUtils.getTrueList(node.getExpression1()), intrmLbl);

        //             ASTUtils.getFalseList(node).addAll(ASTUtils.getFalseList(node.getExpression2()));
        //             ASTUtils.getTrueList(node).addAll(ASTUtils.getTrueList(node.getExpression2()));
        //             break;
        //         case OR:
        //             ASTUtils.getTrueList(node).addAll(ASTUtils.getTrueList(node.getExpression1()));
        //             Program.backpatch(ASTUtils.getFalseList(node.getExpression1()), intrmLbl);

        //             ASTUtils.getFalseList(node).addAll(ASTUtils.getFalseList(node.getExpression2()));
        //             ASTUtils.getTrueList(node).addAll(ASTUtils.getTrueList(node.getExpression2()));
        //             break;
        //         default:
        //             break;
        //     }

        //     CondJumpInstr condJumpInstr = new CondJumpInstr(node.getOperator(), t1, t2);
        //     program.add(condJumpInstr);
        //     ASTUtils.getTrueList(node).add(condJumpInstr);

        //     GotoInstr gotoInstr = new GotoInstr();
        //     program.add(gotoInstr);
        //     ASTUtils.getFalseList(node).add(gotoInstr);

        // } else {

        //     String t = createTemp();
        //     program.add(new BinaryOpInstr(node.getOperator(), t1, t2, t));
        //     stack.push(t);
        //}
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        // InheritBooleanAttributes(node, node.getExpression());
        // node.getExpression().accept(this);
        // String t1 = stack.pop();
        // String t = createTemp();
        // stack.push(t);

        // if (node.getOperator().equals(Operator.NOT)) {
        //     List<GotoInstr> struct_varList = ASTUtils.getFalseList(node);
        //     ASTUtils.setFalseList(node, ASTUtils.getTrueList(node));
        //     ASTUtils.setTrueList(node, struct_varList);
        // }

        // program.add(new UnaryOpInstr(node.getOperator(), t1, t));
    }

    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        SymTableEntry symEntry = ASTUtils.getSafeSymbolTable(node).lookup(node.getIdentifier());
        mn.instructions.add(new VarInsnNode((symEntry.getType().getOpcode(Opcodes.ILOAD)), symEntry.getIndex()));
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
        // ASTUtils.setBooleanExpression(node.getExpression(), true);

        // LabelInstr beginLabel = program.addNewLabel();
        // node.getExpression().accept(this);
        // LabelInstr beginStmtLabel = program.addNewLabel();
        // Program.backpatch(ASTUtils.getTrueList(node.getExpression()), beginStmtLabel);

        // node.getStatement().accept(this);
        // Program.backpatch(ASTUtils.getNextList(node.getStatement()), beginLabel);
        // Program.backpatch(ASTUtils.getContinueList(node.getStatement()), beginLabel);
        
        // program.add(new GotoInstr(beginLabel));
        // ASTUtils.getNextList(node).addAll(ASTUtils.getFalseList(node.getExpression()));
        // ASTUtils.getNextList(node).addAll(ASTUtils.getBreakList(node.getStatement()));
        
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        // ASTUtils.setBooleanExpression(node.getExpression(), true);

        // node.getExpression().accept(this);
        // LabelInstr beginStmtLabel = program.addNewLabel();
        // Program.backpatch(ASTUtils.getTrueList(node.getExpression()), beginStmtLabel);
        // node.getStatement().accept(this);

        // ASTUtils.getNextList(node).addAll(ASTUtils.getFalseList(node.getExpression()));
        // ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getStatement()));

        // ASTUtils.setBreakList(node, ASTUtils.getBreakList(node.getStatement()));
        // ASTUtils.setContinueList(node, ASTUtils.getContinueList(node.getStatement()));
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        // ASTUtils.setBooleanExpression(node.getExpression(), true);

        // ASTUtils.setBooleanExpression(node.getExpression(), true);

        // node.getExpression().accept(this);
        // LabelInstr beginStmtLabel = program.addNewLabel();
        // Program.backpatch(ASTUtils.getTrueList(node.getExpression()), beginStmtLabel);
        // node.getStatement().accept(this);
        // GotoInstr gotoInstr = new GotoInstr();
        // program.add(gotoInstr);

        // LabelInstr beginElseLabel = program.addNewLabel();
        // Program.backpatch(ASTUtils.getFalseList(node.getExpression()), beginElseLabel);

        // ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getStatement()));
        // ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getElseStatement()));
        // ASTUtils.getNextList(node).add(gotoInstr);

        // ASTUtils.setBreakList(node, ASTUtils.getBreakList(node.getStatement()));
        // ASTUtils.setBreakList(node, ASTUtils.getBreakList(node.getElseStatement()));

        // ASTUtils.setContinueList(node, ASTUtils.getContinueList(node.getStatement()));
        // ASTUtils.setContinueList(node, ASTUtils.getContinueList(node.getElseStatement()));
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {

        // GotoInstr gotoInstr = new GotoInstr();
        // program.add(gotoInstr);
        // ASTUtils.getBreakList(node).add(gotoInstr);

    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {

        // GotoInstr gotoInstr = new GotoInstr();
        // program.add(gotoInstr);
        // ASTUtils.getContinueList(node).add(gotoInstr);

    }

    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        // List<GotoInstr> breakList = new ArrayList<GotoInstr>();
        // List<GotoInstr> continueList = new ArrayList<GotoInstr>();
        // Statement ps,s=null;
        // Iterator<Statement> it = node.getStatements().iterator();

        // while (it.hasNext()) {
        //     ps = s;
        //     s = it.next();

        //     backpatchNextList(ps);
        //     s.accept(this);
        //     breakList.addAll(ASTUtils.getBreakList(s));
        //     continueList.addAll(ASTUtils.getContinueList(s));
        // }
        // backpatchNextList(s);

        // ASTUtils.setBreakList(node, breakList);
        // ASTUtils.setContinueList(node, continueList);
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

        for (ParameterDeclaration p : node.getParameters()) {
            //p.accept(this);
            //String t = stack.pop();
            //String t1 = createTemp();
            //stack.push(t1);
        }

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
        if (s != null && !ASTUtils.getNextList(s).isEmpty()) {
            LabelNode labelNode = new LabelNode();
            mn.instructions.add(labelNode);
            backpatch(ASTUtils.getNextList(s), labelNode);
        }
    }

    private void InheritBooleanAttributes(ASTNode parent, Expression node) {
        if (parent instanceof Expression && ASTUtils.isBooleanExpression((Expression) parent)) {
            ASTUtils.setBooleanExpression(node, true);
        }

    }

    private void backpatch(List<JumpInsnNode> list, LabelNode labelNode) {
        if (list == null) {
            return;
        }
        for (JumpInsnNode instr : list) {
            instr.label = labelNode;
        }
    }

    /**
     * Cast the top of the stack to a particular type
     */
    private void widen(Type target, Type source) {
        if (source.equals(target)) {
            return;
        }

        if (source.equals(Type.BOOLEAN_TYPE)) {
            if (target.equals(Type.INT_TYPE)) {
                // nothing
            } else if (target.equals(Type.DOUBLE_TYPE)) {
                mn.instructions.add(new InsnNode(Opcodes.I2D));
            } else if (target.equals(TypeUtils.STRING_TYPE)) {
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Boolean", "toString", "(Z)Ljava/lang/String;"));
            }
        } else if (source.equals(Type.INT_TYPE)) {
            if (target.equals(Type.DOUBLE_TYPE)) {
                mn.instructions.add(new InsnNode(Opcodes.I2D));
            } else if (target.equals(TypeUtils.STRING_TYPE)) {
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;"));
            }
        } else if (source.equals(Type.DOUBLE_TYPE)) {
            if (target.equals(TypeUtils.STRING_TYPE)) {
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;"));
            }
        }
    }

    private void handleBooleanOperator(Expression node, Operator op, Type type) throws ASTVisitorException {
        List<JumpInsnNode> trueList = new ArrayList<JumpInsnNode>();

        if (type.equals(TypeUtils.STRING_TYPE)) {
            mn.instructions.add(new InsnNode(Opcodes.SWAP));
            JumpInsnNode jmp = null;
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));
            switch (op) {
                case EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IFNE, null);
                    break;
                case NOT_EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IFEQ, null);
                    break;
                default:
                    ASTUtils.error(node, "Operator not supported on strings");
                    break;
            }
            mn.instructions.add(jmp);
            trueList.add(jmp);
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            
            mn.instructions.add(new InsnNode(Opcodes.DCMPG));
            JumpInsnNode jmp = null;
                switch (op) {
                    case EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFEQ, null);
                        mn.instructions.add(jmp);
                        break;
                    case NOT_EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFNE, null);
                        mn.instructions.add(jmp);
                        break;
                    case GREATER:
                        jmp = new JumpInsnNode(Opcodes.IFGT, null);
                        mn.instructions.add(jmp);
                        break;
                    case GREATER_EQ:
                        jmp = new JumpInsnNode(Opcodes.IFGE, null);
                        mn.instructions.add(jmp);
                        break;
                    case LESS:
                        jmp = new JumpInsnNode(Opcodes.IFLT, null);
                        mn.instructions.add(jmp);
                        break;
                    case LESS_EQ:
                        jmp = new JumpInsnNode(Opcodes.IFLE, null);
                        mn.instructions.add(jmp);
                        break;
                    default:
                        ASTUtils.error(node, "Operator not supported");
                        break;
                }
                trueList.add(jmp);
            
        } else {
            JumpInsnNode jmp = null;
            switch (op) {
                case EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPEQ, null);
                    mn.instructions.add(jmp);
                    break;
                case NOT_EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPNE, null);
                    mn.instructions.add(jmp);
                    break;
                case GREATER:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPGT, null);
                    mn.instructions.add(jmp);
                    break;
                case GREATER_EQ:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPGE, null);
                    mn.instructions.add(jmp);
                    break;
                case LESS:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPLT, null);
                    mn.instructions.add(jmp);
                    break;
                case LESS_EQ:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPLE, null);
                    mn.instructions.add(jmp);
                    break;
                default:
                    ASTUtils.error(node, "Operator not supported");
                    break;
            }
            trueList.add(jmp);
        }
        ASTUtils.setTrueList(node, trueList);
        List<JumpInsnNode> falseList = new ArrayList<JumpInsnNode>();
        JumpInsnNode jmp = new JumpInsnNode(Opcodes.GOTO, null);
        mn.instructions.add(jmp);
        falseList.add(jmp);
        ASTUtils.setFalseList(node, falseList);
    }

    /**
     * Assumes top of stack contains two strings
     */
    private void handleStringOperator(ASTNode node, Operator op) throws ASTVisitorException {
        if (op.equals(Operator.PLUS)) {
            mn.instructions.add(new TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"));
            mn.instructions.add(new InsnNode(Opcodes.DUP));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V"));
            mn.instructions.add(new InsnNode(Opcodes.SWAP));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
            mn.instructions.add(new InsnNode(Opcodes.SWAP));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;"));
        } else if (op.isRelational()) {
            LabelNode trueLabelNode = new LabelNode();
            switch (op) {
                case EQUAL:
                    mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));
                    mn.instructions.add(new JumpInsnNode(Opcodes.IFNE, trueLabelNode));
                    break;
                case NOT_EQUAL:
                    mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));
                    mn.instructions.add(new JumpInsnNode(Opcodes.IFEQ, trueLabelNode));
                    break;
                default:
                    ASTUtils.error(node, "Operator not supported on strings");
                    break;
            }
            mn.instructions.add(new InsnNode(Opcodes.ICONST_0));
            LabelNode endLabelNode = new LabelNode();
            mn.instructions.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode));
            mn.instructions.add(trueLabelNode);
            mn.instructions.add(new InsnNode(Opcodes.ICONST_1));
            mn.instructions.add(endLabelNode);
        } else {
            ASTUtils.error(node, "Operator not recognized");
        }
    }

    private void handleNumberOperator(ASTNode node, Operator op, Type type) throws ASTVisitorException {
        if (op.equals(Operator.PLUS)) {
            
            mn.instructions.add(new InsnNode(type.getOpcode(Opcodes.IADD)));
        } else if (op.equals(Operator.MINUS)) {
            
            mn.instructions.add(new InsnNode(type.getOpcode(Opcodes.ISUB)));
        } else if (op.equals(Operator.MULTIPLY)) {
            
            mn.instructions.add(new InsnNode(type.getOpcode(Opcodes.IMUL)));
        } else if (op.equals(Operator.DIVISION)) {
            
            mn.instructions.add(new InsnNode(type.getOpcode(Opcodes.IDIV)));
        } else if (op.isRelational()) {
            
            if (type.equals(Type.DOUBLE_TYPE)) {
                mn.instructions.add(new InsnNode(Opcodes.DCMPG));
                JumpInsnNode jmp = null;
                switch (op) {
                    case EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFEQ, null);
                        mn.instructions.add(jmp);
                        break;
                    case NOT_EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFNE, null);
                        mn.instructions.add(jmp);
                        break;
                    case GREATER:
                        jmp = new JumpInsnNode(Opcodes.IFGT, null);
                        mn.instructions.add(jmp);
                        break;
                    case GREATER_EQ:
                        jmp = new JumpInsnNode(Opcodes.IFGE, null);
                        mn.instructions.add(jmp);
                        break;
                    case LESS:
                        jmp = new JumpInsnNode(Opcodes.IFLT, null);
                        mn.instructions.add(jmp);
                        break;
                    case LESS_EQ:
                        jmp = new JumpInsnNode(Opcodes.IFLE, null);
                        mn.instructions.add(jmp);
                        break;
                    default:
                        ASTUtils.error(node, "Operator not supported");
                        break;
                }
                mn.instructions.add(new InsnNode(Opcodes.ICONST_0));
                LabelNode endLabelNode = new LabelNode();
                mn.instructions.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode));
                LabelNode trueLabelNode = new LabelNode();
                jmp.label = trueLabelNode;
                mn.instructions.add(trueLabelNode);
                mn.instructions.add(new InsnNode(Opcodes.ICONST_1));
                mn.instructions.add(endLabelNode);
            } else if (type.equals(Type.INT_TYPE)) {
                LabelNode trueLabelNode = new LabelNode();
                switch (op) {
                    case EQUAL:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPEQ, trueLabelNode));
                        break;
                    case NOT_EQUAL:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPNE, trueLabelNode));
                        break;
                    case GREATER:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPGT, trueLabelNode));
                        break;
                    case GREATER_EQ:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPGE, trueLabelNode));
                        break;
                    case LESS:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPLT, trueLabelNode));
                        break;
                    case LESS_EQ:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPLE, trueLabelNode));
                        break;
                    default:
                        break;
                }
                mn.instructions.add(new InsnNode(Opcodes.ICONST_0));
                LabelNode endLabelNode = new LabelNode();
                mn.instructions.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode));
                mn.instructions.add(trueLabelNode);
                mn.instructions.add(new InsnNode(Opcodes.ICONST_1));
                mn.instructions.add(endLabelNode);
            } else {
                ASTUtils.error(node, "Cannot compare such types.");
            }
        } else {
            ASTUtils.error(node, "Operator not recognized.");
        }
    }

}
