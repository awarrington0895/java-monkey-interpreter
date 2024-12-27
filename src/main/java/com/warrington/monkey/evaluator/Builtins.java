package com.warrington.monkey.evaluator;

import com.warrington.monkey.object.*;

import java.util.Map;

import static com.warrington.monkey.evaluator.Evaluator.NULL;

public class Builtins {
    private static final Map<String, Builtin> builtins = Map.of(
        "len", new Builtin(Builtins::len),
        "first", new Builtin(Builtins::first)
    );

    private static MonkeyObject len(MonkeyObject... args) {
        if (args.length != 1) {
            return Evaluator.newError("wrong number of arguments. got=%d, want=1", args.length);
        }

        return switch (args[0]) {
            case Str s -> new Int(s.value().length());
            case Array a -> new Int(a.elements().size());
            default -> Evaluator.newError("argument to 'len' not supported, got %s", args[0].type());
        };
    }

    private static MonkeyObject first(MonkeyObject... args) {
        if (args.length != 1) {
            return Evaluator.newError("wrong number of arguments. got=%d, want=1", args.length);
        }

        return switch (args[0]) {
            case Array a -> a.elements().isEmpty() ? NULL : a.elements().getFirst();
            default -> Evaluator.newError("argument to 'first' not supported, got %s", args[0].type());
        };
    }

    public static Builtin get(String name) {
        return builtins.get(name);
    }
}
