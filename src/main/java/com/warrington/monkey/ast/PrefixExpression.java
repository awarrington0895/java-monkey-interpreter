package com.warrington.monkey.ast;

import com.warrington.monkey.token.Token;

public class PrefixExpression implements Expression {
    private Token token;
    private String operator;
    private Expression right;

    public PrefixExpression(Token token, String operator) {
        this.token = token;
        this.operator = operator;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String toString() {
        return "(%s%s)".formatted(operator, right.toString());
    }

    public String operator() {
        return this.operator;
    }

    public Expression right() {
        return this.right;
    }

    public void setRight(Expression e) {
        this.right = e;
    }
    
}
