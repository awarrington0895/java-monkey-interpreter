package com.warrington.ast;

import com.warrington.token.Token;

public class LetStatement implements Statement {
    private Token token;
    private Identifier name;
    private Expression value;

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    public Identifier getName() {
        return this.name;
    }

}