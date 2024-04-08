package com.warrington.ast;

import com.warrington.token.Token;

public class LetStatement implements Statement {
    private Token token;
    private Identifier name;
    private Expression value;

    public LetStatement(Token token) {
        this.token = token;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String toString() {
        final var builder = new StringBuilder();

        builder.append(tokenLiteral() + " ");
        builder.append(name.toString());
        builder.append(" = ");

        if (value != null) {
            builder.append(value.toString());
        }

        builder.append(";");

        return builder.toString();
    }

    public Identifier getName() {
        return this.name;
    }

    public void setName(Identifier identifier) {
        this.name = identifier;
    }

    public void setValue(Expression value) {
        this.value = value;
    }

}