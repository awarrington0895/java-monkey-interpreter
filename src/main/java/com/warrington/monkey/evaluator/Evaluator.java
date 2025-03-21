package com.warrington.monkey.evaluator;

import com.warrington.monkey.ast.*;
import com.warrington.monkey.object.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Evaluator {

    public static final Null NULL = new Null();
    // No need to allocate new objects for true/false whenever it is encountered
    // Can simply reference these constants
    static final Bool TRUE = new Bool(true);
    static final Bool FALSE = new Bool(false);

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
            case StringLiteral sl -> new Str(sl.value());
            case MonkeyBoolean mb -> nativeBoolToBooleanObject(mb.value());
            case HashLiteral hl -> evalHashLiteral(hl, env);
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
            case ArrayLiteral al -> {
                List<MonkeyObject> elements = evalExpressions(al.elements(), env);

                if (elements.size() == 1 && isError(elements.getFirst())) {
                    yield elements.getFirst();
                }

                yield new Array(elements);
            }
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
            case IndexExpression ie -> evalIndexExpression(ie, env);
            default -> null;
        };
    }

    private static MonkeyObject evalIndexExpression(IndexExpression ie, Environment env) {
        MonkeyObject left = eval(ie.left(), env);
        MonkeyObject index = eval(ie.index(), env);

        if (left == null) {
            return newError("unable to evaluate left side of index expression: %s", ie);
        }

        if (index == null) {
            return newError("unable to evaluate index of index expression: %s", ie);
        }

        if (isError(left)) {
            return left;
        }

        if (isError(index)) {
            return index;
        }

        if (left instanceof Array(List<MonkeyObject> elements) && index instanceof Int(long value)) {
           if (value >= elements.size() || value < 0) {
               return NULL;
           }

            return elements.get((int) value);
        }

        if (left instanceof Hash(var pairs)) {
            if (!(index instanceof Hashable key)) {
                return newError("unusable as hash key: %s", index.type());
            }

            var pair = pairs.get(key.hashKey());

            if (pair == null) {
                return NULL;
            }

            return pair.value();
        }

        return newError("index operator not supported: %s", left.type());
    }

    private static MonkeyObject applyFunction(MonkeyObject fn, List<MonkeyObject> args) {
        return switch (fn) {
            case MonkeyFunction mf -> {
                final Environment extendedEnv = extendFunctionEnv(mf, args);

                MonkeyObject evaluated = eval(mf.body(), extendedEnv);

                yield unwrapReturnValue(evaluated);
            }
            case Builtin b -> b.func().apply(args.toArray(new MonkeyObject[0]));
            default -> newError("not a function: %s", fn.type());
        };
    }

    private static MonkeyObject unwrapReturnValue(MonkeyObject object) {
        if (object instanceof ReturnValue(MonkeyObject value)) {
            return value;
        }

        return object;
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

        return Collections.unmodifiableList(result);
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
            MonkeyObject builtin = Builtins.get(node.value());

            if (builtin != null) {
                return builtin;
            }

            return newError("identifier not found: %s", node.value());
        }

        return value;
    }

    private static MonkeyObject evalHashLiteral(HashLiteral hash, Environment env) {
        var pairs = new HashMap<HashKey, HashPair>();

        for (var entry : hash.pairs().entrySet()) {
           var key = eval(entry.getKey(), env);

           if (key == null) {
               return newError("cannot parse object: %s", entry.getKey().toString());
           }

           if (isError(key)) {
               return key;
           }

           if (!(key instanceof Hashable hashable)) {
               return newError("unusable as hash key: %s", key.type());
           }

           var value = eval(entry.getValue(), env);

           if (isError(value)) {
               return value;
           }

           pairs.put(hashable.hashKey(), new HashPair(key, value));
        }

        return new Hash(pairs);
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

        if (left.type() == ObjectType.STRING && right.type() == ObjectType.STRING) {
            return evalStringInfixExpression(operator, (Str) left, (Str) right);
        }

        if (left.type() != right.type()) {
            return newError("type mismatch: %s %s %s", left.type(), operator, right.type());
        }

        return newError("unknown operator: %s %s %s", left.type(), operator, right.type());
    }

    private static MonkeyObject evalStringInfixExpression(String operator, Str left, Str right) {
        return switch (operator) {
            case "+" -> new Str(left.value() + right.value());
            default -> newError("unknown operator: %s %s %s", left.type(), operator, right.type());
        };
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

    static MonkeyError newError(String format, Object... a) {
        return new MonkeyError(format.formatted(a));
    }
}
