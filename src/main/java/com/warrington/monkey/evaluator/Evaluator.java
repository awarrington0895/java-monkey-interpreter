package com.warrington.monkey.evaluator;

import com.warrington.monkey.ast.*;
import com.warrington.monkey.object.*;

import java.util.List;

public class Evaluator {

    // No need to allocate new objects for true/false whenever it is encountered
    // Can simply reference these constants
    private static final Bool TRUE = new Bool(true);
    private static final Bool FALSE = new Bool(false);
    static final Null NULL = new Null();

    public static MonkeyObject eval(Node node) {
        return switch (node) {
            // Statements
            case Program p -> evalProgram(p.getStatements());
            case ExpressionStatement es -> eval(es.getExpression());
            case BlockStatement bs -> evalBlockStatement(bs);
            case ReturnStatement rs -> {
                MonkeyObject value = eval(rs.returnValue());

                yield new ReturnValue(value);
            }

            // Expressions
            case IntegerLiteral il -> new Int(il.value());
            case MonkeyBoolean mb -> nativeBoolToBooleanObject(mb.value());
            case PrefixExpression pe -> {
                MonkeyObject right = eval(pe.right());

                yield evalPrefixExpression(pe.operator(), right);
            }

            case InfixExpression ie -> {
                MonkeyObject left = eval(ie.left());
                MonkeyObject right = eval(ie.right());

                yield evalInfixExpression(ie.operator(), left, right);
            }

            case IfExpression ifExpression -> evalIfExpression(ifExpression);
            default -> null;
        };
    }

    private static MonkeyObject evalBlockStatement(BlockStatement block) {
       MonkeyObject result = null;

       for (Statement stmt : block.statements()) {
           result = eval(stmt);
           
           if (result != null && result.type() == ObjectType.RETURN_VALUE) {
               return result;
           }
       }

       return result;
    }

    private static MonkeyObject evalIfExpression(IfExpression ifExp) {
        MonkeyObject condition = eval(ifExp.condition());

        if (isTruthy(condition)) {
            return eval(ifExp.consequence());
        } else if (ifExp.alternative() != null) {
            return eval(ifExp.alternative());
        } else {
            return NULL;
        }
    }

    private static boolean isTruthy(MonkeyObject object) {
        return object != NULL && object != FALSE;
    }

    private static MonkeyObject evalInfixExpression(String operator, MonkeyObject left, MonkeyObject right) {
        if (left.type() == ObjectType.INTEGER && right.type() == ObjectType.INTEGER) {
            return evalIntegerInfixExpression(operator, (Int) left, (Int) right);
        }

        if (left.type() == ObjectType.BOOLEAN && right.type() == ObjectType.BOOLEAN) {
            return evalBooleanInfixExpression(operator, (Bool) left, (Bool) right);
        }

        return NULL;
    }

    private static MonkeyObject evalBooleanInfixExpression(String operator, Bool left, Bool right) {
        return switch (operator) {
            case "==" -> nativeBoolToBooleanObject(left == right);
            case "!=" -> nativeBoolToBooleanObject(left != right);
            default -> NULL;
        };
    }

    private static MonkeyObject evalIntegerInfixExpression(String operator, Int left, Int right) {
        final long leftVal = left.value();
        final long rightVal = right.value();

        return switch (operator) {
            case "+" -> new Int(leftVal + rightVal);
            case "-" -> new Int(leftVal - rightVal);
            case "/" -> new Int(leftVal / rightVal);
            case "*" -> new Int(leftVal * rightVal);
            case "<" -> nativeBoolToBooleanObject(leftVal < rightVal);
            case ">" -> nativeBoolToBooleanObject(leftVal > rightVal);
            case "==" -> nativeBoolToBooleanObject(leftVal == rightVal);
            case "!=" -> nativeBoolToBooleanObject(leftVal != rightVal);
            default -> NULL;
        };
    }

    private static MonkeyObject evalProgram(List<Statement> statements) {
        MonkeyObject result = null;

        for (Statement stmt : statements) {
            result = eval(stmt);

            if (result instanceof ReturnValue(MonkeyObject value)) {
                return value;
            }
        }

        return result;
    }

    private static MonkeyObject nativeBoolToBooleanObject(boolean input) {
        if (input) {
            return TRUE;
        }

        return FALSE;
    }

    private static MonkeyObject evalPrefixExpression(String operator, MonkeyObject right) {
        return switch (operator) {
            case "!" -> evalBangOperatorExpression(right);
            case "-" -> evalMinusPrefixOperatorExpression(right);
            default -> NULL;
        };
    }

    private static MonkeyObject evalMinusPrefixOperatorExpression(MonkeyObject right) {
        return switch (right) {
            case Int i -> new Int(-i.value());
            default -> NULL;
        };
    }

    private static MonkeyObject evalBangOperatorExpression(MonkeyObject right) {
        if (right == TRUE) {
            return FALSE;
        } else if (right == FALSE) {
            return TRUE;
        } else if (right == NULL) {
            return TRUE;
        } else {
            return FALSE;
        }
    }
}
