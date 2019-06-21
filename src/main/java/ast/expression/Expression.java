/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package ast.expression;

import ast.ASTNode;
import visitor.ast.ASTVisitorException;
import visitor.expression.ExpressionVisitor;

public abstract class Expression extends ASTNode {
    
    public abstract void accept(ExpressionVisitor visitor) throws ASTVisitorException;
}
