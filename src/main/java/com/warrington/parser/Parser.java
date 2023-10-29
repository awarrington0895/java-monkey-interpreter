package com.warrington.parser;

import com.warrington.ast.Program;
import com.warrington.lexer.Lexer;
import com.warrington.token.Token;

class Parser {
    private Lexer lexer;

    Token curToken;
    Token peekToken;

    public Parser(Lexer lexer) {
        this.lexer = lexer;

        nextToken();
        nextToken();
    }

    void nextToken() {
        curToken = peekToken;
        peekToken = lexer.nextToken();
    }

    Program parseProgram() {
        return null;
    }
}
