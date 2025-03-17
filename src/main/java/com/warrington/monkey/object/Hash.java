package com.warrington.monkey.object;

import java.util.ArrayList;
import java.util.Map;

public record Hash(
    Map<HashKey, HashPair> pairs
) implements MonkeyObject {
    @Override
    public ObjectType type() {
        return ObjectType.HASH;
    }

    @Override
    public String inspect() {
        final var stringPairs = new ArrayList<String>();

        pairs.forEach((key, value) -> {
            stringPairs.add("%s: %s".formatted(value.key().inspect(), value.value().inspect()));
        });

        return "{ %s }".formatted(String.join(", ", stringPairs));
    }
}
