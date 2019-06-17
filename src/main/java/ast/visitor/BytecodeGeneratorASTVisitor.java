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

import types.TypeUtils;
import ast.*;
import ast.definition.*;
import ast.expression.*;
import ast.statement.*;
import core.Operator;
import symbol.SymTable;
import symbol.SymTableEntry;

import org.apache.commons.lang3.StringEscapeUtils;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;


public class BytecodeGeneratorASTVisitor implements ASTVisitor {

    private ClassNode cn;
    private MethodNode mn;

    private final Deque<MethodNode> mnStack;

    public BytecodeGeneratorASTVisitor() {
        // create class
        cn = new ClassNode();
        cn.access = Opcodes.ACC_PUBLIC;
        cn.version = Opcodes.V1_5;
        cn.name = "Test";
        cn.sourceFile = "Test.in";
        cn.superName = "java/lang/Object";

        mnStack = new ArrayDeque<MethodNode>() ;

        // create constructor
        //kainourgio method node gia kathe sinartisi
        mn = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V"));
        mn.instructions.add(new InsnNode(Opcodes.RETURN));
        mn.maxLocals = 100;
        mn.maxStack = 100;
        
        cn.methods.add(mn);

    }
    
    public ClassNode getClassNode() {
        return cn;
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

        mn.maxLocals = ASTUtils.getSafeLocalIndexPool(node).getMaxLocals() + 100;

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
        
        mnStack.element().instructions.add(new VarInsnNode(symEntry.getType().getOpcode(Opcodes.ISTORE), symEntry.getIndex()));

    }

    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        Type expr1Type = ASTUtils.getSafeType(node.getExpression1());
        Type expr2Type = ASTUtils.getSafeType(node.getExpression2());
        Type maxType = TypeUtils.maxType(expr1Type, expr2Type);

        InheritBooleanAttributes(node, node.getExpression1());
        node.getExpression1().accept(this);
        widen(maxType,expr1Type);

        InheritBooleanAttributes(node, node.getExpression2());
        node.getExpression2().accept(this);
        widen(maxType,expr2Type);

        if (ASTUtils.isBooleanExpression(node)) {
            handleBooleanOperator(node, node.getOperator(), maxType);
        } else if (maxType.equals(TypeUtils.STRING_TYPE)) {
            mn.instructions.add(new InsnNode(Opcodes.SWAP));
            handleStringOperator(node, node.getOperator());
        } else {
            handleNumberOperator(node, node.getOperator(), maxType);
        }
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        InheritBooleanAttributes(node, node.getExpression());
        node.getExpression().accept(this);

