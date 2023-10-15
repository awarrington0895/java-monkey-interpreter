package com.warrington.lexer;

import static com.warrington.token.TokenType.ASSIGN;
import static com.warrington.token.TokenType.ASTERISK;
import static com.warrington.token.TokenType.BANG;
import static com.warrington.token.TokenType.COMMA;
import static com.warrington.token.TokenType.EOF;
import static com.warrington.token.TokenType.EQ;
import static com.warrington.token.TokenType.GT;
import static com.warrington.token.TokenType.ILLEGAL;
import static com.warrington.token.TokenType.INT;
import static com.warrington.token.TokenType.LPAREN;
import static com.warrington.token.TokenType.LSQUIRLY;
import static com.warrington.token.TokenType.LT;
import static com.warrington.token.TokenType.MINUS;
import static com.warrington.token.TokenType.NOT_EQ;
import static com.warrington.token.TokenType.PLUS;
import static com.warrington.token.TokenType.RPAREN;
import static com.warrington.token.TokenType.RSQUIRLY;
import static com.warrington.token.TokenType.SEMICOLON;
import static com.warrington.token.TokenType.SLASH;

import com.warrington.token.Token;
import com.warrington.token.TokenType;

class Lexer {

    private final String input;

    private int position;

    private int readPosition;

    private char ch;

    public Lexer(String input) {
        this.input = input;

        readChar();
    }

    public Token nextToken() {
        Token token;

        skipWhitespace();

        switch (ch) {
            case '=':
                if (peekChar() == '=') {
                    final char firstEq = ch;

                    readChar();

                    final String literal = "" + firstEq + ch;

                    token = new Token(EQ, literal);
                    break;
                } else {
                    token = new Token(ASSIGN, ch);
                    break;
                }
            case ';':
                token = new Token(SEMICOLON, ch);
                break;
            case '(':
                token = new Token(LPAREN, ch);
                break;
            case ')':
                token = new Token(RPAREN, ch);
                break;
            case ',':
                token = new Token(COMMA, ch);
                break;
            case '+':
                token = new Token(PLUS, ch);
                break;
            case '{':
                token = new Token(LSQUIRLY, ch);
                break;
            case '}':
                token = new Token(RSQUIRLY, ch);
                break;
            case '-':
                token = new Token(MINUS, ch);
                break;
            case '!':
                if (peekChar() == '=') {
                    final char bang = ch;

                    readChar();

                    final String literal = "" + bang + ch;

                    token = new Token(NOT_EQ, literal);
                    break;
                } else {
                    token = new Token(BANG, ch);
                    break;
                }
            case '*':
                token = new Token(ASTERISK, ch);
                break;
            case '/':
                token = new Token(SLASH, ch);
                break;
            case '<':
                token = new Token(LT, ch);
                break;
            case '>':
                token = new Token(GT, ch);
                break;
            case '\0':
                token = new Token(EOF, "");
                break;
            default:
                if (isLetter(ch)) {
                    final String identifier = readIdentifier();

                    return new Token(TokenType.lookupIdent(identifier), identifier);
                } else if (isDigit(ch)) {
                    return new Token(INT, readNumber());
                } else {
                    token = new Token(ILLEGAL, ch);
                    break;
                }
        }

        readChar();

        return token;
    }

    private char peekChar() {
        if (readPosition >= input.length()) {
            return 0;
        } else {
            return input.charAt(readPosition);
        }
    }

    private String readIdentifier() {
        final int startPosition = position;

        while (isLetter(ch)) {
            readChar();
        }

        return input.substring(startPosition, position);
    }

    private String readNumber() {
        final int startPosition = position;

        while (isDigit(ch)) {
            readChar();
        }

        return input.substring(startPosition, position);
    }

    private boolean isDigit(char ch) {
        return '0' <= ch && ch <= '9';
    }

    private boolean isLetter(char ch) {
        return 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z' || ch == '_';
    }

    private void readChar() {
        if (readPosition >= input.length()) {
            ch = '\0';
        } else {
            ch = input.charAt(readPosition);
        }

        position = readPosition;
        readPosition += 1;
    }

    private void skipWhitespace() {
        while (isWhitespace(ch)) {
            readChar();
        }
    }

    private boolean isWhitespace(char ch) {
        return ch == ' '
                || ch == '\t'
                || ch == '\n'
                || ch == '\r';
    }

}
