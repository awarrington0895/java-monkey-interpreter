package com.warrington.monkey.ast;

import com.warrington.monkey.token.Token;

public record IndexExpression(
    Token token, // The '[' token
    Expression left,
    Expression index
) implements Expression {

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String toString() {
        return "(%s[%s])".formatted(left, index);
    }
}
