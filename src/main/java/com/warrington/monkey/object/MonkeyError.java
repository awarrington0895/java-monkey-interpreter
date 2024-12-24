package com.warrington.monkey.object;

public record MonkeyError(String message) implements MonkeyObject {

    @Override
    public ObjectType type() {
        return ObjectType.ERROR;
    }

    @Override
    public String inspect() {
        return "ERROR: %s".formatted(message);
    }
}
