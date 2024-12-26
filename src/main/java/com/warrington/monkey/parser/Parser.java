package com.warrington.monkey.parser;

import com.warrington.monkey.ast.*;
import com.warrington.monkey.lexer.Lexer;
import com.warrington.monkey.token.Token;
import com.warrington.monkey.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.warrington.monkey.token.TokenType.*;

public class Parser {
    private static final Map<TokenType, Precedence> precedences = Map.of(EQ, Precedence.EQUALS, NOT_EQ, Precedence.EQUALS, LT, Precedence.LESSGREATER, GT, Precedence.LESSGREATER, PLUS, Precedence.SUM, MINUS, Precedence.SUM, SLASH, Precedence.PRODUCT, ASTERISK, Precedence.PRODUCT, LPAREN, Precedence.CALL);

    private final Lexer lexer;
    private final List<String> errors = new ArrayList<>();

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
        registerPrefix(LPAREN, this::parseGroupedExpression);
        registerPrefix(IF, this::parseIfExpression);
        registerPrefix(FUNCTION, this::parseFunctionLiteral);
        registerPrefix(STRING, this::parseStringLiteral);

        registerInfix(PLUS, this::parseInfixExpression);
        registerInfix(MINUS, this::parseInfixExpression);
        registerInfix(SLASH, this::parseInfixExpression);
        registerInfix(ASTERISK, this::parseInfixExpression);
        registerInfix(EQ, this::parseInfixExpression);
        registerInfix(NOT_EQ, this::parseInfixExpression);
        registerInfix(LT, this::parseInfixExpression);
        registerInfix(GT, this::parseInfixExpression);
        registerInfix(LPAREN, this::parseCallExpression);

        nextToken();
        nextToken();
    }

    void nextToken() {
        curToken = peekToken;
        peekToken = lexer.nextToken();
    }

    public Program parseProgram() {
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

    public List<String> errors() {
        return List.copyOf(this.errors);
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
        assert curTokenIs(LET) : "Let statements should start with 'let'. got='%s'".formatted(curToken.literal());

        final Token startToken = curToken;

        if (!expectPeek(IDENT)) {
            return null;
        }

        final var name = new Identifier(curToken);

        if (!expectPeek(ASSIGN)) {
            return null;
        }

        nextToken();

        final Expression value = parseExpression(Precedence.LOWEST);

        if (peekTokenIs(SEMICOLON)) {
            nextToken();
        }

        return new LetStatement(startToken, name, value);
    }

    private Expression parseGroupedExpression() {
        nextToken();

        Expression exp = parseExpression(Precedence.LOWEST);

        if (!expectPeek(RPAREN)) {
            return null;
        }

        return exp;
    }

    private Expression parseBoolean() {
        return new MonkeyBoolean(curToken, curTokenIs(TRUE));
    }

    private Statement parseReturnStatement() {
        assert curTokenIs(RETURN) : "Return statements should start with 'return'. got='%s'".formatted(curToken.literal());

        final Token startToken = curToken;

        nextToken();

        final Expression returnValue = parseExpression(Precedence.LOWEST);

        if (peekTokenIs(SEMICOLON)) {
            nextToken();
        }

        return new ReturnStatement(startToken, returnValue);
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

    private Expression parseStringLiteral() {
        assert curTokenIs(STRING) : "String literal should be STRING token. got=%s".formatted(curToken.type());

        return new StringLiteral(curToken, curToken.literal());
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

    private Expression parseFunctionLiteral() {
        assert curTokenIs(FUNCTION) : "Functions should start with 'fn'. got='%s'".formatted(curToken.literal());

        final var startToken = curToken;

        if (!expectPeek(LPAREN)) {
            return null;
        }

        List<Identifier> parameters = parseFunctionParameters();

        if (!expectPeek(LSQUIRLY)) {
            return null;
        }

        BlockStatement body = parseBlockStatement();

        return new FunctionLiteral(startToken, parameters, body);
    }

    private List<Identifier> parseFunctionParameters() {
        assert curTokenIs(LPAREN) : "Function parameters should start with '('. got='%s'".formatted(curToken.literal());

        var parameters = new ArrayList<Identifier>();

        if (peekTokenIs(RPAREN)) {
            nextToken();
            return parameters;
        }

        nextToken();

        var param = new Identifier(curToken, curToken.literal());
        parameters.add(param);

        while (peekTokenIs(COMMA)) {
            nextToken();
            nextToken();

            var nextParam = new Identifier(curToken, curToken.literal());
            parameters.add(nextParam);
        }

        if (!expectPeek(RPAREN)) {
            return null;
        }

        return parameters;
    }

    private Expression parseCallExpression(Expression function) {
        return new CallExpression(curToken, function, parseCallArguments());
    }

    private List<Expression> parseCallArguments() {
        assert curTokenIs(LPAREN) : "Call expressions should start with '('. got='%s'.".formatted(curToken.literal());

        var args = new ArrayList<Expression>();

        if (peekTokenIs(RPAREN)) {
            nextToken();
            return args;
        }

        nextToken();

        args.add(parseExpression(Precedence.LOWEST));

        while (peekTokenIs(COMMA)) {
            nextToken();
            nextToken();

            args.add(parseExpression(Precedence.LOWEST));
        }

        if (!expectPeek(RPAREN)) {
            return null;
        }

        return args;
    }

    private Expression parseIfExpression() {
        assert curTokenIs(IF) : "If expressions should start with 'if'. got='%s'".formatted(curToken.literal());

        final var ifToken = curToken;

        if (!expectPeek(LPAREN)) {
            return null;
        }

        nextToken();

        Expression condition = parseExpression(Precedence.LOWEST);

        if (!expectPeek(RPAREN)) {
            return null;
        }

        if (!expectPeek(LSQUIRLY)) {
            return null;
        }

        BlockStatement consequence = parseBlockStatement();

        BlockStatement alternative = null;
        if (peekTokenIs(ELSE)) {
            nextToken();

            if (!expectPeek(LSQUIRLY)) {
                return null;
            }

            alternative = parseBlockStatement();
        }

        return new IfExpression(ifToken, condition, consequence, alternative);
    }

    private BlockStatement parseBlockStatement() {
        assert curTokenIs(LSQUIRLY) : "Block statements should start with '{'. got='%s'".formatted(curToken.literal());
        final var startToken = curToken;

        var statements = new ArrayList<Statement>();

        nextToken();

        while (!curTokenIs(RSQUIRLY) && !curTokenIs(EOF)) {
            Statement stmt = parseStatement();

            if (stmt != null) {
                statements.add(stmt);
            }

            nextToken();
        }

        return new BlockStatement(startToken, statements);
    }

    private Expression parseInfixExpression(Expression left) {
        var expression = new InfixExpression(curToken, curToken.literal(), left);

        Precedence precedence = curPrecedence();

        nextToken();

        expression.setRight(parseExpression(precedence));

        return expression;
    }
}
