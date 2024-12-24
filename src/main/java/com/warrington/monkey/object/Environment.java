package com.warrington.monkey.object;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, MonkeyObject> store = new HashMap<>();

    public MonkeyObject get(String key) {
        return store.get(key);
    }

    public MonkeyObject set(String key, MonkeyObject object) {
        store.put(key, object);

        return object;
    }
}
