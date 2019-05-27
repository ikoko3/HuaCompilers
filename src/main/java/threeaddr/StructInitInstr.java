/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threeaddr;

/**
 *
 * @author john_
 */
public class StructInitInstr implements Instruction{

    public String variable;
    
    public String structName;

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getStructName() {
        return structName;
    }

    public void setStructName(String structName) {
        this.structName = structName;
    }

    public StructInitInstr() {
    }

    public StructInitInstr(String variable, String structName) {
        this.variable = variable;
        this.structName = structName;
    }
    
    
    
    @Override
    public String emit() {
        return  variable +" = new "+structName;
    }
    
}
