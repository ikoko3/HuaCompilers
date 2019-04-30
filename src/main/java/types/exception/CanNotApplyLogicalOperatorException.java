/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package types.exception;

/**
 *
 * @author john_
 */
public class CanNotApplyLogicalOperatorException extends TypeException{
    private static final long serialVersionUID = 1L;

    public CanNotApplyLogicalOperatorException() {
    }

    public CanNotApplyLogicalOperatorException(String msg) {
        super(msg);
    }

    public CanNotApplyLogicalOperatorException(String msg, Throwable t) {
        super(msg, t);
    }

    public CanNotApplyLogicalOperatorException(Throwable t) {
        super(t);
    }
    
    
}
