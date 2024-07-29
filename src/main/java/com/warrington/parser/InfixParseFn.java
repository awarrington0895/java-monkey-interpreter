package com.warrington.parser;

import java.util.function.Function;

import com.warrington.ast.Expression;

public interface InfixParseFn extends Function<Expression, Expression> {
    
}
