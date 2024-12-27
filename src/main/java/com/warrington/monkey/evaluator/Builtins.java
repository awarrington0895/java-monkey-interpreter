package com.warrington.monkey.evaluator;

import com.warrington.monkey.object.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.warrington.monkey.evaluator.Evaluator.NULL;

public class Builtins {
    private static final Map<String, Builtin> builtins = Map.of(
        "len", new Builtin(Builtins::len),
        "first", new Builtin(Builtins::first),
        "last", new Builtin(Builtins::last),
        "rest", new Builtin(Builtins::rest),
        "push", new Builtin(Builtins::push)
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

    private static MonkeyObject last(MonkeyObject... args) {
        if (args.length != 1) {
            return Evaluator.newError("wrong number of arguments. got=%d, want=1", args.length);
        }

        return switch (args[0]) {
            case Array a -> a.elements().isEmpty() ? NULL : a.elements().getLast();
            default -> Evaluator.newError("argument to 'last' not supported, got %s", args[0].type());
        };
    }

    private static MonkeyObject rest(MonkeyObject... args) {
        if (args.length != 1) {
            return Evaluator.newError("wrong number of arguments. got=%d, want=1", args.length);
        }

        if (!(args[0] instanceof Array(List<MonkeyObject> elements))) {
            return Evaluator.newError("argument to 'rest' not supported, got %s", args[0].type());
        }

        if (elements.isEmpty()) {
            return NULL;
        }

        return new Array(elements.subList(1, elements.size()));
    }

    private static MonkeyObject push(MonkeyObject... args) {
        if (args.length != 2) {
            return Evaluator.newError("wrong number of arguments. got=%d, want=2", args.length);
        }

        if (!(args[0] instanceof Array(List<MonkeyObject> elements))) {
            return Evaluator.newError("first argument to 'push' must be ARRAY, got %s", args[0].type());
        }

        var newList = new ArrayList<>(elements);

        newList.add(args[1]);

        return new Array(Collections.unmodifiableList(newList));
    }

    public static Builtin get(String name) {
        return builtins.get(name);
    }
}
