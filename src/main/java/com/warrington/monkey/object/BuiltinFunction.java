package com.warrington.monkey.object;

import java.util.function.Function;

public interface BuiltinFunction extends Function<MonkeyObject[], MonkeyObject> {
    @Override
    MonkeyObject apply(MonkeyObject... args);
}
