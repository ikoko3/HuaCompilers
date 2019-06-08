package types;

import types.exception.*;
import core.Operator;
import ast.definition.ParameterDeclaration;
import java.util.List;
import java.util.Set;
import org.objectweb.asm.Type;

public class TypeUtils {

    public static final Type STRING_TYPE = Type.getType(String.class);

    private TypeUtils() {
    }

    public static Type maxType(Type type1, Type type2) {
        if (type1.equals(Type.FLOAT_TYPE)) {
            return type1;
        } else if (type2.equals(Type.FLOAT_TYPE)) {
            return type2;
        } else if (type1.equals(Type.INT_TYPE)) {
            return type1;
        } else if (type2.equals(Type.INT_TYPE)) {
            return type2;
        } else if (type1.equals(Type.CHAR_TYPE)) {
            return type1;
        } else if (type2.equals(Type.CHAR_TYPE)) {
            return type2;
        } else {
            return type1;
        }
    }

    public static Type minType(Type type1, Type type2) {
        if (type1.equals(Type.CHAR_TYPE)) {
            return type1;
        } else if (type2.equals(Type.CHAR_TYPE)) {
            return type2;
        } else if (type1.equals(Type.INT_TYPE)) {
            return type1;
        } else if (type2.equals(Type.INT_TYPE)) {
            return type2;
        } else if (type1.equals(Type.FLOAT_TYPE)) {
            return type1;
        } else if (type2.equals(Type.FLOAT_TYPE)) {
            return type2;
        } else {
            return type1;
        }
    }

    public static boolean isLargerOrEqualType(Type type1, Type type2) {
        return type1.getSort() >= type2.getSort();
    }

    public static boolean isAssignable(Type target, Type source) {
        return isLargerOrEqualType(target, source);
    }

    public static Type maxType(Set<Type> types) {
        Type max = null;
        for (Type t : types) {
            if (max == null) {
                max = t;
            }
            max = maxType(max, t);
        }
        return max;
    }

    public static Type minType(Set<Type> types) {
        Type min = null;
        for (Type t : types) {
            if (min == null) {
                min = t;
            }
            min = minType(min, t);
        }
        return min;
    }

    public static boolean isUnaryComparible(Operator op, Type type) {
        switch (op) {
            case MINUS:
                return isNumber(type);
            case NOT:
                return type.equals(Type.BOOLEAN_TYPE);
            default:
                return false;
        }
    }

    public static boolean isNumber(Type type) {
        return type.equals(Type.INT_TYPE) || type.equals(Type.FLOAT_TYPE);
    }

    public static boolean isNumber(Set<Type> types) {
        for (Type t : types) {
            if (isNumber(t)) 
                return true;
        }
        return false;
    }

    public static boolean areNumbers(Type... types){
        for (Type t : types) {
            if (!isNumber(t)) 
                return false;
        }
        return true;
    }
    
    public static boolean haveLogicalType(Type... types){
        for (Type t : types) {
            if (!(t.equals(Type.BOOLEAN_TYPE))) 
                return false;
        }
        return true;
    }

    public static Type applyUnary(Operator op, Type type) throws TypeException {
        if (!op.isUnary()) {
            throw new TypeException("Operator " + op + " is not unary");
        }
        if (!TypeUtils.isUnaryComparible(op, type)) {
            throw new TypeException("Type " + type + " is not unary comparible");
        }
        return type;
    }

    public static Type applyBinary(Operator op, Type t1, Type t2) throws TypeException {
        if(op.isArithmetic()){
            if(!areNumbers(t1,t2)) 
                throw new NotNumbersException();
            return maxType(t1,t2);
        }
        else if (op.isRelational()) {
            if(!TypeUtils.areComparable(t1, t2)) 
                throw new NotComparableException("Expressions are not comparable");
            return Type.BOOLEAN_TYPE;
        } 
        else if(op.isLogical()){
            if(!haveLogicalType(t1,t2)) 
                throw new CanNotApplyLogicalOperatorException("The types must be logical");
            return Type.BOOLEAN_TYPE;
        }
        else {
            throw new TypeException("Operator " + op + " not supported");
        }
    }

    public static boolean areComparable(Type type1, Type type2) {
        if (type1.equals(Type.INT_TYPE) || type1.equals(Type.FLOAT_TYPE)) {
             return type2.equals(Type.INT_TYPE) || type2.equals(Type.FLOAT_TYPE);
        } else if (type1.equals(Type.CHAR_TYPE)) {
            return type2.equals(Type.CHAR_TYPE);
        } else { // string
            return type2.equals(TypeUtils.STRING_TYPE);
        }
    }
    
    public static Type[] getParameterTypesFor(List<ParameterDeclaration> params){
        Type[] types =new Type[params.size()];
        for(int i=0;i<params.size();i++){
            types[i] = params.get(i).getVariable().getType();
        }
        
        return types;
    }

}
