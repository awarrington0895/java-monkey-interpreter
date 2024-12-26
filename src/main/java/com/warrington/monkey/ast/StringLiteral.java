package com.warrington.monkey.ast;

import com.warrington.monkey.token.Token;

public record StringLiteral(
    Token token,
    String value
) implements Expression {

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String toString() {
        return value;
    }
}
