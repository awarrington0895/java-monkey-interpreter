package com.warrington.monkey.object;

public record Builtin(BuiltinFunction func) implements MonkeyObject {
    @Override
    public ObjectType type() {
        return ObjectType.BUILTIN;
    }

    @Override
    public String inspect() {
        return "builtin function";
    }
}
