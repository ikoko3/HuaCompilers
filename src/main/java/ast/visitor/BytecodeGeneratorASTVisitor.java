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
import core.ByteCodeUtils;
import core.Operator;
import symbol.SymTable;
import symbol.SymTableEntry;

import org.apache.commons.lang3.StringEscapeUtils;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;


public class BytecodeGeneratorASTVisitor implements ASTVisitor {

    private MethodNode initMn;
    private ClassNode cn;
    

    private final Deque<MethodNode> mnStack;
    private final Deque<ClassNode> cnStack;

    public BytecodeGeneratorASTVisitor() {
        // create class
        cn = new ClassNode();
        cn.access = Opcodes.ACC_PUBLIC;
        cn.version = Opcodes.V1_5;
        cn.name = "Test";
        cn.sourceFile = "Test.in";
        cn.superName = "java/lang/Object";

        mnStack = new ArrayDeque<MethodNode>();
        cnStack = new ArrayDeque<ClassNode>();
        

        // create constructor
        //kainourgio method node gia kathe sinartisi
        initMn = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        initMn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        initMn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V"));
        initMn.instructions.add(new InsnNode(Opcodes.RETURN));
        initMn.maxLocals = 100;
        initMn.maxStack = 100;
        
        cn.methods.add(initMn);

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

        initMn.maxLocals = ASTUtils.getSafeLocalIndexPool(node).getMaxLocals() + 100;

    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        // node.getResult().accept(this);
        // Type exprType = ASTUtils.getSafeType(node.getResult());

        // Expression target = node.getTarget();
        // SymTableEntry symEntry = null;

        // if(target instanceof IdentifierExpression){

            
        //     IdentifierExpression t =  (IdentifierExpression) target;
        //     symEntry = ASTUtils.getSafeSymbolTable(node).lookup(t.getIdentifier());
        // }else if(target instanceof ArrayAccessExpression){
        //     ArrayAccessExpression t = (ArrayAccessExpression) target;
        //     symEntry = null;
        // }else if(target instanceof StructVariableAccessExpression){
        //     StructVariableAccessExpression t = (StructVariableAccessExpression) target;
        //     symEntry = null;
        // }else if(target instanceof StructArrayAccessExpression){
        //     StructArrayAccessExpression t = (StructArrayAccessExpression) target;
        //     symEntry = null;
        // }else{
        //     ASTUtils.error(node, "Assignment is not implemented for the type "+target.getClass());
        // }

        // if(symEntry == null)
        //     ASTUtils.error(node, "Assignment is not implemented for the type "+target.getClass());
            
        //     ByteCodeUtils.widen(symEntry.getType(),exprType,mnStack.element());
        
        //     mnStack.element().instructions.add(new VarInsnNode(symEntry.getType().getOpcode(Opcodes.ISTORE), symEntry.getIndex()));

        AssignmentASTVisitor visitor = new AssignmentASTVisitor(this,node.getResult(),mnStack.element());
        node.getTarget().accept(visitor);
    }

    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        Type expr1Type = ASTUtils.getSafeType(node.getExpression1());
        Type expr2Type = ASTUtils.getSafeType(node.getExpression2());
        Type maxType = TypeUtils.maxType(expr1Type, expr2Type);

        InheritBooleanAttributes(node, node.getExpression1());
        node.getExpression1().accept(this);
        ByteCodeUtils.widen(maxType,expr1Type,mnStack.element());

        LabelNode intrmLbl = null;
        if (node.getOperator().isLogical()) {
            intrmLbl = new LabelNode();
            mnStack.element().instructions.add(intrmLbl);
        }

        InheritBooleanAttributes(node, node.getExpression2());
        node.getExpression2().accept(this);
        ByteCodeUtils.widen(maxType,expr2Type,mnStack.element());

        if (ASTUtils.isBooleanExpression(node)) {
            
            if(node.getOperator().isLogical()){
                switch (node.getOperator()) {
                    case AND:
                        ASTUtils.getFalseList(node).addAll(ASTUtils.getFalseList(node.getExpression1()));
                        backpatch(ASTUtils.getTrueList(node.getExpression1()), intrmLbl);
    
                        ASTUtils.getFalseList(node).addAll(ASTUtils.getFalseList(node.getExpression2()));
                        ASTUtils.getTrueList(node).addAll(ASTUtils.getTrueList(node.getExpression2()));
                        break;
                    case OR:
                        ASTUtils.getTrueList(node).addAll(ASTUtils.getTrueList(node.getExpression1()));
                        backpatch(ASTUtils.getFalseList(node.getExpression1()), intrmLbl);
    
                        ASTUtils.getFalseList(node).addAll(ASTUtils.getFalseList(node.getExpression2()));
                        ASTUtils.getTrueList(node).addAll(ASTUtils.getTrueList(node.getExpression2()));
                        break;
                    default:
                        break;
                }
                JumpInsnNode fl = new JumpInsnNode(Opcodes.GOTO,null);
                        ASTUtils.getFalseList(node).add(fl);
                        mnStack.element().instructions.add(fl);
            }else{
                ByteCodeUtils.handleBooleanOperator(node, node.getOperator(), maxType,mnStack.element());
            }
            
        } else if (maxType.equals(TypeUtils.STRING_TYPE)) {
            mnStack.element().instructions.add(new InsnNode(Opcodes.SWAP));
            ByteCodeUtils.handleStringOperator(node, node.getOperator(),mnStack.element());
        } else {
            ByteCodeUtils.handleNumberOperator(node, node.getOperator(), maxType,mnStack.element());
        }
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        InheritBooleanAttributes(node, node.getExpression());
        node.getExpression().accept(this);
        Type exprType = ASTUtils.getSafeType(node.getExpression());

