package com.warrington.monkey.ast;

import com.warrington.monkey.token.Token;

import java.util.List;

public record BlockStatement(
    Token token,
    List<Statement> statements
) implements Statement {

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        statements.forEach(statement -> builder.append(statement.toString()));

        return builder.toString();
    }
}
