package com.warrington.monkey.token;

public record Token(TokenType type, String literal) {
    public Token(TokenType type, char literal) {
        this(type, String.valueOf(literal));
    }
}