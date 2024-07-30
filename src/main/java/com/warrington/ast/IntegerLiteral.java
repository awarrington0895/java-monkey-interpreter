package com.warrington.ast;

import com.warrington.token.Token;

public class IntegerLiteral implements Expression {
    private Token token;
    private int value;

    public IntegerLiteral(Token token, int value) {
        this.token = token;
        this.value = value;
    }


    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String toString() {
        return token.literal();
    }

    public int value() {
        return value;
    }
    
}
