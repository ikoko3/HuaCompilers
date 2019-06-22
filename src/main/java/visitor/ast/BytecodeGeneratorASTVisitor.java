package visitor.ast;

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
import core.*;
import symbol.SymTable;
import symbol.SymTableEntry;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import visitor.expression.AssignmentASTVisitor;


public class BytecodeGeneratorASTVisitor implements ASTVisitor {

    private MethodNode initMn;
    private ClassNode cn;
    

    private final Deque<MethodNode> mnStack;
    private final List<ClassNode> structsList;

    @SuppressWarnings("unchecked")
    public BytecodeGeneratorASTVisitor() {
        // create class
        cn = new ClassNode();
        cn.access = Opcodes.ACC_PUBLIC;
        cn.version = Opcodes.V1_5;
        cn.name = Environment.PROGRAM_NAME;
        cn.sourceFile = Environment.PROGRAM_NAME + Environment.IN_FILE_EXTENSION;
        cn.superName = "java/lang/Object";

        mnStack = new ArrayDeque<MethodNode>();
        structsList = new ArrayList<ClassNode>();
        

        // create constructor
        //kainourgio method node gia kathe sinartisi
        initMn = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        initMn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        initMn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V"));
        initMn.instructions.add(new InsnNode(Opcodes.RETURN));

        cn.methods.add(initMn);
    }
    
    public List<ClassNode> getStructsList(){
        return structsList;
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

        }
        backpatchNextList(d);

        initMn.maxLocals = ASTUtils.getSafeLocalIndexPool(node).getMaxLocals() + 100;
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {

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

        mnStack.element().instructions.add(new VarInsnNode(Opcodes.NEWARRAY, getT_Type(elementType)));
        
        mnStack.element().instructions.add(new VarInsnNode(Opcodes.ASTORE, entry.getIndex()));
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        node.getVariable().accept(this);       
    }

    @Override
    public void visit(Variable node) throws ASTVisitorException {
        SymTableEntry entry = ASTUtils.getSafeSymbolTable(node).lookup(node.getName());
        if(TypeUtils.isStructType(entry.getType())){

            String classDescr = entry.getType().getInternalName();

            
            mnStack.element().instructions.add(new TypeInsnNode(Opcodes.NEW, classDescr));
            mnStack.element().instructions.add(new InsnNode(Opcodes.DUP));
            mnStack.element().instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, classDescr, "<init>", "()V"));

