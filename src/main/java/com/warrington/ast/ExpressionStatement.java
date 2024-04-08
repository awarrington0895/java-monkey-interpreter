package com.warrington.ast;

import com.warrington.token.Token;

public class ExpressionStatement implements Statement {
    private Token token;
    private Expression expression;

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String toString() {
        if (expression != null) {
            return expression.toString();
        }

        return "";
    }
}
