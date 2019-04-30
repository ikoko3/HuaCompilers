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
public class NotNumbersException extends TypeException{
    private static final long serialVersionUID = 1L;

    public NotNumbersException() {
    }

    public NotNumbersException(String msg) {
        super(msg);
    }

    public NotNumbersException(String msg, Throwable t) {
        super(msg, t);
    }

    public NotNumbersException(Throwable t) {
        super(t);
    }

   
    
}
