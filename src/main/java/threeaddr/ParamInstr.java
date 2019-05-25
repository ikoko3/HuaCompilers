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
public class ParamInstr implements Instruction{

    public String param;

    public ParamInstr() {
    }

    public ParamInstr(String param) {
        this.param = param;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }
    
    
    
    @Override
    public String emit() {
        return "param "+param;
    }
    
}
