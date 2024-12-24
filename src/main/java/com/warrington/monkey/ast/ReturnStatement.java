package com.warrington.monkey.ast;

import com.warrington.monkey.token.Token;

public record ReturnStatement(
    Token token,
    Expression returnValue
) implements Statement {

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    public Expression returnValue() { return returnValue; }

    @Override
    public String toString() {
        final var builder = new StringBuilder();

        builder.append(tokenLiteral()).append(" ");

        if (returnValue != null) {
            builder.append(returnValue.toString());
        }

        builder.append(";");

        return builder.toString();
    }
}
