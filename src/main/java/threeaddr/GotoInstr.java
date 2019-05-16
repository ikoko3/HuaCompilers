/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package threeaddr;

public class GotoInstr implements Instruction {

    protected LabelInstr target;

    public GotoInstr() {
        this.target = null;
    }

    public GotoInstr(LabelInstr target) {
        this.target = target;
    }

    public LabelInstr getTarget() {
        return target;
    }

    public void setTarget(LabelInstr target) {
        this.target = target;
    }

    @Override
    public String emit() {
        if (target == null) {
            return "goto _";
        } else {
            return "goto " + target.getName();
        }
    }

}
