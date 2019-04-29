/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast.definition;

import ast.ASTNode;
import ast.ASTVisitor;
import ast.ASTVisitorException;
import ast.statement.Statement;

/**
 *
 * @author john_
 */
public abstract class Definition extends Statement{

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
