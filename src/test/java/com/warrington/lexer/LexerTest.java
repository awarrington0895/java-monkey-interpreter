package com.warrington.lexer;

import org.junit.jupiter.api.Test;

import com.warrington.token.Token;

import static org.assertj.core.api.Assertions.assertThat;
import static com.warrington.token.TokenType.*;

import java.util.List;

class LexerTest {
    final String input = "=+(){},;";

    @Test
    void nextToken() {
        final var lexer = new Lexer(input);

        final var expectedTokens = List.of(
            new Token(ASSIGN, "="),
            new Token(PLUS, "+"),
            new Token(LPAREN, "("),
            new Token(RPAREN, ")"),
            new Token(LSQUIRLY, "{"),
            new Token(RSQUIRLY, "}"),
            new Token(COMMA, ","),
            new Token(SEMICOLON, ";")
        );


        Token token = null;

        for (int i = 0; i < expectedTokens.size(); i++) {
            token = lexer.nextToken();
            Token expectedToken = expectedTokens.get(i);

            assertThat(token.type())
                .withFailMessage("tests[%d] - tokentype wrong. expected=%s, got=%s".formatted(i, expectedToken.type(), token.type()))
                .isEqualTo(expectedToken.type());

            assertThat(token.literal())
                .withFailMessage("tests[%d] - literal wrong. expected=%s, got=%s".formatted(i, expectedToken.literal(), token.literal()))
                .isEqualTo(expectedToken.literal());
        }
    }
}
