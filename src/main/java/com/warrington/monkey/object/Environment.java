package com.warrington.monkey.object;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, MonkeyObject> store = new HashMap<>();
    private Environment outer = null;

    public MonkeyObject get(String name) {
        MonkeyObject candidate = store.get(name);

        if (candidate == null && outer != null) {
            candidate = outer.get(name);
        }

        return candidate;
    }

    public MonkeyObject set(String key, MonkeyObject object) {
        store.put(key, object);

        return object;
    }

    public Environment newEnclosed() {
        final var inner = new Environment();

        inner.outer = this;

        return inner;
    }
}
