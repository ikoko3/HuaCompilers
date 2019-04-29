/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ast;

/**
 *
 * @author john_
 */
public enum TypeEnum {

    CHAR("CHAR"),
    BOOL("BOOL"),
    INT("INT"),
    FLOAT("FLOAT"),
    VOID("VOID"),
    STRUCT("STRUCT");

    private String type;

    private TypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }

}
