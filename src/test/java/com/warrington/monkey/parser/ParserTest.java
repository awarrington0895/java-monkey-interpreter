package com.warrington.monkey.parser;

import com.warrington.monkey.ast.*;
import com.warrington.monkey.lexer.Lexer;
import com.warrington.monkey.token.Token;
import com.warrington.monkey.token.TokenType;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class ParserTest {

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

    private static Stream<Arguments> providePrefixExpressions() {
        return Stream.of(
            Arguments.of("!5;", "!", 5),
            Arguments.of("-15;", "-", 15),
            Arguments.of("!true;", "!", true),
            Arguments.of("!false;", "!", false)
        );
    }

    private static Stream<Arguments> provideFunctionParameters() {
        return Stream.of(
            Arguments.of("fn() {};", Collections.emptyList()),
            Arguments.of("fn(x) {};", List.of("x")),
            Arguments.of("fn(x, y, z) {};", List.of("x", "y", "z"))
        );
    }

    private static Stream<Arguments> provideLetStatements() {
        return Stream.of(
            Arguments.of("let x = 5;", "x", 5),
            Arguments.of("let y = true;", "y", true),
            Arguments.of("let foobar = y", "foobar", "y")
        );
    }

    private static Stream<Arguments> provideReturnStatements() {
        return Stream.of(
            Arguments.of("return 5;", 5),
            Arguments.of("return true;", true),
            Arguments.of("return foobar;", "foobar")
        );
    }

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
        "a + add(b * c) + d,((a + add((b * c))) + d)",
        "add(a + b + c * d / f + g),add((((a + b) + ((c * d) / f)) + g))",
        "'a * [1, 2, 3, 4][b * c] * d','((a * ([1,2,3,4][(b * c)])) * d)'",
        "'add(a * b[2], b[1], 2 * [1, 2][1])','add((a * (b[2])), (b[1]), (2 * ([1,2][1])))'",
    })
    void testOperatorPrecedenceParsing(String input, String expected) {

        var parser = new Parser(new Lexer(input));

        Program program = parser.parseProgram();

        checkParserErrors(parser);

        var actual = program.toString();

        assertThat(actual)
            .isEqualTo(expected);
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

    @Nested
    @DisplayName("ParseHashLiteral")
    class HashLiteralTests {
        @Test
        void testParsingHashLiteralStringKeys() {
            final var input = """
            {"one": 1, "two": 2, "three": 3}""";

            List<Statement> statements = testParse(input);

            final var expression = ((ExpressionStatement) statements.getFirst()).getExpression();

            assertThat(expression)
                .withFailMessage("exp not HashLiteral. got=%s", expression)
                .isInstanceOf(HashLiteral.class);

            final var hashLiteral = (HashLiteral) expression;

            assertThat(hashLiteral.pairs())
                .hasSize(3);

            var expected = Map.of(
                "one", 1L,
                "two", 2L,
                "three", 3L
            );

            for (Map.Entry<Expression, Expression> entry : hashLiteral.pairs().entrySet()) {
                assertThat(entry.getKey())
                    .isInstanceOf(StringLiteral.class);

                if (entry.getKey() instanceof StringLiteral sl) {
                    testIntegerLiteral(entry.getValue(), expected.get(sl.toString()));
                }
            }
        }

        @Test
        void testParsingHashWithSinglePair() {
            final var input = """
            {"one": 1}""";

            List<Statement> statements = testParse(input);

            final var expression = ((ExpressionStatement) statements.getFirst()).getExpression();

            assertThat(expression)
                .withFailMessage("exp not HashLiteral. got=%s", expression)
                .isInstanceOf(HashLiteral.class);

            final var hashLiteral = (HashLiteral) expression;

            assertThat(hashLiteral.pairs())
                .hasSize(1);

            var expected = Map.of(
                "one", 1L
            );

            for (Map.Entry<Expression, Expression> entry : hashLiteral.pairs().entrySet()) {
                assertThat(entry.getKey())
                    .isInstanceOf(StringLiteral.class);

                if (entry.getKey() instanceof StringLiteral sl) {
                    testIntegerLiteral(entry.getValue(), expected.get(sl.toString()));
                }
            }
        }
        @Test
        void testParsingEmptyHashLiteral() {
            final var input = "{}";

            List<Statement> statements = testParse(input);

            final var expression = ((ExpressionStatement) statements.getFirst()).getExpression();

            assertThat(expression)
                .isInstanceOf(HashLiteral.class);

            if (expression instanceof HashLiteral hl) {
                assertThat(hl.pairs())
                    .hasSize(0);
            }
        }

        @Test
        void testParsingHashLiteralWithExpressions() {
            final var input = """
            {"one": 0 + 1, "two": 10 - 8, "three": 15 / 5}""";

            List<Statement> statements = testParse(input);

            final var expression = ((ExpressionStatement) statements.getFirst()).getExpression();

            assertThat(expression)
                .withFailMessage("exp not HashLiteral. got=%s", expression)
                .isInstanceOf(HashLiteral.class);

            final var hashLiteral = (HashLiteral) expression;

            assertThat(hashLiteral.pairs())
                .hasSize(3);

            var expected = Map.of(
                "one", 1L,
                "two", 2L,
                "three", 3L
            );

            Map<String, Consumer<Expression>> tests = Map.of(
                "one", exp -> testInfixExpression(exp, 0, "+", 1),
                "two", exp -> testInfixExpression(exp, 10, "-", 8),
                "three", exp -> testInfixExpression(exp, 15, "/", 5)
            );

            for (var entry : hashLiteral.pairs().entrySet()) {
                var key = entry.getKey();

                assertThat(key)
                    .isInstanceOf(StringLiteral.class);

                if (key instanceof StringLiteral sl) {
                    var testFunc = tests.get(sl.toString());

                    assertThat(testFunc)
                        .withFailMessage("No test function for key %s found", sl.toString())
                        .isNotNull();

                    testFunc.accept(entry.getValue());
                }
            }
        }
    }

    @Test
    void testParsingEmptyArrayLiteral() {
        final var input = "[]";

        var expression = ((ExpressionStatement) testParse(input).getFirst()).getExpression();

        assertThat(expression)
            .withFailMessage("exp not ArrayLiteral. got=%s", expression)
            .isInstanceOf(ArrayLiteral.class);

        var array = (ArrayLiteral) expression;

        assertThat(array.elements().size())
            .withFailMessage("array.elements().size() is not 0. got=%d", array.elements().size())
            .isEqualTo(0);
    }

    @Test
    void testParsingIndexExpression() {
        final var input = "myArray[1 + 1]";

        List<Statement> stmts = testParse(input);

        ExpressionStatement expStmt = (ExpressionStatement) stmts.getFirst();

        assertThat(expStmt.getExpression())
            .withFailMessage("exp not IndexExpression. got=%s", expStmt.getExpression().getClass())
            .isInstanceOf(IndexExpression.class);

        var indexExp = (IndexExpression) expStmt.getExpression();

        testIdentifier(indexExp.left(), "myArray");

        testInfixExpression(indexExp.index(), 1, "+", 1);
    }

    @Test
    void testParsingArrayLiteral() {
        final var input = "[1, 2 * 2, 3 + 3]";

        var expression = ((ExpressionStatement) testParse(input).getFirst()).getExpression();

        assertThat(expression)
            .withFailMessage("exp not ArrayLiteral. got=%s", expression)
            .isInstanceOf(ArrayLiteral.class);

        var array = (ArrayLiteral) expression;

        assertThat(array.elements().size())
            .withFailMessage("array.elements().size() is not 3. got=%d", array.elements().size())
            .isEqualTo(3);

        testIntegerLiteral(array.elements().getFirst(), 1);
        testInfixExpression(array.elements().get(1), 2, "*", 2);
        testInfixExpression(array.elements().get(2), 3, "+", 3);
    }

    List<Statement> testParse(String input) {
        final var lexer = new Lexer(input);

        final var parser = new Parser(lexer);

        final Program program = parser.parseProgram();

        checkParserErrors(parser);

        return program.getStatements();
    }

    @Test
    void testStringLiteralExpression() {
        final var input = "\"hello world!\";";

        final var lexer = new Lexer(input);

        final var parser = new Parser(lexer);

        final Program program = parser.parseProgram();

        checkParserErrors(parser);

        final List<Statement> statements = program.getStatements();

        assertThat(statements.size())
            .withFailMessage("program has not enough statements. got=%d", statements.size())
            .isEqualTo(1);

        final ExpressionStatement stmt = (ExpressionStatement) statements.getFirst();

        assertThat(stmt.getExpression())
            .withFailMessage("expression is not StringLiteral. got=%s.".formatted(stmt.getExpression().getClass()))
            .isInstanceOf(StringLiteral.class);

        final var literal = (StringLiteral) stmt.getExpression();

        assertThat(literal.tokenLiteral())
            .withFailMessage("literal.tokenLiteral() not %s. got=%s", "foobar", literal.tokenLiteral())
            .isEqualTo("hello world!");
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
    void testIfElseExpression() {
        final var input = "if (x < y) { x } else { y }";

        final var lexer = new Lexer(input);

        final var parser = new Parser(lexer);

        final Program program = parser.parseProgram();

        checkParserErrors(parser);

        final List<Statement> statements = program.getStatements();

        assertThat(statements.size())
            .withFailMessage("program has not enough statements. got=%d", statements.size())
            .isEqualTo(1);

        final ExpressionStatement stmt = (ExpressionStatement) statements.getFirst();

        final IfExpression ifExpression = (IfExpression) stmt.getExpression();

        testInfixExpression(ifExpression.condition(), "x", "<", "y");

        var consequenceSize = ifExpression.consequence().statements().size();

        assertThat(consequenceSize)
            .withFailMessage("consequence is not 1 statements. got=%d.", consequenceSize)
            .isEqualTo(1);

        var consequence = (ExpressionStatement) ifExpression.consequence().statements().getFirst();

        testIdentifier(consequence.getExpression(), "x");

        var alternativeSize = ifExpression.alternative().statements().size();

        assertThat(consequenceSize)
            .withFailMessage("alternative is not 1 statements. got=%d.", alternativeSize)
            .isEqualTo(1);

        var alternative = (ExpressionStatement) ifExpression.alternative().statements().getFirst();

        testIdentifier(alternative.getExpression(), "y");
    }

    @Test
    void testParsingFunctionLiteral() {
        final var input = "fn(x, y) { x + y; }";

        final var lexer = new Lexer(input);

        final var parser = new Parser(lexer);

        final Program program = parser.parseProgram();

        checkParserErrors(parser);

        final List<Statement> statements = program.getStatements();

        assertThat(statements.size())
            .withFailMessage("program has not enough statements. got=%d", statements.size())
            .isEqualTo(1);

        final ExpressionStatement stmt = (ExpressionStatement) statements.getFirst();

        var functionLiteral = (FunctionLiteral) stmt.getExpression();

        assertThat(functionLiteral.parameters().size())
            .withFailMessage("function literal parameters wrong. want 2. got=%d", functionLiteral.parameters().size())
            .isEqualTo(2);

        testIdentifier(functionLiteral.parameters().getFirst(), "x");
        testIdentifier(functionLiteral.parameters().get(1), "y");

        int bodySize = functionLiteral.body().statements().size();

        assertThat(bodySize)
            .withFailMessage("body statements has not 1 statements. got=%d", bodySize)
            .isEqualTo(1);

        var bodyStatement = (ExpressionStatement) functionLiteral.body().statements().getFirst();

        testInfixExpression(bodyStatement.getExpression(), "x", "+", "y");
    }

    @ParameterizedTest
    @MethodSource("provideFunctionParameters")
    void testParsingFunctionParameters(String input, List<String> expectedParameters) {
        final var lexer = new Lexer(input);

        final var parser = new Parser(lexer);

        final Program program = parser.parseProgram();

        checkParserErrors(parser);

        final List<Statement> statements = program.getStatements();

        assertThat(statements.size())
            .withFailMessage("program has not enough statements. got=%d", statements.size())
            .isEqualTo(1);

        final ExpressionStatement stmt = (ExpressionStatement) statements.getFirst();

        var functionLiteral = (FunctionLiteral) stmt.getExpression();

        List<Identifier> parameters = functionLiteral.parameters();

        assertThat(parameters.size())
            .withFailMessage("length parameters wrong. want %d, got=%d", expectedParameters.size(), parameters.size())
            .isEqualTo(expectedParameters.size());

        for (int i = 0; i < parameters.size(); i++) {
            testLiteralExpression(parameters.get(i), expectedParameters.get(i));
        }
    }

    @Test
    void testCallExpressionParsing() {
        final var input = "add(1, 2 * 3, 4 + 5);";

        final var lexer = new Lexer(input);

        final var parser = new Parser(lexer);

        final Program program = parser.parseProgram();

        checkParserErrors(parser);

        final List<Statement> statements = program.getStatements();

        assertThat(statements.size())
            .withFailMessage("program has not enough statements. got=%d", statements.size())
            .isEqualTo(1);

        final ExpressionStatement stmt = (ExpressionStatement) statements.getFirst();

        final CallExpression callExp = (CallExpression) stmt.getExpression();

        var arguments = callExp.arguments();

        assertThat(arguments.size())
            .withFailMessage("wrong number of arguments. expected 3. got=%d", arguments.size())
            .isEqualTo(3);

        testIdentifier(callExp.function(), "add");

        testLiteralExpression(arguments.getFirst(), 1);
        testInfixExpression(arguments.get(1), 2, "*", 3);
        testInfixExpression(arguments.get(2), 4, "+", 5);
    }

    @Test
    void testIfExpression() {
        final var input = "if (x < y) { x }";

        final var lexer = new Lexer(input);

        final var parser = new Parser(lexer);

        final Program program = parser.parseProgram();

        checkParserErrors(parser);

        final List<Statement> statements = program.getStatements();

        assertThat(statements.size())
            .withFailMessage("program has not enough statements. got=%d", statements.size())
            .isEqualTo(1);

        final ExpressionStatement stmt = (ExpressionStatement) statements.getFirst();

        final IfExpression ifExpression = (IfExpression) stmt.getExpression();

        testInfixExpression(ifExpression.condition(), "x", "<", "y");

        var consequenceSize = ifExpression.consequence().statements().size();

        assertThat(consequenceSize)
            .withFailMessage("consequence is not 1 statements. got=%d.", consequenceSize)
            .isEqualTo(1);

        var consequence = (ExpressionStatement) ifExpression.consequence().statements().getFirst();

        testIdentifier(consequence.getExpression(), "x");

        assertThat(ifExpression.alternative())
            .isNull();
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

    @ParameterizedTest
    @MethodSource("provideLetStatements")
    void testLetStatements(String input, String expectedIdentifier, Object expectedValue) {
        final var lexer = new Lexer(input);

        final var parser = new Parser(lexer);

        final Program program = parser.parseProgram();

        checkParserErrors(parser);

        if (program == null) {
            fail("parseProgram() returned null");
        }

        if (program.getStatements().size() != 1) {
            fail("program.getStatements() does not contain 1 statements. got=%d"
                .formatted(program.getStatements().size()));
        }

        Statement stmt = program.getStatements().getFirst();

        if (!testLetStatement(stmt, expectedIdentifier)) {
            return;
        }

        Expression value = ((LetStatement) stmt).value();

        testLiteralExpression(value, expectedValue);
    }

    @Test
    void testString() {
        Program program = new Program();

        var token = new Token(TokenType.LET, "let");
        var myVar = new Identifier(new Token(TokenType.IDENT, "myVar"), "myVar");
        var anotherVar = new Identifier(new Token(TokenType.IDENT, "anotherVar"), "anotherVar");

        LetStatement letStatement = new LetStatement(token, myVar, anotherVar);

        program.addStatement(letStatement);

        if (!program.toString().equals("let myVar = anotherVar;")) {
            fail("program.string() wrong. got=%s".formatted(program.toString()));
        }
    }

    @ParameterizedTest
    @MethodSource("provideReturnStatements")
    void testReturnStatements(String input, Object expectedValue) {
        final var lexer = new Lexer(input);

        final var parser = new Parser(lexer);

        final Program program = parser.parseProgram();

        checkParserErrors(parser);

        if (program == null) {
            fail("parseProgram() returned null");
        }

        if (program.getStatements().size() != 1) {
            fail("program.getStatements() does not contain 1 statements. got=%d"
                .formatted(program.getStatements().size()));
        }

        ReturnStatement stmt = (ReturnStatement) program.getStatements().getFirst();

        assertThat(stmt.tokenLiteral())
            .withFailMessage("stmt.tokenLiteral() not 'return', got='%s'", stmt.tokenLiteral())
            .isEqualTo("return");

        testLiteralExpression(stmt.returnValue(), expectedValue);
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
            if (!letStmt.name().value().equals(name)) {
                fail("letStmt.getName().getValue() not '%s'. got=%s".formatted(name, letStmt.name().value()));
                return false;
            }

            if (!letStmt.name().tokenLiteral().equals(name)) {
                fail("letStmt.getName().tokenLiteral() not '%s'. got=%s".formatted(name,
                    letStmt.name().tokenLiteral()));
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

    void testIntegerLiteral(Expression intLiteralExpression, long value) {
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
