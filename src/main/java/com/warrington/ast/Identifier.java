package com.warrington.ast;

import com.warrington.token.Token;

public class Identifier implements Expression {
    private Token token;
    private String value;

    public String getValue() {
        return value;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    public Identifier(Token token) {
        this(token, token.literal());
    }

    public Identifier(Token token, String value) {
        this.token = token;
        this.value = value;
    }
}