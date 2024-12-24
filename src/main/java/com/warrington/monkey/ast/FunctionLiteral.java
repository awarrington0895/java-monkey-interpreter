package com.warrington.monkey.ast;

import com.warrington.monkey.token.Token;

import java.util.ArrayList;
import java.util.List;

public record FunctionLiteral(
    Token token,
    List<Identifier> parameters,
    BlockStatement body
) implements Expression {

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String toString() {
        var b = new StringBuilder();
        var params = new ArrayList<String>();

        parameters.forEach(p -> params.add(p.toString()));

        b.append(tokenLiteral());
        b.append("(");
        b.append(String.join(",", params));
        b.append(")");
        b.append(body.toString());

        return b.toString();
    }
}