            mnStack.element().instructions.add(new VarInsnNode(Opcodes.ASTORE, entry.getIndex()));
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        SymTableEntry entry = ASTUtils.getSafeSymbolTable(node).lookup(node.getName());
        MethodNode fmn = new MethodNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, node.getName(),entry.getType().getDescriptor(), null, null);
        mnStack.push(fmn);

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

        if(ASTUtils.getSafeType(node).equals(Type.VOID_TYPE)){
            fmn.instructions.add(new InsnNode(Opcodes.RETURN));
        }
        
        cn.methods.add(fmn);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visit(StructDefinition node) throws ASTVisitorException {
        ClassNode structClass = new ClassNode();
        structClass.access = Opcodes.ACC_PUBLIC;
        structClass.version = Opcodes.V1_5;
        structClass.name = node.getName();
        structClass.sourceFile = node.getName() + Environment.IN_FILE_EXTENSION;
        structClass.superName = "java/lang/Object";

        MethodNode ctor = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        ctor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        ctor.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V"));
        

        for (VariableDefinition vardef : node.getVariables()) {
            Variable sVar = vardef.getVariable();
            SymTableEntry entry = ASTUtils.getSafeSymbolTable(sVar).lookup(sVar.getName());

            FieldNode fd = new FieldNode(Opcodes.ACC_PUBLIC,sVar.getName(),entry.getType().getDescriptor(),null,null);
            structClass.fields.add(fd);
            
            if(sVar instanceof Array){
                mnStack.push(ctor);
                sVar.accept(this);
                mnStack.pop();

                //DEBUG
                // ctor.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
                // ctor.instructions.add(new InsnNode(Opcodes.DUP));
                // ctor.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/util/Array", "toString", "()java/lang/String"));
                // ctor.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(java/lang/String)V"));
            }
            
            
        }
        ctor.instructions.add(new InsnNode(Opcodes.RETURN));
        ctor.maxLocals = 100;
        ctor.maxStack = 100;

        structClass.methods.add(ctor);
        structsList.add(structClass);
    }

    @Override
    public void visit(ArrayAccessExpression node) throws ASTVisitorException {
        SymTableEntry symEntry = ASTUtils.getSafeSymbolTable(node).lookup(node.getIdentifier());
        Type arrType = symEntry.getType().getElementType();

        //LOAD ARRREFERENCE TO STACK
        mnStack.element().instructions.add(new VarInsnNode(Opcodes.ALOAD,symEntry.getIndex()));
        node.getIndex().accept(this);

        mnStack.element().instructions.add(new InsnNode(arrType.getOpcode(Opcodes.IALOAD)));
    }

    @Override
    public void visit(BooleanLiteralExpression node) throws ASTVisitorException {
        
    }

    @Override
    public void visit(CharLiteralExpression node) throws ASTVisitorException {

        char d = node.getExpression();
        mnStack.element().instructions.add(new LdcInsnNode(d));
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {

        //TO DO: FIX FOR COMPLEX FUNCTIONS
        if(!node.getIdentifier().equals(Environment.PRINT_FN_ID)){
            SymTableEntry entry = ASTUtils.getSafeSymbolTable(node).lookup(node.getIdentifier());
            int i=0;

            for (Expression e : node.getExpressions()) {
                Type target = entry.getType().getArgumentTypes()[i];
                e.accept(this);
                ByteCodeUtils.widen(target, ASTUtils.getSafeType(e),mnStack.element());
                i++;
            }
            
            mnStack.element().instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Environment.PROGRAM_NAME, node.getIdentifier(), entry.getType().getDescriptor()));

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
    @SuppressWarnings("unchecked")
    public void visit(StructArrayAccessExpression node) throws ASTVisitorException {
        //LOAD ARRAY REFERENCE
        IdentifierExpression  expr = (IdentifierExpression)node.getStruct();

        SymTableEntry struct = ASTUtils.getSafeSymbolTable(node).lookup(expr.getIdentifier());
        String owner = struct.getType().getInternalName();

        SymTable<SymTableEntry> structSymTable = Registry.getInstance().getStructs().get(owner);
        SymTableEntry sField = structSymTable.lookup(node.getIdentifier());
        Type arrType = sField.getType().getElementType();

        mnStack.element().instructions.add(new VarInsnNode(Opcodes.ALOAD, struct.getIndex()));

        String valDesc = sField.getType().getDescriptor();
        mnStack.element().instructions.add(new FieldInsnNode(Opcodes.GETFIELD,owner,node.getIdentifier(),valDesc));

        //LOAD INDEX TO STACK
        node.getIndex().accept(this);

        mnStack.element().instructions.add(new InsnNode(arrType.getOpcode(Opcodes.IASTORE)));
   
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visit(StructVariableAccessExpression node) throws ASTVisitorException {

        IdentifierExpression  expr = (IdentifierExpression)node.getStruct();

        SymTableEntry entry = ASTUtils.getSafeSymbolTable(node).lookup(expr.getIdentifier());

        //Load struct reference
        mnStack.element().instructions.add(new VarInsnNode(Opcodes.ALOAD, entry.getIndex()));


        String owner = entry.getType().getInternalName();
        SymTable<SymTableEntry> structSymTable = Registry.getInstance().getStructs().get(owner);
        SymTableEntry symEntry = structSymTable.lookup(node.getIdentifier());

        String valDesc = symEntry.getType().getDescriptor();
        mnStack.element().instructions.add(new FieldInsnNode(Opcodes.GETFIELD,owner,node.getIdentifier(),valDesc));

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

    private int getT_Type(Type type) throws ASTVisitorException{
        switch(type.getSort()){
            case Type.INT:
                return Opcodes.T_INT;
            case Type.FLOAT:
                return Opcodes.T_FLOAT;
            case Type.CHAR:
                return Opcodes.T_CHAR;
            default:
                throw new ASTVisitorException("Not supported type for array");
        }

    }

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
