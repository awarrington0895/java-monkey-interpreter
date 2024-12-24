package com.warrington.monkey.object;

public record ReturnValue(
    MonkeyObject value
) implements MonkeyObject {
    @Override
    public ObjectType type() {
        return ObjectType.RETURN_VALUE;
    }

    @Override
    public String inspect() {
        return value.inspect();
    }
}
