package com.warrington.monkey.ast;

import com.warrington.monkey.token.Token;

import java.util.ArrayList;
import java.util.List;

public record ArrayLiteral(
    Token token,
    List<Expression> elements
) implements Expression {

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String toString() {
        final var elems = new ArrayList<String>();

        elements.forEach(elem -> elems.add(elem.toString()));

        return "[" +
            String.join(",", elems) +
            "]";
    }
}
