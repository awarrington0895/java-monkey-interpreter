package com.warrington.monkey.token;

import java.util.Map;

public enum TokenType {
    ILLEGAL("ILLEGAL"),
    EOF("EOF"),
    IDENT("IDENT"),
    INT("INT"),
    STRING("STRING"),
    ASSIGN("="),
    PLUS("+"),
    COMMA(","),
    SEMICOLON(";"),
    LPAREN("("),
    RPAREN(")"),
    LSQUIRLY("{"),
    RSQUIRLY("}"),
    MINUS("-"),
    BANG("!"),
    ASTERISK("*"),
    SLASH("/"),
    LT("<"),
    GT(">"),
    EQ("=="),
    NOT_EQ("!="),

    // Keywords
    FUNCTION("FUNCTION"),
    LET("LET"),
    TRUE("TRUE"),
    FALSE("FALSE"),
    IF("IF"),
    ELSE("ELSE"),
    RETURN("RETURN");

    public final String literalValue;

    public static TokenType lookupIdent(String ident) {
        return keywords.getOrDefault(ident, IDENT);
    }

    private TokenType(String literalValue) {
        this.literalValue = literalValue;
    }

    private static final Map<String, TokenType> keywords = Map.of(
            "fn", FUNCTION,
            "let", LET,
            "true", TRUE,
            "false", FALSE,
            "if", IF,
            "else", ELSE,
            "return", RETURN);
}
