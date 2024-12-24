package com.warrington.monkey.evaluator;

import com.warrington.monkey.ast.*;
import com.warrington.monkey.object.Bool;
import com.warrington.monkey.object.Int;
import com.warrington.monkey.object.MonkeyObject;

import java.util.List;

public class Evaluator {

    private static final Bool TRUE = new Bool(true);
    private static final Bool FALSE = new Bool(false);

    public static MonkeyObject eval(Node node) {
        return switch (node) {
            // Statements
            case Program p -> evalStatements(p.getStatements());
            case ExpressionStatement es -> eval(es.getExpression());

            // Expressions
            case IntegerLiteral il -> new Int(il.value());
            case MonkeyBoolean mb -> nativeBoolToBooleanObject(mb.value());
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
}
