package com.warrington.monkey.object;

public record Str(String value) implements MonkeyObject {

    @Override
    public ObjectType type() {
        return ObjectType.STRING;
    }

    @Override
    public String inspect() {
        return value;
    }
}