        handleUnaryOperator(node,node.getOperator());

    }

    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        SymTableEntry symEntry = ASTUtils.getSafeSymbolTable(node).lookup(node.getIdentifier());
        mnStack.element().instructions.add(new VarInsnNode((symEntry.getType().getOpcode(Opcodes.ILOAD)), symEntry.getIndex()));
    }

    @Override
    public void visit(FloatLiteralExpression node) throws ASTVisitorException {

        Float d = node.getLiteral();
        mnStack.element().instructions.add(new LdcInsnNode(d));

    }

    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {

        int d = node.getLiteral();
        mnStack.element().instructions.add(new LdcInsnNode(d));
    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {

        String str = node.getLiteral();
        mnStack.element().instructions.add(new LdcInsnNode(str));

    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
        ASTUtils.setTrueList(node, ASTUtils.getTrueList(node.getExpression()));
        ASTUtils.setFalseList(node, ASTUtils.getFalseList(node.getExpression()));
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        LabelNode beginLabelNode = new LabelNode();
        mnStack.element().instructions.add(beginLabelNode);

        node.getExpression().accept(this);

        LabelNode trueLabelNode = new LabelNode();
        mnStack.element().instructions.add(trueLabelNode);
        backpatch(ASTUtils.getTrueList(node.getExpression()), trueLabelNode);

        node.getStatement().accept(this);

        backpatch(ASTUtils.getNextList(node.getStatement()), beginLabelNode);
        backpatch(ASTUtils.getContinueList(node.getStatement()), beginLabelNode);

        mnStack.element().instructions.add(new JumpInsnNode(Opcodes.GOTO, beginLabelNode));

        ASTUtils.getNextList(node).addAll(ASTUtils.getFalseList(node.getExpression()));
        ASTUtils.getNextList(node).addAll(ASTUtils.getBreakList(node.getStatement()));
        
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        node.getExpression().accept(this);

        LabelNode labelNode = new LabelNode();
        mnStack.element().instructions.add(labelNode);
        backpatch(ASTUtils.getTrueList(node.getExpression()), labelNode);

        node.getStatement().accept(this);

        ASTUtils.getBreakList(node).addAll(ASTUtils.getBreakList(node.getStatement()));
        ASTUtils.getContinueList(node).addAll(ASTUtils.getContinueList(node.getStatement()));

        ASTUtils.getNextList(node).addAll(ASTUtils.getFalseList(node.getExpression()));
        ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getStatement()));
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        node.getExpression().accept(this);

        LabelNode stmt1StartLabelNode = new LabelNode();
        mnStack.element().instructions.add(stmt1StartLabelNode);
        node.getElseStatement().accept(this);

        JumpInsnNode skipGoto = new JumpInsnNode(Opcodes.GOTO, null);
        mnStack.element().instructions.add(skipGoto);

        LabelNode stmt2StartLabelNode = new LabelNode();
        mnStack.element().instructions.add(stmt2StartLabelNode);
        node.getElseStatement().accept(this);

        backpatch(ASTUtils.getTrueList(node.getExpression()), stmt1StartLabelNode);
        backpatch(ASTUtils.getFalseList(node.getExpression()), stmt2StartLabelNode);

        ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getElseStatement()));
        ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getElseStatement()));
        ASTUtils.getNextList(node).add(skipGoto);

        ASTUtils.getBreakList(node).addAll(ASTUtils.getBreakList(node.getElseStatement()));
        ASTUtils.getBreakList(node).addAll(ASTUtils.getBreakList(node.getElseStatement()));

        ASTUtils.getContinueList(node).addAll(ASTUtils.getContinueList(node.getElseStatement()));
        ASTUtils.getContinueList(node).addAll(ASTUtils.getContinueList(node.getElseStatement()));
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
        SymTableEntry entry = ASTUtils.getSafeSymbolTable(node).lookup(node.getName());
        MethodNode fmn = new MethodNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, node.getName(),entry.getType().getDescriptor(), null, null);
        mnStack.push(fmn);

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
        mnStack.pop();
        fmn.maxLocals = ASTUtils.getSafeLocalIndexPool(node).getMaxLocals() + 1;
        

        // IMPORTANT: this should be dynamically calculated
        // use COMPUTE_MAXS when computing the ClassWriter,
        // e.g. new ClassWriter(ClassWriter.COMPUTE_MAXS)
        //fmn.maxStack = 32;

        if(ASTUtils.getSafeType(node).equals(Type.VOID_TYPE)){
            fmn.instructions.add(new InsnNode(Opcodes.RETURN));
        }
        
        cn.methods.add(fmn);
    }

    @Override
    public void visit(StructDefinition node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(ArrayAccessExpression node) throws ASTVisitorException {
        
        // String t = createTemp();
        // Type type = ASTUtils.getType(node);
        // String type_size = String.valueOf(type.getSize()*4);
        // node.getIndex().accept(this);
        // String i = stack.pop();
        // String arr = Registry.getInstance().getDefinedArrays().get(node.getIdentifier());
        // arr = arr + "." + t;
        
        // program.add(new BinaryOpInstr(Operator.MULTIPLY,type_size,i,t));
        // stack.push(arr);
    }

    @Override
    public void visit(BooleanLiteralExpression node) throws ASTVisitorException {
        
    }

    @Override
    public void visit(CharLiteralExpression node) throws ASTVisitorException {

        char d = node.getExpression();
        mnStack.element().instructions.add(new LdcInsnNode(d));
        // String t = createTemp();
        // stack.push(t);
        // program.add(new AssignInstr(t,"\'" + node.getExpression() + "\'"));
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {

        //TO DO: FIX FOR COMPLEX FUNCTIONS
        List<String> params = new ArrayList<String>();
         
        if(!node.getIdentifier().equals("print")){
            for (Expression e : node.getExpressions()) {
                e.accept(this);
            }
            
            SymTableEntry entry = ASTUtils.getSafeSymbolTable(node).lookup(node.getIdentifier());
            mnStack.element().instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"Test", node.getIdentifier(), entry.getType().getDescriptor()));
        }else {
            //In the print function we know that there is only 1 parameter.
            Expression expr = node.getExpressions().get(0);
            
            
            mnStack.element().instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
            Type pType = ASTUtils.getSafeType(expr) ;
            expr.accept(this);
            mnStack.element().instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "("+pType.getDescriptor()+")V"));
    
        }
        
    }

    @Override
    public void visit(StructArrayAccessExpression node) throws ASTVisitorException {
        // node.getStruct().accept(this);
       
        // String struct_var;
        // if(node.getStruct() instanceof IdentifierExpression){
        //     IdentifierExpression  expr = (IdentifierExpression)node.getStruct();
        //     struct_var = Registry.getInstance().getDefinedStructs().get(expr.getIdentifier());
        // }else{
        //     struct_var = stack.pop();
        // }
        // struct_var = struct_var+"."+node.getIdentifier();

        // Type type = ASTUtils.getType(node);
        // String type_size = String.valueOf(type.getSize()*4);

        

        // node.getIndex().accept(this);
        // String index = stack.pop();
        // String address = createTemp();
        // program.add(new BinaryOpInstr(Operator.MULTIPLY,type_size,index,address));

        // String t1 = createTemp();
        // program.add(new AssignInstr(t1, struct_var));

        // String arr = Registry.getInstance().getDefinedArrays().get(node.getIdentifier());
        // arr = t1 + "." + address; 

        // stack.push(arr); 
    }

    @Override
    public void visit(StructVariableAccessExpression node) throws ASTVisitorException {
        // node.getStruct().accept(this);
       
        // IdentifierExpression  expr = (IdentifierExpression)node.getStruct();
        // String struct_var = Registry.getInstance().getDefinedStructs().get(expr.getIdentifier());
        // struct_var = struct_var+"."+node.getIdentifier();
        // //We know from the previous visitor that the struct is valid.

        // stack.push(struct_var);

    }

    @Override
    public void visit(EmptyStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        
        if (node.getExpression() != null) {
            node.getExpression().accept(this);

            Type exprType = ASTUtils.getSafeType(node.getExpression());

            SymTable<SymTableEntry> st = ASTUtils.getSafeSymbolTable(node);
            Type returnType = st.lookup(ASTUtils.getCurrentFunctionName(node)).getType().getReturnType();
            widen(returnType, exprType);

            mnStack.element().instructions.add(new InsnNode(returnType.getOpcode(Opcodes.IRETURN)));
        }else{
            mnStack.element().instructions.add(new InsnNode(Opcodes.RETURN));
        }
        
    }

    @Override
    public void visit(VariableDefinition node) throws ASTVisitorException {
              
    }

    private void backpatchNextList(Statement s) {
        if (s != null && !ASTUtils.getNextList(s).isEmpty()) {
            LabelNode labelNode = new LabelNode();
            mnStack.element().instructions.add(labelNode);
            backpatch(ASTUtils.getNextList(s), labelNode);
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

        if (source.equals(Type.INT_TYPE)) {
            if (target.equals(Type.FLOAT_TYPE)) {
                mnStack.element().instructions.add(new InsnNode(Opcodes.I2F));
            } else if (target.equals(TypeUtils.STRING_TYPE)) {
                mnStack.element().instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;"));
            }
        } else if (source.equals(Type.FLOAT_TYPE)) {
            if (target.equals(TypeUtils.STRING_TYPE)) {
                mnStack.element().instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;"));
            }
        }
    }

    private void handleNumberOperator(Expression node, Operator op, Type type) throws ASTVisitorException {
        List<JumpInsnNode> trueList = new ArrayList<JumpInsnNode>();
        if (op.equals(Operator.PLUS)) {

            mnStack.element().instructions.add(new InsnNode(type.getOpcode(Opcodes.IADD)));
        } else if (op.equals(Operator.MINUS)) {

            mnStack.element().instructions.add(new InsnNode(type.getOpcode(Opcodes.ISUB)));
        } else if (op.equals(Operator.MULTIPLY)) {

            mnStack.element().instructions.add(new InsnNode(type.getOpcode(Opcodes.IMUL)));
        } else if (op.equals(Operator.DIVISION)) {

            mnStack.element().instructions.add(new InsnNode(type.getOpcode(Opcodes.IDIV)));
        } else if (op.isRelational()) {

            if (type.equals(Type.FLOAT_TYPE)) {
                mnStack.element().instructions.add(new InsnNode(Opcodes.FCMPG));
                JumpInsnNode jmp = null;
                switch (op) {
                    case EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFEQ, null);
                        mnStack.element().instructions.add(jmp);
                        break;
                    case NOT_EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFNE, null);
                        mnStack.element().instructions.add(jmp);
                        break;
                    case GREATER:
                        jmp = new JumpInsnNode(Opcodes.IFGT, null);
                        mnStack.element().instructions.add(jmp);
                        break;
                    case GREATER_EQ:
                        jmp = new JumpInsnNode(Opcodes.IFGE, null);
                        mnStack.element().instructions.add(jmp);
                        break;
                    case LESS:
                        jmp = new JumpInsnNode(Opcodes.IFLT, null);
                        mnStack.element().instructions.add(jmp);
                        break;
                    case LESS_EQ:
                        jmp = new JumpInsnNode(Opcodes.IFLE, null);
                        mnStack.element().instructions.add(jmp);
                        break;
                    default:
                        ASTUtils.error(node, "Operator not supported");
                        break;
                }
                trueList.add(jmp);
            } else if (type.equals(Type.INT_TYPE)) {
                JumpInsnNode jmp = null;
                switch (op) {
                    case EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IF_ICMPEQ, null);
                        mnStack.element().instructions.add(jmp);
                        break;
                    case NOT_EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IF_ICMPNE, null);
                        mnStack.element().instructions.add(jmp);
                        break;
                    case GREATER:
                        jmp = new JumpInsnNode(Opcodes.IF_ICMPGT, null);
                        mnStack.element().instructions.add(jmp);
                        break;
                    case GREATER_EQ:
                        jmp = new JumpInsnNode(Opcodes.IF_ICMPGE, null);
                        mnStack.element().instructions.add(jmp);
                        break;
                    case LESS:
                        jmp = new JumpInsnNode(Opcodes.IF_ICMPLT, null);
                        mnStack.element().instructions.add(jmp);
                        break;
                    case LESS_EQ:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPLT, null);
                    mnStack.element().instructions.add(jmp);
                    break;
                default:
                    break;
            }
            trueList.add(jmp);
        }

    }
        ASTUtils.setTrueList(node, trueList);
        List<JumpInsnNode> falseList = new ArrayList<JumpInsnNode>();
        JumpInsnNode jmp = new JumpInsnNode(Opcodes.GOTO, null);

        falseList.add(jmp);
        ASTUtils.setFalseList(node, falseList);
    }

    private void handleBooleanOperator(Expression node, Operator op, Type type) throws ASTVisitorException {
        List<JumpInsnNode> trueList = new ArrayList<JumpInsnNode>();

        if (type.equals(TypeUtils.STRING_TYPE)) {
            mnStack.element().instructions.add(new InsnNode(Opcodes.SWAP));
            JumpInsnNode jmp = null;
            mnStack.element().instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));
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
            mnStack.element().instructions.add(jmp);
            trueList.add(jmp);
        } else if (type.equals(Type.FLOAT_TYPE)) {
            mnStack.element().instructions.add(new InsnNode(Opcodes.FCMPG));
            JumpInsnNode jmp = null;
                switch (op) {
                    case EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFEQ, null);
                        mnStack.element().instructions.add(jmp);
                        break;
                    case NOT_EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFNE, null);
                        mnStack.element().instructions.add(jmp);
                        break;
                    case GREATER:
                        jmp = new JumpInsnNode(Opcodes.IFGT, null);
                        mnStack.element().instructions.add(jmp);
                        break;
                    case GREATER_EQ:
                        jmp = new JumpInsnNode(Opcodes.IFGE, null);
                        mnStack.element().instructions.add(jmp);
                        break;
                    case LESS:
                        jmp = new JumpInsnNode(Opcodes.IFLT, null);
                        mnStack.element().instructions.add(jmp);
                        break;
                    case LESS_EQ:
                        jmp = new JumpInsnNode(Opcodes.IFLE, null);
                        mnStack.element().instructions.add(jmp);
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
                    mnStack.element().instructions.add(jmp);
                    break;
                case NOT_EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPNE, null);
                    mnStack.element().instructions.add(jmp);
                    break;
                case GREATER:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPGT, null);
                    mnStack.element().instructions.add(jmp);
                    break;
                case GREATER_EQ:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPGE, null);
                    mnStack.element().instructions.add(jmp);
                    break;
                case LESS:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPLT, null);
                    mnStack.element().instructions.add(jmp);
                    break;
                case LESS_EQ:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPLE, null);
                    mnStack.element().instructions.add(jmp);
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
        mnStack.element().instructions.add(jmp);
        falseList.add(jmp);
        ASTUtils.setFalseList(node, falseList);
    }

    private void handleUnaryOperator(UnaryExpression node, Operator op){
        if (op.equals(Operator.NOT)) {
            ASTUtils.setFalseList(node, ASTUtils.getTrueList(node.getExpression()));
            ASTUtils.setTrueList(node, ASTUtils.getFalseList(node.getExpression()));
        }
    }

    /**
     * Assumes top of stack contains two strings
     */
    private void handleStringOperator(ASTNode node, Operator op) throws ASTVisitorException {
        if (op.equals(Operator.PLUS)) {
            mnStack.element().instructions.add(new TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"));
            mnStack.element().instructions.add(new InsnNode(Opcodes.DUP));
            mnStack.element().instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V"));
            mnStack.element().instructions.add(new InsnNode(Opcodes.SWAP));
            mnStack.element().instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
            mnStack.element().instructions.add(new InsnNode(Opcodes.SWAP));
            mnStack.element().instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
            mnStack.element().instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;"));
        } else if (op.isRelational()) {
            LabelNode trueLabelNode = new LabelNode();
            switch (op) {
                case EQUAL:
                    mnStack.element().instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));
                    mnStack.element().instructions.add(new JumpInsnNode(Opcodes.IFNE, trueLabelNode));
                    break;
                case NOT_EQUAL:
                    mnStack.element().instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));
                    mnStack.element().instructions.add(new JumpInsnNode(Opcodes.IFEQ, trueLabelNode));
                    break;
                default:
                    ASTUtils.error(node, "Operator not supported on strings");
                    break;
            }
            mnStack.element().instructions.add(new InsnNode(Opcodes.ICONST_0));
            LabelNode endLabelNode = new LabelNode();
            mnStack.element().instructions.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode));
            mnStack.element().instructions.add(trueLabelNode);
            mnStack.element().instructions.add(new InsnNode(Opcodes.ICONST_1));
            mnStack.element().instructions.add(endLabelNode);
        } else {
            ASTUtils.error(node, "Operator not recognized");
        }
    }

    // private void handleNumberOperator(ASTNode node, Operator op, Type type) throws ASTVisitorException {
    //     //TO DO: widen int to float
    //     if (op.equals(Operator.PLUS)) {
            
    //         mnStack.element().instructions.add(new InsnNode(type.getOpcode(Opcodes.IADD)));
    //     } else if (op.equals(Operator.MINUS)) {
            
    //         mnStack.element().instructions.add(new InsnNode(type.getOpcode(Opcodes.ISUB)));
    //     } else if (op.equals(Operator.MULTIPLY)) {
            
    //         mnStack.element().instructions.add(new InsnNode(type.getOpcode(Opcodes.IMUL)));
    //     } else if (op.equals(Operator.DIVISION)) {
            
    //         mnStack.element().instructions.add(new InsnNode(type.getOpcode(Opcodes.IDIV)));
    //     } else if (op.isRelational()) {
            
    //         if (type.equals(Type.FLOAT_TYPE)) {
    //             mnStack.element().instructions.add(new InsnNode(Opcodes.FCMPG));
    //             JumpInsnNode jmp = null;
    //             switch (op) {
    //                 case EQUAL:
    //                     jmp = new JumpInsnNode(Opcodes.IFEQ, null);
    //                     mnStack.element().instructions.add(jmp);
    //                     break;
    //                 case NOT_EQUAL:
    //                     jmp = new JumpInsnNode(Opcodes.IFNE, null);
    //                     mnStack.element().instructions.add(jmp);
    //                     break;
    //                 case GREATER:
    //                     jmp = new JumpInsnNode(Opcodes.IFGT, null);
    //                     mnStack.element().instructions.add(jmp);
    //                     break;
    //                 case GREATER_EQ:
    //                     jmp = new JumpInsnNode(Opcodes.IFGE, null);
    //                     mnStack.element().instructions.add(jmp);
    //                     break;
    //                 case LESS:
    //                     jmp = new JumpInsnNode(Opcodes.IFLT, null);
    //                     mnStack.element().instructions.add(jmp);
    //                     break;
    //                 case LESS_EQ:
    //                     jmp = new JumpInsnNode(Opcodes.IFLE, null);
    //                     mnStack.element().instructions.add(jmp);
    //                     break;
    //                 default:
    //                     ASTUtils.error(node, "Operator not supported");
    //                     break;
    //             }
    //             mnStack.element().instructions.add(new InsnNode(Opcodes.ICONST_0));
    //             LabelNode endLabelNode = new LabelNode();
    //             mnStack.element().instructions.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode));
    //             LabelNode trueLabelNode = new LabelNode();
    //             jmp.label = trueLabelNode;
    //             mnStack.element().instructions.add(trueLabelNode);
    //             mnStack.element().instructions.add(new InsnNode(Opcodes.ICONST_1));
    //             mnStack.element().instructions.add(endLabelNode);
    //         } else if (type.equals(Type.INT_TYPE)) {
    //             LabelNode trueLabelNode = new LabelNode();
    //             switch (op) {
    //                 case EQUAL:
    //                     mnStack.element().instructions.add(new JumpInsnNode(Opcodes.IF_ICMPEQ, trueLabelNode));
    //                     break;
    //                 case NOT_EQUAL:
    //                     mnStack.element().instructions.add(new JumpInsnNode(Opcodes.IF_ICMPNE, trueLabelNode));
    //                     break;
    //                 case GREATER:
    //                     mnStack.element().instructions.add(new JumpInsnNode(Opcodes.IF_ICMPGT, trueLabelNode));
    //                     break;
    //                 case GREATER_EQ:
    //                     mnStack.element().instructions.add(new JumpInsnNode(Opcodes.IF_ICMPGE, trueLabelNode));
    //                     break;
    //                 case LESS:
    //                     mnStack.element().instructions.add(new JumpInsnNode(Opcodes.IF_ICMPLT, trueLabelNode));
    //                     break;
    //                 case LESS_EQ:
    //                     mnStack.element().instructions.add(new JumpInsnNode(Opcodes.IF_ICMPLE, trueLabelNode));
    //                     break;
    //                 default:
    //                     break;
    //             }
    //             mnStack.element().instructions.add(new InsnNode(Opcodes.ICONST_0));
    //             LabelNode endLabelNode = new LabelNode();
    //             mnStack.element().instructions.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode));
    //             mnStack.element().instructions.add(trueLabelNode);
    //             mnStack.element().instructions.add(new InsnNode(Opcodes.ICONST_1));
    //             mnStack.element().instructions.add(endLabelNode);
    //         } else {
    //             ASTUtils.error(node, "Cannot compare such types.");
    //         }
    //     } else {
    //         ASTUtils.error(node, "Operator not recognized.");
    //     }
    // }

    private void InheritBooleanAttributes(ASTNode parent, Expression node) {
        if (parent instanceof Expression && ASTUtils.isBooleanExpression((Expression) parent)) {
            ASTUtils.setBooleanExpression(node, true);
        }

    }

}
