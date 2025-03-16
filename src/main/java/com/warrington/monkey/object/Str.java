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

    public HashKey hashKey() {
       return new HashKey(ObjectType.STRING, this.value.hashCode());
    }
}
