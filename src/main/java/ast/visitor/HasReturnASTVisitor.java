package ast.visitor;

import ast.expression.*;
import ast.statement.*;
import ast.definition.*;
import ast.*;

import java.util.List;

/**
 * Check if the a set of statements have a return statement.
 */
public class HasReturnASTVisitor implements ASTVisitor {

    private Boolean stContsReturn = false;

    public Boolean containsReturn(){
        return stContsReturn;
    }

    public HasReturnASTVisitor() {
        
    }

    @Override
    public void visit(CompUnit node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        //nothing
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

    }
    
    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        HandleStatements(node,node.getStatements());
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
        HandleStatements(node,node.getStatements());
    }

    @Override
    public void visit(StructDefinition node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(ArrayAccessExpression node) throws ASTVisitorException {
        //nothing
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
    public void visit(FloatLiteralExpression node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {
        //nothing
    }


    @Override
    public void visit(StructArrayAccessExpression node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(StructVariableAccessExpression node) throws ASTVisitorException {
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
    public void visit(EmptyStatement node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        stContsReturn = checkExclusiveStatements(node.getStatement(),node.getElseStatement());
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        //nothing
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        stContsReturn = true;
    }

    @Override
    public void visit(VariableDefinition node) throws ASTVisitorException {
        //nothing
    }

    private void HandleStatements(ASTNode node,List<Statement> statements) throws ASTVisitorException{
        for(Statement st : statements){
            st.accept(this);

            if(stContsReturn){
                stContsReturn = true;

                if(statements.indexOf(st) < statements.size() - 1)
                    ASTUtils.warning(st,"Unreachable code after this line.");

                break;
            }
        }
    }

    //This function checks if multiple statments all contain a return statemnt.
    private boolean checkExclusiveStatements(Statement... statements) throws ASTVisitorException{
        for(Statement st : statements){
            HasReturnASTVisitor visitor = new HasReturnASTVisitor();
            st.accept(visitor);

            if(!visitor.containsReturn())
                return false;
        }
        return true;
    }
}
