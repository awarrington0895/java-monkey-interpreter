package com.warrington.monkey.ast;

import com.warrington.monkey.token.Token;

import java.util.ArrayList;
import java.util.List;

public record CallExpression(
    Token token,
    Expression function,
    List<Expression> arguments
) implements Expression {

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String toString() {
        var b = new StringBuilder();
        var args = new ArrayList<String>();

        arguments.forEach(arg -> args.add(arg.toString()));

       b.append(function);
       b.append("(");
       b.append(String.join(", ", args));
       b.append(")");

       return b.toString();
    }
}
