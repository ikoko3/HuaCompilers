/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.statement;

import ast.ASTVisitor;
import ast.ASTVisitorException;

/**
 *
 * @author john_
 */
public class ContinueStatement extends Statement{

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
}
