package com.warrington.lexer;

import static com.warrington.token.TokenType.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.warrington.token.Token;

class LexerTest {
    final String input = """
            let five = 5;

            let ten = 10;

            let add = fn(x, y) {
                x + y;
            };

            let result = add(five, ten);
            !-/*5;
            5 < 10 > 5;

            if (5 < 10) {
                return true;
            } else {
                return false;
            }

            10 == 10;
            10 != 9;
            """;

    @Test
    void nextToken() {
        final var lexer = new Lexer(input);

        final var expectedTokens = List.of(
                new Token(LET, "let"),
                new Token(IDENT, "five"),
                new Token(ASSIGN, "="),
                new Token(INT, "5"),
                new Token(SEMICOLON, ";"),
                new Token(LET, "let"),
                new Token(IDENT, "ten"),
                new Token(ASSIGN, "="),
                new Token(INT, "10"),
                new Token(SEMICOLON, ";"),
                new Token(LET, "let"),
                new Token(IDENT, "add"),
                new Token(ASSIGN, "="),
                new Token(FUNCTION, "fn"),
                new Token(LPAREN, "("),
                new Token(IDENT, "x"),
                new Token(COMMA, ","),
                new Token(IDENT, "y"),
                new Token(RPAREN, ")"),
                new Token(LSQUIRLY, "{"),
                new Token(IDENT, "x"),
                new Token(PLUS, "+"),
                new Token(IDENT, "y"),
                new Token(SEMICOLON, ";"),
                new Token(RSQUIRLY, "}"),
                new Token(SEMICOLON, ";"),
                new Token(LET, "let"),
                new Token(IDENT, "result"),
                new Token(ASSIGN, "="),
                new Token(IDENT, "add"),
                new Token(LPAREN, "("),
                new Token(IDENT, "five"),
                new Token(COMMA, ","),
                new Token(IDENT, "ten"),
                new Token(RPAREN, ")"),
                new Token(SEMICOLON, ";"),
                new Token(BANG, "!"),
                new Token(MINUS, "-"),
                new Token(SLASH, "/"),
                new Token(ASTERISK, "*"),
                new Token(INT, "5"),
                new Token(SEMICOLON, ";"),
                new Token(INT, "5"),
                new Token(LT, "<"),
                new Token(INT, "10"),
                new Token(GT, ">"),
                new Token(INT, "5"),
                new Token(SEMICOLON, ";"),
                new Token(IF, "if"),
                new Token(LPAREN, "("),
                new Token(INT, "5"),
                new Token(LT, "<"),
                new Token(INT, "10"),
                new Token(RPAREN, ")"),
                new Token(LSQUIRLY, "{"),
                new Token(RETURN, "return"),
                new Token(TRUE, "true"),
                new Token(SEMICOLON, ";"),
                new Token(RSQUIRLY, "}"),
                new Token(ELSE, "else"),
                new Token(LSQUIRLY, "{"),
                new Token(RETURN, "return"),
                new Token(FALSE, "false"),
                new Token(SEMICOLON, ";"),
                new Token(RSQUIRLY, "}"),
                new Token(INT, "10"),
                new Token(EQ, "=="),
                new Token(INT, "10"),
                new Token(SEMICOLON, ";"),
                new Token(INT, "10"),
                new Token(NOT_EQ, "!="),
                new Token(INT, "9"),
                new Token(SEMICOLON, ";"),
                new Token(EOF, ""));

        Token token = null;

        for (int i = 0; i < expectedTokens.size(); i++) {
            token = lexer.nextToken();
            Token expectedToken = expectedTokens.get(i);

            assertThat(token.type())
                    .withFailMessage("tests[%d] - tokentype wrong. expected=%s, got=%s".formatted(i,
                            expectedToken.type(), token.type()))
                    .isEqualTo(expectedToken.type());

            assertThat(token.literal())
                    .withFailMessage("tests[%d] - literal wrong. expected=%s, got=%s".formatted(i,
                            expectedToken.literal(), token.literal()))
                    .isEqualTo(expectedToken.literal());
        }
    }
}
