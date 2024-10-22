package com.warrington.ast;

import com.warrington.token.Token;

public class InfixExpression implements Expression {
    private Token token;
    private String operator;
    private Expression left;
    private Expression right;

    public InfixExpression(Token token, String operator, Expression left) {
        this.token = token;
        this.operator = operator;
        this.left = left;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String toString() {
        return "(%s %s %s)".formatted(left.toString(), operator, right.toString());
    }

    public String operator() {
        return operator;
    }

    public Expression left() {
        return left;
    }

    public Expression right() {
        return right;
    }

    public void setRight(Expression right) {
        this.right = right;
    } 
}
