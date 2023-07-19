package com.warrington.lexer;

import com.warrington.token.Token;

import static com.warrington.token.TokenType.*;

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

        final Token token = switch (ch) {
            case '=' -> new Token(ASSIGN, ch);
            case ';' -> new Token(SEMICOLON, ch);
            case '(' -> new Token(LPAREN, ch);
            case ')' -> new Token(RPAREN, ch);
            case ',' -> new Token(COMMA, ch);
            case '+' -> new Token(PLUS, ch);
            case '{' -> new Token(LSQUIRLY, ch);
            case '}' -> new Token(RSQUIRLY, ch);
            case '\0' -> new Token(EOF, "");
            default -> new Token(EOF, "");
        };

        // switch (ch) {
        //     case '=':
        //         token = new Token(ASSIGN, ch);
        //         break;
        //     case ';':
        //         token = new Token(SEMICOLON, ch);
        //         break;
        //     case '(':
        //         token = new Token(LPAREN, ch);
        //         break;
        //     case ')':
        //         token = new Token(RPAREN, ch);
        //         break;
        //     case ',':
        //         token = new Token(COMMA, ch);
        //         break;
        //     case '+':
        //         token = new Token(PLUS, ch);
        //         break;
        //     case '{':
        //         token = new Token(LSQUIRLY, ch);
        //         break;
        //     case '}':
        //         token = new Token(RSQUIRLY, ch);
        //         break;
        //     case '\0':
        //         token = new Token(EOF, "");
        // }

        readChar();

        return token;
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

}
