package core;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import ast.ASTNode;
import ast.ASTUtils;
import ast.expression.Expression;
import ast.expression.UnaryExpression;
import visitor.ast.ASTVisitorException;
import types.TypeUtils;

public class ByteCodeUtils{
    public static void widen(Type target, Type source, MethodNode mn) {
        if (source.equals(target)) {
            return;
        }

        if (source.equals(Type.INT_TYPE)) {
            if (target.equals(Type.FLOAT_TYPE)) {
                mn.instructions.add(new InsnNode(Opcodes.I2F));
            } else if (target.equals(TypeUtils.STRING_TYPE)) {
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;"));
            }
        } else if (source.equals(Type.FLOAT_TYPE)) {
            if (target.equals(TypeUtils.STRING_TYPE)) {
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;"));
            }
        }
    }

    public static void handleNumberOperator(Expression node, Operator op, Type type, MethodNode mn) throws ASTVisitorException {
        List<JumpInsnNode> trueList = new ArrayList<JumpInsnNode>();
        if (op.equals(Operator.PLUS)) {

            mn.instructions.add(new InsnNode(type.getOpcode(Opcodes.IADD)));
        } else if (op.equals(Operator.MINUS)) {

            mn.instructions.add(new InsnNode(type.getOpcode(Opcodes.ISUB)));
        } else if (op.equals(Operator.MULTIPLY)) {

            mn.instructions.add(new InsnNode(type.getOpcode(Opcodes.IMUL)));
        } else if (op.equals(Operator.DIVISION)) {

            mn.instructions.add(new InsnNode(type.getOpcode(Opcodes.IDIV)));
        } else if (op.isRelational()) {

            if (type.equals(Type.FLOAT_TYPE)) {
                mn.instructions.add(new InsnNode(Opcodes.FCMPG));
                JumpInsnNode jmp = null;
                switch (op) {
                    case EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFEQ, null);
                        mn.instructions.add(jmp);
                        break;
                    case NOT_EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFNE, null);
                        mn.instructions.add(jmp);
                        break;
                    case GREATER:
                        jmp = new JumpInsnNode(Opcodes.IFGT, null);
                        mn.instructions.add(jmp);
                        break;
                    case GREATER_EQ:
                        jmp = new JumpInsnNode(Opcodes.IFGE, null);
                        mn.instructions.add(jmp);
                        break;
                    case LESS:
                        jmp = new JumpInsnNode(Opcodes.IFLT, null);
                        mn.instructions.add(jmp);
                        break;
                    case LESS_EQ:
                        jmp = new JumpInsnNode(Opcodes.IFLE, null);
                        mn.instructions.add(jmp);
                        break;
                    default:
                        ASTUtils.error(node, "Operator not supported");
                        break;
                }
                trueList.add(jmp);
            } else if (type.equals(Type.INT_TYPE)) {
                JumpInsnNode jmp = null;
                switch (op) {
                    case EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IF_ICMPEQ, null);
                        mn.instructions.add(jmp);
                        break;
                    case NOT_EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IF_ICMPNE, null);
                        mn.instructions.add(jmp);
                        break;
                    case GREATER:
                        jmp = new JumpInsnNode(Opcodes.IF_ICMPGT, null);
                        mn.instructions.add(jmp);
                        break;
                    case GREATER_EQ:
                        jmp = new JumpInsnNode(Opcodes.IF_ICMPGE, null);
                        mn.instructions.add(jmp);
                        break;
                    case LESS:
                        jmp = new JumpInsnNode(Opcodes.IF_ICMPLT, null);
                        mn.instructions.add(jmp);
                        break;
                    case LESS_EQ:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPLT, null);
                    mn.instructions.add(jmp);
                    break;
                default:
                    break;
            }
            trueList.add(jmp);
        }

    }
        ASTUtils.setTrueList(node, trueList);
        List<JumpInsnNode> falseList = new ArrayList<JumpInsnNode>();
        JumpInsnNode jmp = new JumpInsnNode(Opcodes.GOTO, null);

        falseList.add(jmp);
        ASTUtils.setFalseList(node, falseList);
    }

    public static void handleBooleanOperator(Expression node, Operator op, Type type,MethodNode mn) throws ASTVisitorException {
        List<JumpInsnNode> trueList = new ArrayList<JumpInsnNode>();

        if (type.equals(TypeUtils.STRING_TYPE)) {
            mn.instructions.add(new InsnNode(Opcodes.SWAP));
            JumpInsnNode jmp = null;
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));
            switch (op) {
                case EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IFNE, null);
                    break;
                case NOT_EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IFEQ, null);
                    break;
                default:
                    ASTUtils.error(node, "Operator not supported on strings");
                    break;
            }
            mn.instructions.add(jmp);
            trueList.add(jmp);
        } else if (type.equals(Type.FLOAT_TYPE)) {
            mn.instructions.add(new InsnNode(Opcodes.FCMPG));
            JumpInsnNode jmp = null;
                switch (op) {
                    case EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFEQ, null);
                        mn.instructions.add(jmp);
                        break;
                    case NOT_EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFNE, null);
                        mn.instructions.add(jmp);
                        break;
                    case GREATER:
                        jmp = new JumpInsnNode(Opcodes.IFGT, null);
                        mn.instructions.add(jmp);
                        break;
                    case GREATER_EQ:
                        jmp = new JumpInsnNode(Opcodes.IFGE, null);
                        mn.instructions.add(jmp);
                        break;
                    case LESS:
                        jmp = new JumpInsnNode(Opcodes.IFLT, null);
                        mn.instructions.add(jmp);
                        break;
                    case LESS_EQ:
                        jmp = new JumpInsnNode(Opcodes.IFLE, null);
                        mn.instructions.add(jmp);
                        break;
                    default:
                        ASTUtils.error(node, "Operator not supported");
                        break;
                }
                trueList.add(jmp);
            
        } else {
            JumpInsnNode jmp = null;
            switch (op) {
                case EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPEQ, null);
                    mn.instructions.add(jmp);
                    break;
                case NOT_EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPNE, null);
                    mn.instructions.add(jmp);
                    break;
                case GREATER:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPGT, null);
                    mn.instructions.add(jmp);
                    break;
                case GREATER_EQ:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPGE, null);
                    mn.instructions.add(jmp);
                    break;
                case LESS:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPLT, null);
                    mn.instructions.add(jmp);
                    break;
                case LESS_EQ:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPLE, null);
                    mn.instructions.add(jmp);
                    break;
                case AND:
                    break;
                case OR:
                    break;
                default:
                    ASTUtils.error(node, "Operator not supported");
                    break;
            }
            trueList.add(jmp);
        }
        ASTUtils.setTrueList(node, trueList);
        List<JumpInsnNode> falseList = new ArrayList<JumpInsnNode>();
        JumpInsnNode jmp = new JumpInsnNode(Opcodes.GOTO, null);
        mn.instructions.add(jmp);
        falseList.add(jmp);
        ASTUtils.setFalseList(node, falseList);
    }

    public static void handleUnaryOperator(UnaryExpression node, Operator op,Type type,MethodNode mn) throws ASTVisitorException{
        switch(op){
            case NOT:
                ASTUtils.setFalseList(node, ASTUtils.getTrueList(node.getExpression()));
                ASTUtils.setTrueList(node, ASTUtils.getFalseList(node.getExpression()));
                break;
            case MINUS:
                mn.instructions.add(new InsnNode(type.getOpcode(Opcodes.INEG)));
                break;
            default:
                ASTUtils.error(node,"This operator is not supported in unary expressions");
        }
    }

    /**
     * Assumes top of stack contains two strings
     */
    public static void handleStringOperator(ASTNode node, Operator op,MethodNode mn) throws ASTVisitorException {
        if (op.equals(Operator.PLUS)) {
            mn.instructions.add(new TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"));
            mn.instructions.add(new InsnNode(Opcodes.DUP));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V"));
            mn.instructions.add(new InsnNode(Opcodes.SWAP));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
            mn.instructions.add(new InsnNode(Opcodes.SWAP));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;"));
        } else if (op.isRelational()) {
            LabelNode trueLabelNode = new LabelNode();
            switch (op) {
                case EQUAL:
                    mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));
                    mn.instructions.add(new JumpInsnNode(Opcodes.IFNE, trueLabelNode));
                    break;
                case NOT_EQUAL:
                    mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));
                    mn.instructions.add(new JumpInsnNode(Opcodes.IFEQ, trueLabelNode));
                    break;
                default:
                    ASTUtils.error(node, "Operator not supported on strings");
                    break;
            }
            mn.instructions.add(new InsnNode(Opcodes.ICONST_0));
            LabelNode endLabelNode = new LabelNode();
            mn.instructions.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode));
            mn.instructions.add(trueLabelNode);
            mn.instructions.add(new InsnNode(Opcodes.ICONST_1));
            mn.instructions.add(endLabelNode);
        } else {
            ASTUtils.error(node, "Operator not recognized");
        }
    }

}