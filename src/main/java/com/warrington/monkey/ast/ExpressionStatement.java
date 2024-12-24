package com.warrington.monkey.ast;

import com.warrington.monkey.token.Token;

public class ExpressionStatement implements Statement {
    private Token token;
    private Expression expression;

    public ExpressionStatement(Token token, Expression expression) {
        this.token = token;
        this.expression = expression;
    }

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

    public Expression getExpression() {
        return expression;
    }
}
