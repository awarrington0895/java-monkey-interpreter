package com.warrington.monkey.object;

public record Bool(boolean value) implements MonkeyObject {
    @Override
    public ObjectType type() {
        return ObjectType.BOOLEAN;
    }

    @Override
    public String inspect() {
        return "%s".formatted(value);
    }

    public HashKey hashKey() {
        return new HashKey(ObjectType.BOOLEAN, Boolean.hashCode(this.value));
    }
}
