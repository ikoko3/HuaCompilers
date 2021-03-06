/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.statement;

import visitor.ast.ASTVisitor;
import visitor.ast.ASTVisitorException;
import visitor.statement.StatementVisitor;

/**
 *
 * @author john_
 */
public class BreakStatement extends Statement{

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
        public void accept(StatementVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
