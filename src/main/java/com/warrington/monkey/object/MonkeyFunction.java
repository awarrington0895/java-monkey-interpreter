package com.warrington.monkey.object;

import com.warrington.monkey.ast.BlockStatement;
import com.warrington.monkey.ast.Identifier;

import java.util.ArrayList;
import java.util.List;

public record MonkeyFunction(
    List<Identifier> parameters,
    BlockStatement body,
    Environment env
) implements MonkeyObject {

    @Override
    public ObjectType type() {
        return ObjectType.FUNCTION;
    }

    @Override
    public String inspect() {
        final var b = new StringBuilder();
        final var params = new ArrayList<String>();

        for (Identifier ident : parameters) {
            params.add(ident.toString());
        }

        b.append("fn");
        b.append("(");
        b.append(String.join(", ", params));
        b.append(") {\n");
        b.append(body.toString());
        b.append("\n}");

        return b.toString();
    }
}
