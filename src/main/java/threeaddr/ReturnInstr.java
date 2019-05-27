/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threeaddr;


public class ReturnInstr implements Instruction{

    private String value;

    public ReturnInstr() {
    }

    public ReturnInstr(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    @Override
    public String emit() {
        if(value == null)
            return "return";
        else
            return "return " + value;   
    }
    
}
