/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package ast;

import ast.visitor.ASTVisitor;
import ast.visitor.ASTVisitorException;
import ast.definition.Definition;
import java.util.ArrayList;
import java.util.List;

public class CompUnit extends ASTNode {

    private List<Definition> Definitions;

    public CompUnit() {
        Definitions = new ArrayList<Definition>();
    }

    public CompUnit(List<Definition> definitions) {
        this.Definitions = definitions;
    }

    public List<Definition> getDefinitions() {
        return Definitions;
    }

    public void setDefinitions(List<Definition> Definitions) {
        this.Definitions = Definitions;
    }



    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
