/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package ast.statement;

import ast.ASTNode;
import visitor.ast.ASTVisitorException;
import visitor.statement.StatementVisitor;

public abstract class Statement extends ASTNode {
    
    public abstract void accept(StatementVisitor visitor) throws ASTVisitorException;
}