        ByteCodeUtils.handleUnaryOperator(node,node.getOperator(),exprType,mnStack.element());

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

        inheritWhileLists(node,node.getStatement());
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        node.getExpression().accept(this);

        LabelNode stmt1StartLabelNode = new LabelNode();
        mnStack.element().instructions.add(stmt1StartLabelNode);
        node.getStatement().accept(this);

        JumpInsnNode skipGoto = new JumpInsnNode(Opcodes.GOTO, null);
        mnStack.element().instructions.add(skipGoto);

        LabelNode stmt2StartLabelNode = new LabelNode();
        mnStack.element().instructions.add(stmt2StartLabelNode);
        node.getElseStatement().accept(this);

        backpatch(ASTUtils.getTrueList(node.getExpression()), stmt1StartLabelNode);
        backpatch(ASTUtils.getFalseList(node.getExpression()), stmt2StartLabelNode);

        ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getStatement()));
        ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getElseStatement()));
        ASTUtils.getNextList(node).add(skipGoto);

        inheritWhileLists(node,node.getStatement());
        inheritWhileLists(node,node.getElseStatement());
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        JumpInsnNode jmp = new JumpInsnNode(Opcodes.GOTO, null);
        mnStack.element().instructions.add(jmp);
        ASTUtils.getBreakList(node).add(jmp);

    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        JumpInsnNode jmp = new JumpInsnNode(Opcodes.GOTO, null);
        mnStack.element().instructions.add(jmp);
        ASTUtils.getContinueList(node).add(jmp);

    }

    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        Statement ps,s=null;
        Iterator<Statement> it = node.getStatements().iterator();

        while (it.hasNext()) {
            ps = s;
            s = it.next();

            backpatchNextList(ps);
            s.accept(this);
            
            inheritWhileLists(node, s);
        }
        backpatchNextList(s);

    }

    @Override
    public void visit(Array node) throws ASTVisitorException {
        mnStack.element().instructions.add(new LdcInsnNode(node.getLength()));
        SymTableEntry entry = ASTUtils.getSafeSymbolTable(node).lookup(node.getName());
        Type elementType = entry.getType().getElementType();

        mnStack.element().instructions.add(new TypeInsnNode(Opcodes.ANEWARRAY,Type.getType(Integer.class).getInternalName()));
        

        mnStack.element().instructions.add(new VarInsnNode(Opcodes.ASTORE, entry.getIndex()));
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
        ClassNode structClass = new ClassNode();
        structClass.access = Opcodes.ACC_PUBLIC;
        structClass.version = Opcodes.V1_5;
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
            SymTableEntry entry = ASTUtils.getSafeSymbolTable(node).lookup(node.getIdentifier());
            int i=0;

            for (Expression e : node.getExpressions()) {
                Type target = entry.getType().getArgumentTypes()[i];
                e.accept(this);
                ByteCodeUtils.widen(target, ASTUtils.getSafeType(e),mnStack.element());
                i++;
            }
            
            
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
            ByteCodeUtils.widen(returnType, exprType,mnStack.element());

            mnStack.element().instructions.add(new InsnNode(returnType.getOpcode(Opcodes.IRETURN)));
        }else{
            mnStack.element().instructions.add(new InsnNode(Opcodes.RETURN));
        }
        
    }

    @Override
    public void visit(VariableDefinition node) throws ASTVisitorException {
        node.getVariable().accept(this);
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
   

    private void InheritBooleanAttributes(ASTNode parent, Expression node) {
        if (parent instanceof Expression && ASTUtils.isBooleanExpression((Expression) parent)) {
            ASTUtils.setBooleanExpression(node, true);
        }
    }

    private void inheritWhileLists(Statement parent, Statement child){
        ASTUtils.getBreakList(parent).addAll(ASTUtils.getBreakList(child));
        ASTUtils.getContinueList(parent).addAll(ASTUtils.getContinueList(child));
    }

}
