/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package ast.statement;

import visitor.ast.ASTVisitor;
import visitor.ast.ASTVisitorException;
import java.util.ArrayList;
import java.util.List;
import visitor.statement.StatementVisitor;

public class CompoundStatement extends Statement {

    private List<Statement> statements;

    public CompoundStatement() {
        this.statements = new ArrayList<Statement>();
    }

    public CompoundStatement(List<Statement> statements) {
        this.statements = statements;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }

    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }
    
        public void accept(StatementVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
