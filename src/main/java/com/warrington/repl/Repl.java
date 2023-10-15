package com.warrington.repl;

import java.util.Scanner;

import com.warrington.lexer.Lexer;
import com.warrington.token.Token;
import com.warrington.token.TokenType;

public class Repl {
    private static final String PROMPT = ">> ";

    private Repl() {
    }

    public static void start() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print(PROMPT);

                final String line = scanner.nextLine();

                if (line == null) {
                    return;
                }

                final var lexer = new Lexer(line);

                for (Token token = lexer.nextToken(); token.type() != TokenType.EOF; token = lexer.nextToken()) {
                    System.out.println(token);
                }

            }
        }
    }
}
