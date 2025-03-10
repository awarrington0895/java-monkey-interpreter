package com.warrington.monkey.ast;

import com.warrington.monkey.token.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record HashLiteral(
    Token token,
    Map<Expression, Expression> pairs
) implements Expression {
    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String toString() {
        List<String> stringPairs = new ArrayList<>();

        pairs.forEach((key, value) -> {
            stringPairs.add("%s: %s".formatted(key.toString(), value.toString()));
        });

        return "{ %s }".formatted(String.join(",", stringPairs));
    }
}
