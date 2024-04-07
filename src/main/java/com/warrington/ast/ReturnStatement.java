package com.warrington.ast;

import com.warrington.token.Token;

public class ReturnStatement implements Statement {
    private Token token;
    private Expression returnValue;

    public ReturnStatement(Token token) {
        this.token = token;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }
}
