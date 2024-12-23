package com.warrington.monkey.parser;

import com.warrington.monkey.ast.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.warrington.monkey.lexer.Lexer;
import com.warrington.monkey.token.Token;
import com.warrington.monkey.token.TokenType;

import static com.warrington.monkey.token.TokenType.*;

class Parser {
    private static final Map<TokenType, Precedence> precedences = Map.of(
        EQ,       Precedence.EQUALS,
        NOT_EQ,   Precedence.EQUALS,
        LT,       Precedence.LESSGREATER,
        GT,       Precedence.LESSGREATER,
        PLUS,     Precedence.SUM,
        MINUS,    Precedence.SUM,
        SLASH,    Precedence.PRODUCT,
        ASTERISK, Precedence.PRODUCT
    );

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
        registerPrefix(TRUE, this::parseBoolean);
        registerPrefix(FALSE, this::parseBoolean);

        registerInfix(PLUS, this::parseInfixExpression);
        registerInfix(MINUS, this::parseInfixExpression);
        registerInfix(SLASH, this::parseInfixExpression);
        registerInfix(ASTERISK, this::parseInfixExpression);
        registerInfix(EQ, this::parseInfixExpression);
        registerInfix(NOT_EQ, this::parseInfixExpression);
        registerInfix(LT, this::parseInfixExpression);
        registerInfix(GT, this::parseInfixExpression);

        nextToken();
        nextToken();
    }

    void nextToken() {
        curToken = peekToken;
        peekToken = lexer.nextToken();
    }

    Program parseProgram() {
        final var program = new Program();

        while (curToken.type() != EOF) {
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

    private Precedence peekPrecedence() {
        return precedences.getOrDefault(peekToken.type(), Precedence.LOWEST);
    }

    private Precedence curPrecedence() {
        return precedences.getOrDefault(curToken.type(), Precedence.LOWEST);
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

        if (!expectPeek(IDENT)) {
            return null;
        }

        stmt.setName(new Identifier(curToken));

        if (!expectPeek(ASSIGN)) {
            return null;
        }

        // TODO: We're skipping the expressions until we encounter a semicolon
        while (!curTokenIs(SEMICOLON)) {
            nextToken();
        }

        return stmt;
    }

    private Expression parseBoolean() {
        return new MonkeyBoolean(curToken, curTokenIs(TRUE));
    }

    private Statement parseReturnStatement() {
        final var stmt = new ReturnStatement(curToken);

        nextToken();

        while (!curTokenIs(SEMICOLON)) {
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

        Expression leftExpression = prefix.get();

        while (!peekTokenIs(SEMICOLON) && precedence.ordinal() < peekPrecedence().ordinal()) {
            InfixParseFn infix = infixParseFns.get(peekToken.type());

            if (infix == null) {
                return leftExpression;
            }

            nextToken();

            leftExpression = infix.apply(leftExpression);
        }

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

    private Expression parseInfixExpression(Expression left) {
        var expression = new InfixExpression(curToken, curToken.literal(), left);

        Precedence precedence = curPrecedence();

        nextToken();

        expression.setRight(parseExpression(precedence));

        return expression;
    }
}
