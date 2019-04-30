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
public class NotComparableException extends TypeException{
    private static final long serialVersionUID = 1L;

    public NotComparableException() {
    }

    public NotComparableException(String msg) {
        super(msg);
    }

    public NotComparableException(String msg, Throwable t) {
        super(msg, t);
    }

    public NotComparableException(Throwable t) {
        super(t);
    }
    
    
}
