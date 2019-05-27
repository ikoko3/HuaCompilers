/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package threeaddr;

public class AssignInstr implements Instruction {

    private String result;
    private String target;

    public AssignInstr(String target, String result) {
        this.result = result;
        this.target = target;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String emit() {
        return target + " = " + result;
    }

}
