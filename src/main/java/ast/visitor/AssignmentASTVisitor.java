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


public class AssignmentASTVisitor implements ASTVisitor {

    private MethodNode mn;

    public AssignmentASTVisitor(MethodNode mn) {
        this.mn = mn;
    }

    @Override
    public void visit(CompUnit node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        node.getResult().accept(this);
        Type exprType = ASTUtils.getSafeType(node.getResult());

        Expression target = node.getTarget();
        SymTableEntry symEntry = null;

        if(target instanceof IdentifierExpression){

            ByteCodeUtils.widen(symEntry.getType(),exprType,mn);
        
        mn.instructions.add(new VarInsnNode(symEntry.getType().getOpcode(Opcodes.ISTORE), symEntry.getIndex()));
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
            ASTUtils.error(node, "Assignment is not implemented for the type "+target.getClass());
        }

        if(symEntry == null)
            ASTUtils.error(node, "Assignment is not implemented for the type "+target.getClass());
            
        

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
        //nothing
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
    public void visit(WhileStatement node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(Array node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        //nothing   
    }

    @Override
    public void visit(Variable node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        //nothing
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

    @Override
    public void visit(EmptyStatement node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(VariableDefinition node) throws ASTVisitorException {
        node.getVariable().accept(this);
    }

}
