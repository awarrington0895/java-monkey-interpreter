package com.warrington.token;

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

    private TokenType(String literalValue) {
        this.literalValue = literalValue;
    }
}
