package com.warrington.parser;

import com.warrington.ast.Expression;
import com.warrington.ast.ExpressionStatement;

import static com.warrington.token.TokenType.BANG;
import static com.warrington.token.TokenType.IDENT;
import static com.warrington.token.TokenType.INT;
import static com.warrington.token.TokenType.MINUS;
import static com.warrington.token.TokenType.SEMICOLON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.warrington.ast.Identifier;
import com.warrington.ast.IntegerLiteral;
import com.warrington.ast.LetStatement;
import com.warrington.ast.PrefixExpression;
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

    Map<TokenType, PrefixParseFn> prefixParseFns;
    Map<TokenType, InfixParseFn> infixParseFns;

    public Parser(Lexer lexer) {
        this.lexer = lexer;

        this.prefixParseFns = new HashMap<>();
        this.infixParseFns = new HashMap<>();

        registerPrefix(IDENT, this::parseIdentifier);
        registerPrefix(INT, this::parseIntegerLiteral);
        registerPrefix(BANG, this::parsePrefixExpression);
        registerPrefix(MINUS, this::parsePrefixExpression);

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
            default -> parseExpressionStatement();
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

    private void registerPrefix(TokenType token, PrefixParseFn fn) {
        this.prefixParseFns.put(token, fn);
    }

    private void registerInfix(TokenType token, InfixParseFn fn) {
        this.infixParseFns.put(token, fn);
    }

    private ExpressionStatement parseExpressionStatement() {
        final var stmt = new ExpressionStatement(curToken, parseExpression(Precedence.LOWEST));

        if (peekTokenIs(SEMICOLON)) {
            nextToken();
        }

        return stmt;
    }

    private Expression parseExpression(Precedence precedence) {
        final PrefixParseFn prefix = prefixParseFns.get(curToken.type());

        if (prefix == null) {
            noPrefixParseFnError(curToken.type());
            return null;
        }

        final var leftExpression = prefix.get();

        return leftExpression;
    }

    private Expression parseIdentifier() {
        return new Identifier(curToken, curToken.literal());
    }

    private Expression parseIntegerLiteral() {
        int value;

        try {
            value = Integer.parseInt(curToken.literal());
        } catch (NumberFormatException ex) {
            errors.add("could not parse %s as integer".formatted(curToken.literal()));

            return null;
        }

        return new IntegerLiteral(curToken, value);
    }

    private void noPrefixParseFnError(TokenType tokenType) {
        var message = "no prefix parse function for %s found".formatted(tokenType);

        errors.add(message);
    }

    private Expression parsePrefixExpression() {
        var expression = new PrefixExpression(curToken, curToken.literal());

        nextToken();

        expression.setRight(parseExpression(Precedence.PREFIX));

        return expression;
    }
}
