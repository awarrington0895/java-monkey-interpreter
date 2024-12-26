package com.warrington.monkey.evaluator;

import com.warrington.monkey.object.Builtin;
import com.warrington.monkey.object.Int;
import com.warrington.monkey.object.MonkeyObject;
import com.warrington.monkey.object.Str;

import java.util.Map;

public class Builtins {
    private static final Map<String, Builtin> builtins = Map.of(
        "len", new Builtin(Builtins::len)
    );

    private static MonkeyObject len(MonkeyObject... args) {
        if (args.length != 1) {
            return Evaluator.newError("wrong number of arguments. got=%d, want=1", args.length);
        }

        return switch (args[0]) {
            case Str s -> new Int(s.value().length());
            default -> Evaluator.newError("argument to 'len' not supported, got %s", args[0].type());
        };
    }

    public static Builtin get(String name) {
        return builtins.get(name);
    }
}
