package visitor.expression;

import ast.expression.*;
import visitor.ast.ASTVisitorException;


public interface ExpressionVisitor {
    
    void visit(ArrayAccessExpression node) throws ASTVisitorException;
    
    void visit(BinaryExpression node) throws ASTVisitorException;

    void visit(BooleanLiteralExpression node) throws ASTVisitorException;
    
    void visit(CharLiteralExpression node) throws ASTVisitorException;

    void visit(FloatLiteralExpression node) throws ASTVisitorException;
    
    void visit(FunctionCallExpression node) throws ASTVisitorException;
    
    void visit(IdentifierExpression node) throws ASTVisitorException;
    
    void visit(IntegerLiteralExpression node) throws ASTVisitorException;

    void visit(ParenthesisExpression node) throws ASTVisitorException;
    
    void visit(StringLiteralExpression node) throws ASTVisitorException;
    
    void visit(StructArrayAccessExpression node) throws ASTVisitorException;
    
    void visit(StructVariableAccessExpression node) throws ASTVisitorException;
    
    void visit(UnaryExpression node) throws ASTVisitorException;
}
