package com.warrington.monkey.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.stream.Stream;

import com.warrington.monkey.ast.*;
import com.warrington.monkey.parser.Parser;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;

import com.warrington.monkey.lexer.Lexer;
import com.warrington.monkey.token.Token;
import com.warrington.monkey.token.TokenType;
import org.junit.jupiter.params.provider.MethodSource;

class ParserTest {

    @ParameterizedTest
    @CsvSource({
        "-a * b,((-a) * b)",
        "!-a,(!(-a))",
        "a + b + c,((a + b) + c)",
        "a + b - c,((a + b) - c)",
        "a * b * c,((a * b) * c)",
        "a * b / c,((a * b) / c)",
        "a + b / c,(a + (b / c))",
        "a + b * c + d / e - f,(((a + (b * c)) + (d / e)) - f)",
        "3 + 4; -5 * 5,(3 + 4)((-5) * 5)",
        "5 > 4 == 3 < 4,((5 > 4) == (3 < 4))",
        "5 < 4 != 3 > 4,((5 < 4) != (3 > 4))",
        "3 + 4 * 5 == 3 * 1 + 4 * 5,((3 + (4 * 5)) == ((3 * 1) + (4 * 5)))",
        "1 + (2 + 3) + 4,((1 + (2 + 3)) + 4)",
        "(5 + 5) * 2,((5 + 5) * 2)",
        "2 / (5 + 5),(2 / (5 + 5))",
        "-(5 + 5),(-(5 + 5))",
        "!(true == true),(!(true == true))",
    })
    void testOperatorPrecedenceParsing(String input, String expected) {

        var parser = new Parser(new Lexer(input));

        Program program = parser.parseProgram();

        checkParserErrors(parser);

        var actual = program.toString();

        assertThat(actual)
            .isEqualTo(expected);
    }

    private static Stream<Arguments> provideInfixExpressions() {
        return Stream.of(
            Arguments.of("5 + 5", 5, "+", 5),
            Arguments.of("5 - 5", 5, "-", 5),
            Arguments.of("5 * 5", 5, "*", 5),
            Arguments.of("5 / 5", 5, "/", 5),
            Arguments.of("5 > 5", 5, ">", 5),
            Arguments.of("5 < 5", 5, "<", 5),
            Arguments.of("5 == 5", 5, "==", 5),
            Arguments.of("5 != 5", 5, "!=", 5),
            Arguments.of("true == true", true, "==", true),
            Arguments.of("true != false", true, "!=", false),
            Arguments.of("false == false", false, "==", false)
        );
    }



    @ParameterizedTest
    @MethodSource("provideInfixExpressions")
    void testParsingInfixExpressions(String input, Object left, String operator, Object right) {
        var lexer = new Lexer(input);

        var parser = new Parser(lexer);

        Program program = parser.parseProgram();

        checkParserErrors(parser);

        List<Statement> statements = program.getStatements();

        assertThat(statements.size())
            .withFailMessage("program does not contain %d statements. got=%d", 1, statements.size())
            .isEqualTo(1);

        ExpressionStatement stmt = (ExpressionStatement) statements.getFirst();

        testInfixExpression(stmt.getExpression(), left, operator, right);
    }

    private static Stream<Arguments> providePrefixExpressions() {
        return Stream.of(
            Arguments.of("!5;", "!", 5),
            Arguments.of("-15;", "-", 15),
            Arguments.of("!true;", "!", true),
            Arguments.of("!false;", "!", false)
        );
    }

    @ParameterizedTest
    @MethodSource("providePrefixExpressions")
    void testParsingPrefixExpressions(String input, String operator, Object value) {
        var lexer = new Lexer(input);

        var parser = new Parser(lexer);

        var program = parser.parseProgram();

        checkParserErrors(parser);

        List<Statement> statements = program.getStatements();

        assertThat(statements.size())
            .withFailMessage("program.getStatements() does not contain %d statements. got=%d", 1, statements.size())
            .isEqualTo(1);

        var stmt = (ExpressionStatement) statements.getFirst();

        var exp = (PrefixExpression) stmt.getExpression();

        assertThat(exp.operator())
            .withFailMessage("exp.operator is not '%s'.  got=%s".formatted(operator, exp.operator()))
            .isEqualTo(operator);

        testLiteralExpression(exp.right(), value);
    }

    @Test
    void testIntegerLiteralExpression() {
        final var input = "5;";

        final var lexer = new Lexer(input);

        final var parser = new Parser(lexer);

        final Program program = parser.parseProgram();

        checkParserErrors(parser);

        final List<Statement> statements = program.getStatements();

        assertThat(statements.size())
            .withFailMessage("program has not enough statements. got=%d", statements.size())
            .isEqualTo(1);

        final ExpressionStatement stmt = (ExpressionStatement) statements.getFirst();

        final var literal = (IntegerLiteral) stmt.getExpression();

        assertThat(literal.value())
            .withFailMessage("literal.value() not %s. got=%s", "foobar", literal.value())
            .isEqualTo(5);

        assertThat(literal.tokenLiteral())
            .withFailMessage("literal.tokenLiteral() not %s. got=%s", "foobar", literal.tokenLiteral())
            .isEqualTo("5");
    }

