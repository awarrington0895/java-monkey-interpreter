package com.warrington.monkey.ast;

import com.warrington.monkey.token.Token;

public record LetStatement(
    Token token,
    Identifier name,
    Expression value
) implements Statement {

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String toString() {
        final var builder = new StringBuilder();

        builder.append(tokenLiteral()).append(" ");
        builder.append(name.toString());
        builder.append(" = ");

        if (value != null) {
            builder.append(value);
        }

        builder.append(";");

        return builder.toString();
    }
}