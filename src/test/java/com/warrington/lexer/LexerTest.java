package com.warrington.lexer;

// import static com.warrington.token.TokenType.ASSIGN;
// import static com.warrington.token.TokenType.COMMA;
// import static com.warrington.token.TokenType.EOF;
// import static com.warrington.token.TokenType.FUNCTION;
// import static com.warrington.token.TokenType.IDENT;
// import static com.warrington.token.TokenType.INT;
// import static com.warrington.token.TokenType.LET;
// import static com.warrington.token.TokenType.LPAREN;
// import static com.warrington.token.TokenType.LSQUIRLY;
// import static com.warrington.token.TokenType.PLUS;
// import static com.warrington.token.TokenType.RPAREN;
// import static com.warrington.token.TokenType.RSQUIRLY;
// import static com.warrington.token.TokenType.SEMICOLON;
import static com.warrington.token.TokenType.ASSIGN;
import static com.warrington.token.TokenType.ASTERISK;
import static com.warrington.token.TokenType.BANG;
import static com.warrington.token.TokenType.COMMA;
import static com.warrington.token.TokenType.EOF;
import static com.warrington.token.TokenType.FUNCTION;
import static com.warrington.token.TokenType.GT;
import static com.warrington.token.TokenType.IDENT;
import static com.warrington.token.TokenType.INT;
import static com.warrington.token.TokenType.LET;
import static com.warrington.token.TokenType.LPAREN;
import static com.warrington.token.TokenType.LSQUIRLY;
import static com.warrington.token.TokenType.LT;
import static com.warrington.token.TokenType.MINUS;
import static com.warrington.token.TokenType.PLUS;
import static com.warrington.token.TokenType.RPAREN;
import static com.warrington.token.TokenType.RSQUIRLY;
import static com.warrington.token.TokenType.SEMICOLON;
import static com.warrington.token.TokenType.SLASH;
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
