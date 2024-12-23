package com.warrington.ast;

import com.warrington.token.Token;

public class ReturnStatement implements Statement {
    private Token token;
    private Expression returnValue;

    public ReturnStatement(Token token) {
        this.token = token;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    public Expression returnValue() { return returnValue; }

    @Override
    public String toString() {
        final var builder = new StringBuilder();

        builder.append(tokenLiteral() + " ");

        if (returnValue != null) {
            builder.append(returnValue.toString());
        }

        builder.append(";");

        return builder.toString();
    }
}
