/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package visitor.expression;

import visitor.ast.ASTVisitorException;
import visitor.ast.BytecodeGeneratorASTVisitor;

import ast.*;
import ast.expression.*;
import core.ByteCodeUtils;
import core.Registry;
import symbol.SymTable;
import symbol.SymTableEntry;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;


public class AssignmentASTVisitor implements ExpressionVisitor {

    private MethodNode mn;
    private Expression value;
    private BytecodeGeneratorASTVisitor bCGenerator;

    public AssignmentASTVisitor(BytecodeGeneratorASTVisitor bCGenerator, Expression value, MethodNode mn) {
        this.bCGenerator = bCGenerator;
        this.value = value;
        this.mn = mn;
    }

   

 
    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        SymTableEntry symEntry = ASTUtils.getSafeSymbolTable(node).lookup(node.getIdentifier());
        Type exprType = ASTUtils.getSafeType(value);

        value.accept(bCGenerator);
        ByteCodeUtils.widen(symEntry.getType(),exprType,mn);
        
        mn.instructions.add(new VarInsnNode(symEntry.getType().getOpcode(Opcodes.ISTORE), symEntry.getIndex())); 
    }

    @Override
    public void visit(FloatLiteralExpression node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(ArrayAccessExpression node) throws ASTVisitorException {
        
        SymTableEntry symEntry = ASTUtils.getSafeSymbolTable(node).lookup(node.getIdentifier());
        Type arrType = symEntry.getType().getElementType();
        Type exprType = ASTUtils.getSafeType(value);


        //LOAD ARRREFERENCE TO STACK
        mn.instructions.add(new VarInsnNode(Opcodes.ALOAD,symEntry.getIndex()));
        // //LOAD INDEX TO STACK
        node.getIndex().accept(bCGenerator);
        //LOAD VALUE TO STACK
        value.accept(bCGenerator);
        
        ByteCodeUtils.widen(arrType,exprType,mn);
        //ADD AASTORE
        mn.instructions.add(new InsnNode(arrType.getOpcode(Opcodes.IASTORE)));
    }

    @Override
    public void visit(BooleanLiteralExpression node) throws ASTVisitorException {
        //nothing       
    }

    @Override
    public void visit(CharLiteralExpression node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {
        //nothing
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
        Type exprType = ASTUtils.getSafeType(value);

        mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, struct.getIndex()));

        String valDesc = sField.getType().getDescriptor();
        mn.instructions.add(new FieldInsnNode(Opcodes.GETFIELD,owner,node.getIdentifier(),valDesc));

        //LOAD INDEX TO STACK
        node.getIndex().accept(bCGenerator);
        //LOAD VALUE TO STACK
        value.accept(bCGenerator);
         
        ByteCodeUtils.widen(arrType,exprType,mn);
        //ADD AASTORE
        mn.instructions.add(new InsnNode(arrType.getOpcode(Opcodes.IASTORE)));

    }

    @Override
    @SuppressWarnings("unchecked")
    public void visit(StructVariableAccessExpression node) throws ASTVisitorException {  
        //WORKS ONLY FOR SIMPLE STRUCTS!!      
        IdentifierExpression  expr = (IdentifierExpression)node.getStruct();
        SymTableEntry entry = ASTUtils.getSafeSymbolTable(node).lookup(expr.getIdentifier());

        //Load struct reference
        mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, entry.getIndex()));
        //load value
        value.accept(bCGenerator);

        String owner = entry.getType().getInternalName();
        SymTable<SymTableEntry> structSymTable = Registry.getInstance().getStructs().get(owner);
        SymTableEntry symEntry = structSymTable.lookup(node.getIdentifier());

        String valDesc = symEntry.getType().getDescriptor();

        ByteCodeUtils.widen(symEntry.getType(),ASTUtils.getSafeType(value), mn);
        mn.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD,owner,node.getIdentifier(),valDesc));

    }

}
