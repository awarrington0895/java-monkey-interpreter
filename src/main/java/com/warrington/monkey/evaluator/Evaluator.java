package com.warrington.monkey.evaluator;

import com.warrington.monkey.ast.*;
import com.warrington.monkey.object.*;

import java.util.List;

public class Evaluator {

    // No need to allocate new objects for true/false whenever it is encountered
    // Can simply reference these constants
    private static final Bool TRUE = new Bool(true);
    private static final Bool FALSE = new Bool(false);
    private static final Null NULL = new Null();

    public static MonkeyObject eval(Node node) {
        return switch (node) {
            // Statements
            case Program p -> evalStatements(p.getStatements());
            case ExpressionStatement es -> eval(es.getExpression());

            // Expressions
            case IntegerLiteral il -> new Int(il.value());
            case MonkeyBoolean mb -> nativeBoolToBooleanObject(mb.value());
            case PrefixExpression pe -> {
                MonkeyObject right = eval(pe.right());

                yield evalPrefixExpression(pe.operator(), right);
            }
            default -> null;
        };
    }

    private static MonkeyObject evalStatements(List<Statement> statements) {
       MonkeyObject result = null;

        for (Statement stmt : statements) {
            result = eval(stmt);
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
        return switch(operator) {
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
