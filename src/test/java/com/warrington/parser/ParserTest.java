package com.warrington.parser;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.warrington.ast.LetStatement;
import com.warrington.ast.Program;
import com.warrington.ast.Statement;
import com.warrington.lexer.Lexer;

class ParserTest {

    @Test
    void testLetStatements() {
        final var input = """
                let x = 5;
                let y = 10;
                let foobar = 838383;
                """;

        final var lexer = new Lexer(input);

        final var parser = new Parser(lexer);

        final Program program = parser.parseProgram();

        if (program == null) {
            fail("parseProgram() returned null");
        }

        if (program.getStatements().size() != 3) {
            fail("program.getStatements() does not contain 3 statements. got=%d"
                    .formatted(program.getStatements().size()));
        }

        final var expectedIdentifiers = List.of(
                "x",
                "y",
                "foobar");

        for (int i = 0; i < expectedIdentifiers.size(); i++) {
            final var statement = program.getStatements().get(i);

            if (!testLetStatement(statement, expectedIdentifiers.get(i))) {
                return;
            }

        }

    }

    boolean testLetStatement(Statement statement, String name) {
        if (!statement.tokenLiteral().equals("let")) {
            fail("statement.tokenLiteral not 'let'. got=%s".formatted(statement.tokenLiteral()));
            return false;
        }

        if (statement instanceof LetStatement letStmt) {
            if (!letStmt.getName().getValue().equals(name)) {
                fail("letStmt.getName().getValue() not '%s'. got=%s".formatted(name, letStmt.getName().getValue()));
                return false;
            }

            if (!letStmt.getName().tokenLiteral().equals(name)) {
                fail("letStmt.getName().tokenLiteral() not '%s'. got=%s".formatted(name,
                        letStmt.getName().tokenLiteral()));
                return false;
            }

            return true;
        } else {
            fail("statement is not a LetStatement. got=%s".formatted(statement.getClass()));
            return false;
        }

    }
}
