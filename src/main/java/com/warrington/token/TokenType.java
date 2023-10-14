package com.warrington.token;

import java.util.Map;

public enum TokenType {
    ILLEGAL("ILLEGAL"),
    EOF("EOF"),
    IDENT("IDENT"),
    INT("INT"),
    ASSIGN("="),
    PLUS("+"),
    COMMA(","),
    SEMICOLON(";"),
    LPAREN("("),
    RPAREN(")"),
    LSQUIRLY("{"),
    RSQUIRLY("}"),

    // Keywords
    FUNCTION("FUNCTION"),
    LET("LET");

    public String literalValue;

    public static TokenType lookupIdent(String ident) {
        return keywords.getOrDefault(ident, IDENT);
    }

    private TokenType(String literalValue) {
        this.literalValue = literalValue;
    }

    private static final Map<String, TokenType> keywords = Map.of(
        "fn", FUNCTION,
        "let", LET
    );
}
