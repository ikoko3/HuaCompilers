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

}
