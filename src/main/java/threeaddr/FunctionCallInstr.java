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
public class FunctionCallInstr implements Instruction{

    public String functionName;
    
    public int noOfParams;

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public int getNoOfParams() {
        return noOfParams;
    }

    public void setNoOfParams(int noOfParams) {
        this.noOfParams = noOfParams;
    }

    public FunctionCallInstr(String functionName, int noOfParams) {
        this.functionName = functionName;
        this.noOfParams = noOfParams;
    }

    public FunctionCallInstr() {
    }
    
    
    
    @Override
    public String emit() {
        return "call "+functionName +", "+noOfParams;
    }
    
}
