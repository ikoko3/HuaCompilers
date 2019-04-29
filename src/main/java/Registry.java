/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */

import java.util.HashMap;
import java.util.Map;
import ast.ASTNode;
import symbol.SymTable;

/**
 * Global registry (Singleton pattern)
 */
public class Registry {

    ASTNode root;
    private Map<String,SymTable> structs;

    private Registry() {
        root = null;
        structs = new HashMap<String,SymTable>();
    }

    private static class SingletonHolder {

        public static final Registry instance = new Registry();

    }

    public static Registry getInstance() {
        return SingletonHolder.instance;
    }

    public ASTNode getRoot() {
        return root;
    }

    public void setRoot(ASTNode root) {
        this.root = root;
    }

    public Map<String, SymTable> getStructs() {
        return structs;
    }  

}
