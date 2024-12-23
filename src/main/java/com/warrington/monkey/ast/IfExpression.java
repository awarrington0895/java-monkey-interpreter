package com.warrington.monkey.ast;

import com.warrington.monkey.token.Token;

public record IfExpression(
    Token token,
    Expression condition,
    BlockStatement consequence,
    BlockStatement alternative
) implements Expression {

    public IfExpression(Token token, Expression condition, BlockStatement consequence) {
        this(token, condition, consequence, null);
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String toString() {
        var b = new StringBuilder();

        b.append("if");
        b.append(condition);
        b.append(" ");
        b.append(consequence);

        if (alternative != null) {
            b.append("else ");
            b.append(alternative);
        }

        return b.toString();
    }
}
