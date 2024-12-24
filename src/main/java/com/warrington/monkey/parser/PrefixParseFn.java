package com.warrington.monkey.parser;

import java.util.function.Supplier;

import com.warrington.monkey.ast.Expression;

interface PrefixParseFn extends Supplier<Expression> {}
