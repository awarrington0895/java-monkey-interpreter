package com.warrington.monkey.evaluator;

import com.warrington.monkey.ast.*;
import com.warrington.monkey.object.*;

import java.util.ArrayList;
import java.util.List;

public class Evaluator {

    static final Null NULL = new Null();
    // No need to allocate new objects for true/false whenever it is encountered
    // Can simply reference these constants
    private static final Bool TRUE = new Bool(true);
    private static final Bool FALSE = new Bool(false);

    public static MonkeyObject eval(Node node, Environment env) {
        return switch (node) {
            // Statements
            case Program p -> evalProgram(p.getStatements(), env);
            case ExpressionStatement es -> eval(es.getExpression(), env);
            case BlockStatement bs -> evalBlockStatement(bs, env);
            case ReturnStatement rs -> {
                MonkeyObject value = eval(rs.returnValue(), env);

                if (isError(value)) {
                    yield value;
                }

                yield new ReturnValue(value);
            }

            case LetStatement ls -> {
                MonkeyObject value = eval(ls.value(), env);

                if (isError(value)) {
                    yield value;
                }

                env.set(ls.name().value(), value);

                yield value;
            }

            // Expressions
            case IntegerLiteral il -> new Int(il.value());
            case MonkeyBoolean mb -> nativeBoolToBooleanObject(mb.value());
            case PrefixExpression pe -> {
                MonkeyObject right = eval(pe.right(), env);

                if (isError(right)) {
                    yield right;
                }

                yield evalPrefixExpression(pe.operator(), right);
            }

            case InfixExpression ie -> {
                MonkeyObject left = eval(ie.left(), env);

                if (isError(left)) {
                    yield left;
                }

                MonkeyObject right = eval(ie.right(), env);

                if (isError(right)) {
                    yield right;
                }

                yield evalInfixExpression(ie.operator(), left, right);
            }

            case IfExpression ifExpression -> evalIfExpression(ifExpression, env);

            case Identifier i -> evalIdentifier(i, env);
            case FunctionLiteral fl -> new MonkeyFunction(fl.parameters(), fl.body(), env);
            case CallExpression ce -> {
                MonkeyObject function = eval(ce.function(), env);

                if (isError(function)) {
                    yield function;
                }

                List<MonkeyObject> args = evalExpressions(ce.arguments(), env);

                if (args.size() == 1 && isError(args.getFirst())) {
                    yield args.getFirst();
                }

                yield applyFunction(function, args);
            }
            default -> null;
        };
    }

    private static MonkeyObject applyFunction(MonkeyObject fn, List<MonkeyObject> args) {
        if (!(fn instanceof MonkeyFunction)) {
            return newError("not a function: %s", fn.type());
        }

        MonkeyFunction function = (MonkeyFunction) fn;

        final Environment extendedEnv = extendFunctionEnv(function, args);

        MonkeyObject evaluated = eval(function.body(), extendedEnv);

        return unwrapReturnValue(evaluated);
    }

    private static MonkeyObject unwrapReturnValue(MonkeyObject object) {
        return switch (object) {
            case ReturnValue(MonkeyObject value) -> value;
            default -> object;
        };
    }

    private static Environment extendFunctionEnv(MonkeyFunction fn, List<MonkeyObject> args) {
        Environment functionScope = fn.env().newEnclosed();

        for (int i = 0; i < fn.parameters().size(); i++) {
            functionScope.set(fn.parameters().get(i).value(), args.get(i));
        }

        return functionScope;
    }

    private static List<MonkeyObject> evalExpressions(List<Expression> exps, Environment env) {
        final var result = new ArrayList<MonkeyObject>();

        for (Expression exp : exps) {
            MonkeyObject evaluated = eval(exp, env);

            if (isError(evaluated)) {
                return List.of(evaluated);
            }

            result.add(evaluated);
        }

        return result;
    }

    private static boolean isError(MonkeyObject object) {
        if (object != null) {
            return object.type() == ObjectType.ERROR;
        }

        return false;
    }

    private static MonkeyObject evalIdentifier(Identifier node, Environment env) {
        MonkeyObject value = env.get(node.value());

        if (value == null) {
            return newError("identifier not found: %s", node.value());
        }

        return value;
    }

    private static MonkeyObject evalBlockStatement(BlockStatement block, Environment env) {
        MonkeyObject result = null;

        for (Statement stmt : block.statements()) {
            result = eval(stmt, env);

            if (result != null && (result.type() == ObjectType.RETURN_VALUE || result.type() == ObjectType.ERROR)) {
                return result;
            }
        }

        return result;
    }

    private static MonkeyObject evalIfExpression(IfExpression ifExp, Environment env) {
        MonkeyObject condition = eval(ifExp.condition(), env);

        if (isError(condition)) {
            return condition;
        }

        if (isTruthy(condition)) {
            return eval(ifExp.consequence(), env);
        } else if (ifExp.alternative() != null) {
            return eval(ifExp.alternative(), env);
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

        if (left.type() != right.type()) {
            return newError("type mismatch: %s %s %s", left.type(), operator, right.type());
        }

        return newError("unknown operator: %s %s %s", left.type(), operator, right.type());
    }

    private static MonkeyObject evalBooleanInfixExpression(String operator, Bool left, Bool right) {
        return switch (operator) {
            case "==" -> nativeBoolToBooleanObject(left == right);
            case "!=" -> nativeBoolToBooleanObject(left != right);
            default -> newError("unknown operator: %s %s %s", left.type(), operator, right.type());
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
            default -> newError("unknown operator: %s %s %s", left.type(), operator, right.type());
        };
    }

    private static MonkeyObject evalProgram(List<Statement> statements, Environment env) {
        MonkeyObject result = null;

        for (Statement stmt : statements) {
            result = eval(stmt, env);

            if (result instanceof ReturnValue(MonkeyObject value)) {
                return value;
            }

            if (result instanceof MonkeyError err) {
                return err;
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
            default -> newError("unknown operator: %s%s", operator, right.type());
        };
    }

    private static MonkeyObject evalMinusPrefixOperatorExpression(MonkeyObject right) {
        return switch (right) {
            case Int i -> new Int(-i.value());
            default -> newError("unknown operator: -%s", right.type());
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

    private static MonkeyError newError(String format, Object... a) {
        return new MonkeyError(format.formatted(a));
    }
}
