package com.warrington.monkey.object;

public record Int(int value) implements MonkeyObject {

    @Override
    public ObjectType type() {
        return ObjectType.INTEGER;
    }

    @Override
    public String inspect() {
        return "%d".formatted(value);
    }
}
