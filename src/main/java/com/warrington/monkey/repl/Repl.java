package com.warrington.monkey.repl;

import java.util.List;
import java.util.Scanner;

import com.warrington.monkey.ast.Program;
import com.warrington.monkey.evaluator.Evaluator;
import com.warrington.monkey.lexer.Lexer;
import com.warrington.monkey.object.MonkeyObject;
import com.warrington.monkey.parser.Parser;
import com.warrington.monkey.token.Token;
import com.warrington.monkey.token.TokenType;

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
                final var parser = new Parser(lexer);

                final Program program = parser.parseProgram();

                if (!parser.errors().isEmpty()) {
                    printParserErrors(parser.errors());
                    continue;
                }

                final MonkeyObject evaluated = Evaluator.eval(program);

                if (evaluated != null) {
                    System.out.println(evaluated.inspect());
                }
            }
        }
    }

    public static void startParser() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print(PROMPT);

                final String line = scanner.nextLine();

                if (line == null) {
                    return;
                }

                final var lexer = new Lexer(line);
                final var parser = new Parser(lexer);

                final Program program = parser.parseProgram();

                if (!parser.errors().isEmpty()) {
                    printParserErrors(parser.errors());
                    continue;
                }

               System.out.println(program.toString());
            }
        }
    }

    private static void printParserErrors(List<String> errors) {
        System.out.println("Woops!  We ran into some monkey business here!");
        System.out.println(" parser errors:");
        errors.forEach(error -> System.out.printf("\t%s\n", error));
    }

    public static void startLexer() {
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
