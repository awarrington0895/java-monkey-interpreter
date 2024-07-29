package com.warrington.parser;

import java.util.function.Supplier;

import com.warrington.ast.Expression;

interface PrefixParseFn extends Supplier<Expression> {}
