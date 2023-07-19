package com.warrington.token;

public record Token(TokenType type, String literal) {
    public Token(TokenType type, char literal) {
        this(type, String.valueOf(literal));
    }
}