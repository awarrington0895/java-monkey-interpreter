package com.warrington.parser;

import com.warrington.ast.Expression;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.warrington.ast.Identifier;
import com.warrington.ast.LetStatement;
import com.warrington.ast.Program;
import com.warrington.ast.ReturnStatement;
import com.warrington.ast.Statement;
import com.warrington.lexer.Lexer;
import com.warrington.token.Token;
import com.warrington.token.TokenType;

class Parser {
    private Lexer lexer;
    private List<String> errors = new ArrayList<>();

    Token curToken;
    Token peekToken;

    Map<Token, Supplier<Expression>> prefixParseFns;
    Map<Token, Function<Expression, Expression>> infixParseFns;

    public Parser(Lexer lexer) {
        this.lexer = lexer;

        this.prefixParseFns = new HashMap<>();
        this.infixParseFns = new HashMap<>();

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

    List<String> errors() {
        return this.errors;
    }

    void peekError(TokenType tokenType) {
        final var message = "expected next token to be %s, got %s".formatted(tokenType, peekToken.type());

        errors.add(message);
    }

    private Statement parseStatement() {
        return switch (curToken.type()) {
            case LET -> parseLetStatement();
            case RETURN -> parseReturnStatement();
            default -> null;
        };
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

    private Statement parseReturnStatement() {
        final var stmt = new ReturnStatement(curToken);

        nextToken();

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
            peekError(expectedType);
            return false;
        }
    }

    private void registerPrefix(Token token, Supplier<Expression> fn) {
        this.prefixParseFns.put(token, fn);
    }

    private void registerInfix(Token token, Function<Expression, Expression> fn) {
        this.infixParseFns.put(token, fn);
    }
}
