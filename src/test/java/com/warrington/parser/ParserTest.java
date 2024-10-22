package com.warrington.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.warrington.ast.Expression;
import com.warrington.ast.ExpressionStatement;
import com.warrington.ast.Identifier;
import com.warrington.ast.InfixExpression;
import com.warrington.ast.IntegerLiteral;
import com.warrington.ast.LetStatement;
import com.warrington.ast.PrefixExpression;
import com.warrington.ast.Program;
import com.warrington.ast.ReturnStatement;
import com.warrington.ast.Statement;
import com.warrington.lexer.Lexer;
import com.warrington.token.Token;
import com.warrington.token.TokenType;

class ParserTest {

    @ParameterizedTest
    @CsvSource({
        "5 + 5,5,+,5",
        "5 - 5,5,-,5",
        "5 * 5,5,*,5",
        "5 / 5,5,/,5",
        "5 > 5,5,>,5",
        "5 < 5,5,<,5",
        "5 == 5,5,==,5",
        "5 != 5,5,!=,5",
    })
    void testParsingInfixExpressions(String input, int left, String operator, int right) {
        var lexer = new Lexer(input);
        
        var parser = new Parser(lexer);

        Program program = parser.parseProgram();

        checkParserErrors(parser);

        List<Statement> statements = program.getStatements();

        assertThat(statements.size())
            .withFailMessage("program does not contain %d statements. got=%d", 1, statements.size())
            .isEqualTo(1);
        
        ExpressionStatement stmt = (ExpressionStatement) statements.get(0);

        InfixExpression expression = (InfixExpression) stmt.getExpression();        
        
        testIntegerLiteral(expression.left(), left);

        assertThat(expression.operator())
            .withFailMessage("exp.operator() is not '%s'. got=%s", operator, expression.operator())
            .isEqualTo(operator);

        testIntegerLiteral(expression.right(), left);
    }

    @ParameterizedTest
    @CsvSource({"!5;,!,5", "-15;,-,15"})
    void testParsingPrefixExpressions(String input, String operator, int integerValue) {
        var lexer = new Lexer(input);

        var parser = new Parser(lexer);

        var program = parser.parseProgram();

        checkParserErrors(parser);

        List<Statement> statements = program.getStatements();

        assertThat(statements.size())
            .withFailMessage("program.getStatements() does not contain %d statements. got=%d", 1, statements.size())
            .isEqualTo(1);

        var stmt = (ExpressionStatement) statements.get(0);

        var exp = (PrefixExpression) stmt.getExpression();

        assertThat(exp.operator())
            .withFailMessage("exp.operator is not '%s'.  got=%s".formatted(operator, exp.operator()))
            .isEqualTo(operator);

        testIntegerLiteral(exp.right(), integerValue);
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

        final ExpressionStatement stmt = (ExpressionStatement) statements.get(0);

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

        final ExpressionStatement stmt = (ExpressionStatement) statements.get(0);

        final var ident = (Identifier) stmt.getExpression();

        assertThat(ident.value())
            .withFailMessage("ident.getValue() not %s. got=%s", "foobar", ident.value())
            .isEqualTo("foobar");

        assertThat(ident.tokenLiteral())
            .withFailMessage("ident.tokenLiteral() not %s. got=%s", "foobar", ident.tokenLiteral())
            .isEqualTo("foobar");
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

        if (errors.size() == 0) {
            return;
        }

        errors.forEach(msg -> softly.fail("parser error: %s", msg));

        softly.assertAll();
    }
}
