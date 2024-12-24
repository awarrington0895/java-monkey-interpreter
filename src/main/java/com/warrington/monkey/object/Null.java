package com.warrington.monkey.object;

public record Null() implements MonkeyObject {
    @Override
    public ObjectType type() {
        return ObjectType.NULL;
    }

    @Override
    public String inspect() {
        return "null";
    }
}
