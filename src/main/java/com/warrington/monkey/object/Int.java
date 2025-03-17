package com.warrington.monkey.object;

/**
 * Representation of a 64-bit integer in Monkey
 *
 * @param value this is a long since it is intended to be 64 bit
 */
public record Int(long value) implements MonkeyObject, Hashable {

    @Override
    public ObjectType type() {
        return ObjectType.INTEGER;
    }

    @Override
    public String inspect() {
        return "%d".formatted(value);
    }

    public HashKey hashKey() {
        return new HashKey(ObjectType.INTEGER, Long.hashCode(this.value));
    }
}
