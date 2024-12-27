package com.warrington.monkey.object;

import java.util.ArrayList;
import java.util.List;

public record Array(
    List<MonkeyObject> elements
) implements MonkeyObject {
    @Override
    public ObjectType type() {
        return ObjectType.ARRAY;
    }

    @Override
    public String inspect() {
        var elems = new ArrayList<String>();

        elements.forEach(elem -> elems.add(elem.inspect()));

        return "[%s]".formatted(String.join(", ", elems));
    }
}
