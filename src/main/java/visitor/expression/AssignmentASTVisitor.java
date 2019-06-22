/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package visitor.expression;

import visitor.ast.ASTVisitorException;
import visitor.ast.BytecodeGeneratorASTVisitor;
import visitor.ast.ASTVisitor;
import ast.definition.ParameterDeclaration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import types.TypeUtils;
import ast.*;
import ast.definition.*;
import ast.expression.*;
import ast.statement.*;
import core.ByteCodeUtils;
import core.Environment;
import core.Operator;
import core.Registry;
import symbol.SymTable;
import symbol.SymTableEntry;

import org.apache.commons.lang3.StringEscapeUtils;

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

        SymTableEntry entry = ASTUtils.getSafeSymbolTable(node).lookup(expr.getIdentifier());
        mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, entry.getIndex()));

        String owner = entry.getType().getInternalName();
        String valDesc = Type.getType("[I").getDescriptor();
        mn.instructions.add(new FieldInsnNode(Opcodes.GETFIELD,owner,node.getIdentifier(),valDesc));

        SymTable<SymTableEntry> structSymTable = Registry.getInstance().getStructs().get(owner);
        SymTableEntry symEntry = structSymTable.lookup(node.getIdentifier());
        Type arrType = symEntry.getType().getElementType();
        Type exprType = ASTUtils.getSafeType(value);

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
