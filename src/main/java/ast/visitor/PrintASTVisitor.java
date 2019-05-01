package ast.visitor;

/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
import ast.expression.*;
import ast.statement.*;
import ast.definition.*;
import ast.*;


import org.apache.commons.lang3.StringEscapeUtils;

public class PrintASTVisitor implements ASTVisitor {

    @Override
    public void visit(CompUnit node) throws ASTVisitorException {
        for (Definition d : node.getDefinitions()) {
            d.accept(this);
        }
    }

    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        System.out.print(" ");
        System.out.print(node.getOperator());
        System.out.print(" ");
        node.getExpression2().accept(this);
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        System.out.print(node.getOperator());
        System.out.print(" ");
        node.getExpression().accept(this);
    }

    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        System.out.print(node.getIdentifier());
    }

    @Override
    public void visit(FloatLiteralExpression node) throws ASTVisitorException {
        System.out.print(node.getLiteral());
    }

    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        System.out.print(node.getLiteral());
    }
    
    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        System.out.print("\"");
        System.out.print(StringEscapeUtils.escapeJava(node.getLiteral()));
        System.out.print("\"");
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        System.out.print("( ");
        node.getExpression().accept(this);
        System.out.print(" )");
    }

    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        System.out.println(" { ");
        for(Statement st: node.getStatements()) { 
            st.accept(this);
        }
        System.out.println(" } ");
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        System.out.print(" = ");
        node.getExpression2().accept(this);
        System.out.print(";");
        System.out.println("");
    }

    @Override
    public void visit(Array node) throws ASTVisitorException {
        node.getType().accept(this);
        System.out.print(" "+ node.getName() +"[" +node.getLength()+ "]");
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        node.getVariable().accept(this);
    }

    @Override
    public void visit(Variable node) throws ASTVisitorException {
        node.getType().accept(this);
        System.out.print(" "+node.getName());
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        System.out.println("");
        node.getReturnType().accept(this);
        System.out.print(" "+node.getName() + "(" );
        for(ParameterDeclaration p: node.getParameters()){
            p.accept(this);
            if(node.getParameters().indexOf(p) != node.getParameters().size()-1)
                System.out.print(",");
        }
        System.out.println(") {");
        for(Statement s: node.getStatements()){
            s.accept(this);
        }
        System.out.println("}");
    }

    @Override
    public void visit(StructDefinition node) throws ASTVisitorException {
        System.out.println("");
        System.out.println("struct "+node.getName()+" {");
        for(VariableDefinition v: node.getVariables()){
             v.accept(this);
        }
        System.out.println("};");
    }

    @Override
    public void visit(ArrayAccessExpression node) throws ASTVisitorException {
        System.out.print(node.getIdentifier()+"[");
        node.getIndex().accept(this);
        System.out.print("]");
    }

    @Override
    public void visit(BooleanLiteralExpression node) throws ASTVisitorException {
        System.out.print(node.isExpression());
    }

    @Override
    public void visit(CharLiteralExpression node) throws ASTVisitorException {
        System.out.print("\'");
        System.out.print(node.getExpression());
        System.out.print("\'");
    }

    @Override
    public void visit(FunctionCallExpression node) throws ASTVisitorException {
        System.out.print(node.getIdentifier()+"(");
        for(Expression e: node.getExpressions()){
            e.accept(this);
        }
        System.out.print(");");
    }

    @Override
    public void visit(StructArrayAccessExpression node) throws ASTVisitorException {
        System.out.print(" ");
        node.getStruct().accept(this);
        System.out.print("."+node.getIdentifier()+"[");
        node.getIndex().accept(this);
        System.out.print("]");
    }

    @Override
    public void visit(StructVariableAccessExpression node) throws ASTVisitorException {
        System.out.print(" ");
        node.getStruct().accept(this);
        System.out.print("."+node.getIdentifier());
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        System.out.println("break;");
    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        System.out.println("continue;");
    }

    @Override
    public void visit(EmptyStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        System.out.println(";");
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        System.out.print("if(");
        node.getExpression().accept(this);
        System.out.println(")");
        node.getStatement().accept(this);
        System.out.println(" else");
        node.getElseStatement().accept(this);
        System.out.println("");
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        System.out.print("if(");
        node.getExpression().accept(this);
        System.out.println(")");
        node.getStatement().accept(this);
        System.out.println("");
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        System.out.print("return ");
        if(node.getExpression()!=null)
            node.getExpression().accept(this);
        System.out.println(";");
    }

    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        System.out.print("while(");
        node.getExpression().accept(this);
        System.out.println(")");
        node.getStatement().accept(this);
        System.out.println("");
    }

    @Override
    public void visit(TypeSpecifier node) throws ASTVisitorException {
        /*if(node.getType()==TypeEnum.STRUCT){
            System.out.print("struct "+node.getStuctId());
        }else{
            System.out.print(node.getType());
        }*/
        System.out.print(node.getType().getClassName());
    }
    
    @Override
    public void visit(StructSpecifier node) throws ASTVisitorException {
        System.out.print(node.getType());
    }

    @Override
    public void visit(VariableDefinition node) throws ASTVisitorException {
        node.getVariable().accept(this);
        System.out.print(";");
        System.out.println("");
    }

    

}
