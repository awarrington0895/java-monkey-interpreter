package com.warrington.parser;

import com.warrington.ast.Identifier;
import com.warrington.ast.LetStatement;
import com.warrington.ast.Program;
import com.warrington.ast.Statement;
import com.warrington.lexer.Lexer;
import com.warrington.token.Token;
import com.warrington.token.TokenType;

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
        final var program = new Program();

        while (curToken.type() != TokenType.EOF) {
            Statement stmt = parseStatement();

            if (stmt != null) {
                program.addStatement(stmt);
            }

            nextToken();
        }

        return program;
    }

    private Statement parseStatement() {
        switch (curToken.type()) {
            case LET:
                return parseLetStatement();
            default:
                return null;
        }
    }

    private Statement parseLetStatement() {
        final var stmt = new LetStatement(curToken);

        if (!expectPeek(TokenType.IDENT)) {
            return null;
        }

        stmt.setName(new Identifier(curToken));

        if (!expectPeek(TokenType.ASSIGN)) {
            return null;
        }

        // TODO: We're skipping the expressions until we encounter a semicolon
        while (!curTokenIs(TokenType.SEMICOLON)) {
            nextToken();
        }

        return stmt;
    }

    private boolean curTokenIs(TokenType expectedType) {
        return curToken.type() == expectedType;
    }

    private boolean peekTokenIs(TokenType expectedType) {
        return peekToken.type() == expectedType;
    }

    private boolean expectPeek(TokenType expectedType) {
        if (peekTokenIs(expectedType)) {
            nextToken();
            return true;
        } else {
            return false;
        }
    }
}
