package com.warrington.monkey.ast;

import com.warrington.monkey.token.Token;

public class Identifier implements Expression {
    private Token token;
    private String value;

    public String value() {
        return value;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String toString() {
        return value;
    }

    public Identifier(Token token) {
        this(token, token.literal());
    }

    public Identifier(Token token, String value) {
        this.token = token;
        this.value = value;
    }
}