    @Test
    void testIdentifierExpression() {
        final var input = "foobar;";

        final var lexer = new Lexer(input);

        final var parser = new Parser(lexer);

        final Program program = parser.parseProgram();

        checkParserErrors(parser);

        final List<Statement> statements = program.getStatements();

        assertThat(statements.size())
            .withFailMessage("program has not enough statements. got=%d", statements.size())
            .isEqualTo(1);

        final ExpressionStatement stmt = (ExpressionStatement) statements.getFirst();

        testIdentifier(stmt.getExpression(), "foobar");
    }

    @Test
    void testBooleanExpression() {
        final var input = "true;";

        final var lexer = new Lexer(input);

        final var parser = new Parser(lexer);

        final Program program = parser.parseProgram();

        checkParserErrors(parser);

        final List<Statement> statements = program.getStatements();

        assertThat(statements.size())
            .withFailMessage("program has not enough statements. got=%d", statements.size())
            .isEqualTo(1);

        final ExpressionStatement stmt = (ExpressionStatement) statements.getFirst();

        testLiteralExpression(stmt.getExpression(), true);
    }

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

        checkParserErrors(parser);

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

    @Test
    void testString() {
        Program program = new Program();

        LetStatement letStatement = new LetStatement(new Token(TokenType.LET, "let"));
        Identifier myVar = new Identifier(new Token(TokenType.IDENT, "myVar"), "myVar");
        Identifier anotherVar = new Identifier(new Token(TokenType.IDENT, "anotherVar"), "anotherVar");

        letStatement.setName(myVar);
        letStatement.setValue(anotherVar);

        program.addStatement(letStatement);

        if (!program.toString().equals("let myVar = anotherVar;")) {
            fail("program.string() wrong. got=%s".formatted(program.toString()));
        }
    }

    @Test
    void testReturnStatements() {
        final var input = """
            return 5;
            return 10;
            return 993322;
            """;

        final var lexer = new Lexer(input);

        final var parser = new Parser(lexer);

        final Program program = parser.parseProgram();

        checkParserErrors(parser);

        if (program == null) {
            fail("parseProgram() returned null");
        }

        if (program.getStatements().size() != 3) {
            fail("program.getStatements() does not contain 3 statements. got=%d"
                .formatted(program.getStatements().size()));
        }

        program.getStatements().forEach(statement -> {
            if (statement instanceof ReturnStatement returnStatement) {
                if (!returnStatement.tokenLiteral().equals("return")) {
                    fail("statement.tokenLiteral not 'return'. got=%s".formatted(statement.tokenLiteral()));
                }
            } else {
                fail("statement is not a ReturnStatement");
            }
        });
    }

    void testLiteralExpression(
        Expression expression,
        Object expected
    ) {
        switch (expected) {
            case Integer i -> testIntegerLiteral(expression, i);
            case String s -> testIdentifier(expression, s);
            case Boolean b -> testBoolean(expression, b);
            default -> fail("type of expression not handled! got=%s".formatted(expected.getClass()));
        }
    }

    void testInfixExpression(
        Expression expression,
        Object left,
        String operator,
        Object right
    ) {
        InfixExpression opExp = (InfixExpression) expression;

        testLiteralExpression(opExp.left(), left);

        assertThat(opExp.operator())
            .withFailMessage("opExp.operator() is not '%s'. got=%s", operator, opExp.operator())
            .isEqualTo(operator);

        testLiteralExpression(opExp.right(), right);
    }

    boolean testLetStatement(Statement statement, String name) {
        if (!statement.tokenLiteral().equals("let")) {
            fail("statement.tokenLiteral not 'let'. got=%s".formatted(statement.tokenLiteral()));
            return false;
        }

        if (statement instanceof LetStatement letStmt) {
            if (!letStmt.getName().value().equals(name)) {
                fail("letStmt.getName().getValue() not '%s'. got=%s".formatted(name, letStmt.getName().value()));
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

    void testBoolean(Expression expression, boolean value) {
        var bool = (MonkeyBoolean) expression;

        assertThat(bool.value())
            .withFailMessage("bool.value() not %s. got %s", value, bool.value())
            .isEqualTo(value);

        assertThat(bool.tokenLiteral())
            .withFailMessage("bool.tokenLiteral() not %s. got %s", value, bool.tokenLiteral())
            .isEqualTo("%s".formatted(value));
    }

    void testIdentifier(Expression expression, String value) {
        var ident = (Identifier) expression;

        assertThat(ident.value())
            .withFailMessage("ident.value() not %s. got %s", value, ident.value())
            .isEqualTo(value);

        assertThat(ident.tokenLiteral())
            .withFailMessage("ident.tokenLiteral() not %s. got %s", value, ident.tokenLiteral())
            .isEqualTo(value);
    }

    void testIntegerLiteral(Expression intLiteralExpression, int value) {
        var intLiteral = (IntegerLiteral) intLiteralExpression;

        assertThat(intLiteral.value())
            .withFailMessage("intL.value() not %d. got=%d", value, intLiteral.value())
            .isEqualTo(value);

        assertThat(intLiteral.tokenLiteral())
            .withFailMessage("integerLiteral.tokenLiteral() not %d.  got=%s", value, intLiteral.tokenLiteral())
            .isEqualTo(String.valueOf(value));
    }

    void checkParserErrors(Parser parser) {
        var softly = new SoftAssertions();

        final List<String> errors = parser.errors();

        if (errors.isEmpty()) {
            return;
        }

        errors.forEach(msg -> softly.fail("parser error: %s", msg));

        softly.assertAll();
    }
}
