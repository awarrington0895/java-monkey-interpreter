package com.warrington.monkey.parser;

import java.util.function.Function;

import com.warrington.monkey.ast.Expression;

public interface InfixParseFn extends Function<Expression, Expression> {
    
}
